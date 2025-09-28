package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.Output;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.Error;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.util.*;

import static java.lang.Boolean.TRUE;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private final FusionSlam FS;
    private final ServicesCounter counterInstance = ServicesCounter.getInstance();
    private final StatisticalFolder SFolderInstance = StatisticalFolder.getInstance();
    private final Error errorInstance = Error.getInstance();
    private final Output outputInstance;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("Fusion Slam Service");
        this.FS = fusionSlam;
        this.outputInstance = new Output();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        counterInstance.onFusion();

        //subscribe to TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, (TrackedObjectsEvent trackedEvent) -> {
            FS.addList(trackedEvent.getDetectedObjects());
            complete(trackedEvent, TRUE);
        });
        subscribeEvent(PoseEvent.class, (PoseEvent p) -> {
            FS.addPose(p.getCurrentPose());
            complete(p, TRUE);
        });


        //subscribe to tickBroadcast
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {

            if (!counterInstance.areServicesFinished()) {
                List<List<TrackedObject>> trackedObjects = FS.getTrackedObjects();
                Iterator<List<TrackedObject>> iterator = trackedObjects.iterator();

                while (iterator.hasNext()) {
                    List<TrackedObject> currList = iterator.next();
                    int listTime = currList.get(0).getTime();
                    if (listTime <= FS.getPoses().size()) {
                        //get the current pose based on the detection timestamp
                        Pose currPose = FS.getPoseForTick(listTime);
                        FS.addLandMarkLogic(currList, currPose); // method that checks if the landMark exists or not, and updates the map according to
                        iterator.remove();
                    }
                }
            } else {
                outputInstance.outputForTermination();
                sendBroadcast(new TerminatedBroadcast(this, "FusionSlam"));
                counterInstance.offFusion();
                terminate();
            }

        });


        //subscribe to PoseEvent
        subscribeEvent(PoseEvent.class, (PoseEvent poseEvent) -> {
            Pose currentPose = poseEvent.getCurrentPose(); // extract the pose from the event
            FS.addPose(currentPose); //store the pose in FusionSLAM
            complete(poseEvent, TRUE);

        });


        //subscribe to terminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
            if (terminate.getServiceClass() == TimeService.class) {
                outputInstance.outputForTermination();
                sendBroadcast(new TerminatedBroadcast(this, "FusionSlam"));
                counterInstance.offFusion();
                terminate();
            }
        });

        //subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            outputInstance.outputForCrashing();
            counterInstance.offFusion();
            terminate();
        });

        GurionRockRunner.latch.countDown();
    }

}



