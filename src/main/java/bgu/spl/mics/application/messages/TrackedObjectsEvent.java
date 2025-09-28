package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;

public class TrackedObjectsEvent implements Event<Boolean> {
    int time;
    int lidarFrequency;
    List<TrackedObject> trackedObjects;

    public TrackedObjectsEvent(int time, int lidarFrequency, List<TrackedObject> trackedObjects) {
        this.time = time;
        this.lidarFrequency = lidarFrequency;
        this.trackedObjects = trackedObjects;
    }

    public List<TrackedObject> getDetectedObjects() {
        return trackedObjects;
    }

    public int getTime() {
        return time;
    }

    public int getLidarFrequency() {
        return lidarFrequency;
    }

}
