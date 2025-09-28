package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.Error;

import java.util.List;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private GPSIMU gpsimu;
    private final ServicesCounter counterInstance= ServicesCounter.getInstance();
    private final StatisticalFolder SFolderInstance= StatisticalFolder.getInstance();
    private final bgu.spl.mics.application.objects.Error errorInstance= Error.getInstance();
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("Pose Service");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        //subscribe to tickBroadcast
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getCurrentTick();

            if (currentTick <= gpsimu.maxPoseTime()) {
                gpsimu.setCurrentTick(currentTick);

                Pose currPose = gpsimu.getPoseForTick(currentTick);
                if (currPose != null) {
                    errorInstance.addPose(currPose);
                    sendEvent(new PoseEvent(currPose));
                }
            }
            //need to terminate because it's finished its job
            else {
                this.gpsimu.setStatus(STATUS.DOWN);
                counterInstance.decrementPoseServices();
                sendBroadcast(new TerminatedBroadcast(this, "pose service"));
                terminate();
            }
        });


        //subscribe to terminatedBroadcast
        //terminate because time runs out
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
            if(terminate.getServiceClass() == TimeService.class) {
                gpsimu.setStatus(STATUS.DOWN);
                terminate();
            }
        });

        //subscribe to crashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            gpsimu.setStatus(STATUS.ERROR);
            counterInstance.decrementPoseServices();
            terminate();

        });

        GurionRockRunner.latch.countDown();
    }

    public void setPoses(List<Pose> poseList) {
        this.gpsimu.setPoses(poseList);
    }
}
