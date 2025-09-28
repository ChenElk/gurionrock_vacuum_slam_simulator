package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Error {

    private static class ErrorHolder {
        private static final Error instance = new Error();
    }

    private String error;
    private String faultySensor;
    private ConcurrentHashMap<String, StampedDetectedObjects> lastCamerasFrame;
    private ConcurrentHashMap<String, List<TrackedObject>> lastLiDarWorkerTrackersFrame;
    private List<Pose> poses;
    private StatisticalFolder statistics;


    private Error() {
        error = "";
        faultySensor = "";
        lastCamerasFrame = new ConcurrentHashMap<>();
        lastLiDarWorkerTrackersFrame = new ConcurrentHashMap<>();
        poses = new ArrayList<>();
        statistics = StatisticalFolder.getInstance();
    }

    public static Error getInstance() {
        return Error.ErrorHolder.instance;
    }

    public String getError() {
        return error;
    }

    public void setError(String errorString) {
        this.error = errorString;
    }

    public String getFaultySensor() {
        return faultySensor;
    }

    public void setFaultySensor(String faultySensor) {
        this.faultySensor = faultySensor;
    }

    public ConcurrentHashMap<String, StampedDetectedObjects> getLastCamerasFrame() {
        return lastCamerasFrame;
    }

    public void updateLastCamerasFrame(String s, StampedDetectedObjects stampedDetectedObjects) {
        if(!lastCamerasFrame.containsKey(s)) {
            lastCamerasFrame.put(s, stampedDetectedObjects);
        }
        else{
            lastCamerasFrame.remove(s);
            lastCamerasFrame.put(s, stampedDetectedObjects);
        }
    }

    public ConcurrentHashMap<String, List<TrackedObject>> getLastLiDarWorkerTrackersFrame() {
        return lastLiDarWorkerTrackersFrame;
    }

    public void updateLastLiDarWorkersFrame(String s, List<TrackedObject> list) {
        if(!lastLiDarWorkerTrackersFrame.containsKey(s)) {
            lastLiDarWorkerTrackersFrame.put(s, list);
        }
        else{
            lastLiDarWorkerTrackersFrame.remove(s);
            lastLiDarWorkerTrackersFrame.put(s, list);
        }
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public void addPose(Pose pose) {
        poses.add(pose);
    }

}
