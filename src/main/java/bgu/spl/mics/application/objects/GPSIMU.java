package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    List<Pose> poseList;

    public GPSIMU(List<Pose> poses){
        this.currentTick = 0;
        this.status= STATUS.UP;
        this.poseList= poses;
    }


    public int getCurrentTick(){
        return currentTick;
    }

    public List<Pose> getPosesList(){
        return poseList;
    }

    public void setStatus(STATUS status){
        this.status = status;
    }

    public void setPoses(List<Pose> poseList){
        this.poseList = poseList;
    }

    public void setCurrentTick(int tick) {
        this.currentTick = tick;
    }

    public Pose getPoseForTick(int tick) {
        int index = tick - 1; // adjusting for 1-based or 0-based indexing
        if (index >= 0 && index < poseList.size()) {
            return poseList.get(index);
        }
        return null;
    }

    public int maxPoseTime(){
        return poseList.size();
    }
}
