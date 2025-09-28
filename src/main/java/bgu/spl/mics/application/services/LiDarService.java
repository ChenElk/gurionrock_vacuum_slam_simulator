package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.Error;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private final LiDarWorkerTracker lidarWorker;
    private final Map<Integer, List<TrackedObject>> trackedObjectsMap;
    private final ServicesCounter counterInstance= ServicesCounter.getInstance();
    private final StatisticalFolder SFolderInstance= StatisticalFolder.getInstance();
    private final Error errorInstance= Error.getInstance();

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDar Service");
        this.lidarWorker = LiDarWorkerTracker;
        this.trackedObjectsMap = new HashMap<>();
        counterInstance.incrementLidarServices();

    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currTick = tick.getCurrentTick();
            if(lidarWorker.foundError(currTick)){
                lidarWorker.setStatus(STATUS.ERROR);
                errorInstance.setFaultySensor("LiDar" + lidarWorker.getId());
                errorInstance.setError("Connection to LiDAR lost");
                sendBroadcast(new CrashedBroadcast("LiDar" + lidarWorker.getId(), "The LiDar sensor disconnected"));
            }
            //if there are still working cameras or there are objects to send
            if(counterInstance.getCameraServices()!=0 || !trackedObjectsMap.isEmpty()){
                //iterate through all the lists in the map
                Iterator<Map.Entry<Integer, List<TrackedObject>>> iterator = trackedObjectsMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, List<TrackedObject>> entry = iterator.next();
                    int listTickTime = entry.getKey();
                    List<TrackedObject> list = entry.getValue();

                    //check if the current tick fits the lidar frequency
                    if (!list.isEmpty() && currTick >= (lidarWorker.getFrequency() + listTickTime)) {
                        //create a trackedObjectsEvent
                        TrackedObjectsEvent event = new TrackedObjectsEvent(listTickTime, lidarWorker.getFrequency(), list);
                        iterator.remove();
                        trackedObjectsMap.remove(listTickTime, list);
                        sendEvent(event);
                    }
                }
            }
            //there are no active cameras or no tracked objects to send
            else{
                sendBroadcast(new TerminatedBroadcast(this, "lidar"+lidarWorker.getId()));
                counterInstance.decrementLidarServices();
                this.lidarWorker.setStatus(STATUS.DOWN);
                terminate();
            }

        });

        //subscribe to detectedObjectEvent
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent detect)->{
            List<TrackedObject> list = lidarWorker.createTrackedObjects(detect.getStampDetectedObjects());
            if (list != null && !list.isEmpty()) {
                int numDetctedObjects=list.size();
                for (int i = 0; i < numDetctedObjects; i=i+1) {
                    SFolderInstance.incrementTrackedObjects();
                }
                trackedObjectsMap.merge(detect.getDetectionTime(), list, (existingList, newList) -> {
                    existingList.addAll(newList); // Merge both lists
                    return existingList; // Return the merged list
                });

                errorInstance.updateLastLiDarWorkersFrame("LiDar"+lidarWorker.getId(), list);
                complete(detect, TRUE);
            }
        });

        //subscribe to terminatedBroadcast

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
            //the only time a lidar service should terminate is when time runs out
            if(terminate.getServiceClass() == TimeService.class) {
                counterInstance.decrementLidarServices();
                lidarWorker.setStatus(STATUS.DOWN);
                terminate();
            }
        });

        //subscribe to crashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            counterInstance.decrementLidarServices();
            lidarWorker.setStatus(STATUS.ERROR);
            terminate();
        });

        GurionRockRunner.latch.countDown();
    }
}
