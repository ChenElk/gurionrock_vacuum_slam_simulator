package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private String id;
    private String description;
    List<CloudPoint> coordinates;

    //constructor
    public LandMark(String id, String description, List<CloudPoint> globalCoordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = globalCoordinates;
    }


    public String getId(){
        return id;
    }

    public String getDescription(){
        return description;
    }

    public void addCoordinates(CloudPoint coordinates) {
        this.coordinates.add(coordinates);
    }

    public List<CloudPoint> getCoordinates(){
        return coordinates;
    }
}
