package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private String id;
    private int time;
    private String description;
    List<CloudPoint> coordinates;


    public TrackedObject(String id, int time, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    public String getDescription() {
        return description;
    }

    public int getTime() {
        return time;
    }

    public void setCoordinates(List<CloudPoint> list) {
        this.coordinates= list;
    }
}
