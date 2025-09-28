package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private int id;
    private int frequency;
    private STATUS status;
    private List<StampedDetectedObjects> detectedObjectsList;
    private List<StampedDetectedObjects> detectedObjectsToSend;

    public Camera(int id, int frequency){
        this.id= id;
        this.frequency= frequency;
        this.status = STATUS.UP;
        detectedObjectsList= new ArrayList<>();
        detectedObjectsToSend= new ArrayList<>();
    }

    public int getId(){
        return id;
    }

    public int getFrequency(){
        return frequency;
    }

    public STATUS getStatus(){
        return status;
    }

    public void setStatus(STATUS status){
        this.status = status;
    }

    /**
     * PRE: The `DetectedObject` must not be null.
     * POST: The `DetectedObject` is added to the `detectedObjectsList`.
     *
     * @param DetectedObject The object to be added to the detected objects list.
     */
    public void addDetectedObject(StampedDetectedObjects DetectedObject) {
        this.detectedObjectsList.add(DetectedObject);
    }

    //returns the stamped object with the objects exists in this time
    public StampedDetectedObjects getDetectObjectsByTime(int time){
        for (StampedDetectedObjects stampedObject : detectedObjectsList) {
            if (stampedObject.getTime() == time) {
                return stampedObject;
            }
        }
        return null;
    }

    //check for error object (if exists) in the current tick, and return its description
    public String checkIfErrorExists(int tick){

        if (detectedObjectsList == null || detectedObjectsList.isEmpty()) {
            return null;
        }

        for (StampedDetectedObjects stamped : detectedObjectsList) {
            if (stamped.getTime() == tick) {
                for (DetectedObject obj : stamped.getDetectedObjects()) {
                    if (obj.getId().equals("ERROR")){
                        return obj.getDescription();
                    }
                }
                //update the error singleton
                Error.getInstance().updateLastCamerasFrame("Camera" + getId(), stamped);
            }
        }
        return null; //return null if no error object exists
    }

    //check if there are left objects to return at the current tick
    public boolean checkIfLeft(int tick) {
        if (!detectedObjectsList.isEmpty()) {
            StampedDetectedObjects lastObjects = detectedObjectsList.get(detectedObjectsList.size() - 1);
            return lastObjects.getTime() + getFrequency() >= tick;
        }
        return false;
    }

    /**
     * PRE: `stamp` must not be null, and its `detectedObjects` list must not be null or empty.
     * POST: A new `StampedDetectedObjects` object with updated time (current time + frequency) is added
     *       to the `detectedObjectsToSend` list.
     *
     * @param stamp The stamped detected objects to add to the list for sending.
     */
    public void addObjectsToSend(StampedDetectedObjects stamp) {
        if(stamp!=null &&  stamp.getDetectedObjects() != null && !stamp.getDetectedObjects().isEmpty())
            detectedObjectsToSend.add(new StampedDetectedObjects(stamp.getTime()+getFrequency(), stamp.getDetectedObjects()));
    }

    /**
     * PRE: `time` must be a non-negative integer.
     * POST: Returns the stamped detected objects with a time matching the specified value, or null if no match is found.
     *
     * @param time The time to search for in the `detectedObjectsToSend` list.
     * @return A `StampedDetectedObjects` object or null if no match is found.
     */
    public StampedDetectedObjects getStampedToSend(int time) {
        for (StampedDetectedObjects stampedObject : detectedObjectsToSend) {
            if (stampedObject.getTime() == time) {
                return stampedObject;
            }
        }
        return null;
    }

    /**
     * PRE: None.
     * POST: Resets the `Camera` object to its default state:
     *       - Status is set to UP.
     *       - `detectedObjectsList` and `detectedObjectsToSend` lists are cleared.
     */
    public void clearInstance() {
        this.status = STATUS.UP;
        if (detectedObjectsList != null) {
            this.detectedObjectsList.clear();
        }
        if (detectedObjectsToSend != null) {
            this.detectedObjectsToSend.clear();
        }
    }
}
