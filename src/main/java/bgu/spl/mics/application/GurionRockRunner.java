package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 */
public class GurionRockRunner {
    public static CountDownLatch latch;
    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("provide a configuration file");
            return;
        }
        String configFile = args[0]; //config file path
        String configFileDir= configFile.substring(0, configFile.lastIndexOf('/')); //chen
        Output.outputDirectoryPath = configFileDir;

        try{
            Gson gson = new Gson();
            Parsers parser= new Parsers(gson);
            Configuration config= parser.parseConfiguration(configFile);

            if (config == null) {
                System.err.println("invalid configuration file");
                return;
            }

            StatisticalFolder sFolder = StatisticalFolder.getInstance();
            ServicesCounter sCounter= ServicesCounter.getInstance();
            int numOfServices = 0;
            List<Thread> threads = new ArrayList<>();

            //initialize cameras
            List<Camera> cameras = new ArrayList<>();
            for (Configuration.CameraConfig camConfig : config.getCameras().getCamerasConfigurations()) {
                // Create Camera object
                Camera camera = new Camera(camConfig.getId(), camConfig.getFrequency());
                cameras.add(camera);

                // Parse and populate camera data
                //List<StampedDetectedObjects> cameraData = parser.camerasParser(config.getCameras().getCameraDatasPath(), camConfig.getCameraKey());
                List<StampedDetectedObjects> cameraData = parser.camerasParser(configFileDir + config.getCameras().getCameraDatasPath().substring(1), camConfig.getCameraKey()); //chen
                for (StampedDetectedObjects stampedObject : cameraData) {
                    camera.addDetectedObject(stampedObject);
                }

                // Create and start CameraService
                CameraService cameraService = new CameraService(camera);
                numOfServices++;

                Thread cameraThread = new Thread(() -> {
                    try {
                        cameraService.run();
                    } catch (Exception e) {
                        System.err.println("CameraService error: " + e.getMessage());
                    }
                });
                threads.add(cameraThread);
            }

        //initialize lidars
        //LiDarDataBase lidarDataBase = parser.lidarsParser(config.getLiDarWorkers().getLidarsDataPath());
        LiDarDataBase lidarDataBase = parser.lidarsParser(configFileDir + config.getLiDarWorkers().getLidarsDataPath().substring(1)); //chen
        for (Configuration.LidarConfig lidarConfig : config.getLiDarWorkers().getLidarConfigurations()) {
            LiDarWorkerTracker lidarWorker = new LiDarWorkerTracker(lidarConfig.getId(), lidarConfig.getFrequency(), lidarDataBase);

            // Create and start LiDarWorkerService
            LiDarService lidarService = new LiDarService(lidarWorker);
            numOfServices++;

            Thread lidarThread = new Thread(() -> {
                lidarService.run();
            });
            threads.add(lidarThread);
        }

        //initialize poses
        //List<Pose> poses = parser.posesParser(config);
        List<Pose> poses = parser.posesParser(configFileDir+ config.getPosesPath().substring(1));//chen
        GPSIMU gpsimu = new GPSIMU(poses);
        PoseService poseService = new PoseService(gpsimu);
        numOfServices++;

        Thread poseThread = new Thread(() -> {
            poseService.run();
        });
        threads.add(poseThread);


        //initialize fusion slam service
        FusionSlamService fusionSlamService = new FusionSlamService(FusionSlam.getInstance());
        Thread fusionSlamThread = new Thread(() ->{
            try{
                fusionSlamService.run();
            }catch (Exception e) {
                System.err.println("FusionSlamService error: " + e.getMessage());
            }
        });
        threads.add(fusionSlamThread);
        latch = new CountDownLatch(threads.size());  // Latch for TimeService start

        // Start threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all services to initialize
        latch.await();

            // Start TimeService after all services are ready
            TimeService timeService = new TimeService(config.getTickTime(), config.getDuration());
            Thread timeServiceThread = new Thread(timeService);
            timeServiceThread.start();

            // Join threads
            for (Thread thread : threads) {
                try{
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }
            timeServiceThread.join();


        } catch (InterruptedException e) {
            System.err.println("Error while waiting for threads to finish: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error initializing components: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

