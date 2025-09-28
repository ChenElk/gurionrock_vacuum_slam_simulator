package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.Error;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private final Camera camera;
    private final ServicesCounter counterInstance= ServicesCounter.getInstance();
    private final StatisticalFolder SFolderInstance= StatisticalFolder.getInstance();
    private final Error errorInstance= Error.getInstance();
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera Service");
        this.camera = camera;
        counterInstance.incrementCameraServices();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {

            int currentTick = tick.getCurrentTick();
            //if there are left objects to send
            if (camera.checkIfLeft(currentTick)) {
                String checkError = camera.checkIfErrorExists(currentTick);
                //if there is an error we need to send crashed broadcast
                if (checkError != null) {
                    camera.setStatus(STATUS.ERROR);
                    errorInstance.setError(checkError);
                    errorInstance.setFaultySensor("Camera" + camera.getId());
                    sendBroadcast(new CrashedBroadcast("camera" + camera.getId(), checkError));
                }
                //there is no error, we need to send events.
                else{
                    StampedDetectedObjects currentStamp= camera.getDetectObjectsByTime(currentTick);

                    camera.addObjectsToSend(currentStamp); //method that updates the objects to send with current time + camera frequency
                    StampedDetectedObjects stampedToSend= camera.getStampedToSend(currentTick); //find the stamped object to send from the updated list
                    //No valid stamped object to send at tick
                    if (stampedToSend == null || stampedToSend.getDetectedObjects().isEmpty()) {
                        return; //skip processing for this tick
                    }

                    int numObjects= stampedToSend.getDetectedObjects().size();

                    for (int i = 0; i < numObjects; i = i + 1) {
                        SFolderInstance.incrementDetectedObjects();
                    }

                    DetectObjectsEvent event = new DetectObjectsEvent(new StampedDetectedObjects(currentTick-camera.getFrequency(), stampedToSend.getDetectedObjects()));
                    sendEvent(event);
                }
            }

            //if there are no left objects to send, the camera service should terminate
            else{
                sendBroadcast(new TerminatedBroadcast(this, "camera" + camera.getId()));
                counterInstance.decrementCameraServices();
                camera.setStatus(STATUS.DOWN);
                terminate();
            }
        });

        //subscribe to terminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
            //the only time a camera service should terminate is when time runs out
            if(terminate.getServiceClass() == TimeService.class){
                counterInstance.decrementCameraServices();
                camera.setStatus(STATUS.DOWN);
                terminate();
            }
        });

        //subscribe to crashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            counterInstance.decrementCameraServices();
            camera.setStatus(STATUS.ERROR);
            terminate();
        });

        GurionRockRunner.latch.countDown();
    }
}

