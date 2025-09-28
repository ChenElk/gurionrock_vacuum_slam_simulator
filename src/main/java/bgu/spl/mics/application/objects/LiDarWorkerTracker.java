package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private final int id;
    private final int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;
    private final LiDarDataBase dataBaseInstance;

    public LiDarWorkerTracker(int id, int frequency, LiDarDataBase dataBaseInstance){
        this.id=id;
        this.frequency=frequency;
        this.status=STATUS.UP;
        this.lastTrackedObjects= new ArrayList<>();
        this.dataBaseInstance= dataBaseInstance;

    }

    public void setStatus(STATUS status){
        this.status = status;
    }

    public static List<CloudPoint> convertToCloudPoints(List<List<Double>> pointsList) {
        List<CloudPoint> cloudPoints = new ArrayList<>();
        for (List<Double> point : pointsList) {
            double x = point.get(0);
            double y = point.get(1);
            cloudPoints.add(new CloudPoint(x, y));
        }
        return cloudPoints;
    }

    //this method received StampedDetectedObject and create a list of TrackedObjects
    public List<TrackedObject> createTrackedObjects(StampedDetectedObjects stampedDetected) {
        List<TrackedObject> result = new ArrayList<>();
        int time=stampedDetected.getTime();
        //iterate through the detected objects list in the StampedDetected object
        for (DetectedObject detObj : stampedDetected.getDetectedObjects()) {

            //for each detectedObject, search in the database for the matching cloudpoint according to the time
            for (StampedCloudPoints sCloudPoint : dataBaseInstance.getCloudPoints()) {
                //if the time and the id matches
                if (time == sCloudPoint.getTime()){
                    if (detObj.getId().equals(sCloudPoint.getId()) && !sCloudPoint.getId().equals("ERROR")) {
                        List<CloudPoint> convertedCloudPoints= convertToCloudPoints(sCloudPoint.getCoordinates());
                        TrackedObject trackO = new TrackedObject(detObj.getId(), time, detObj.getDescription(),convertedCloudPoints);

                        if(!result.contains(trackO)){
                            result.add(trackO);
                            lastTrackedObjects.add(trackO);
                        }
                    } else if (sCloudPoint.getId().equals("ERROR"))
                        return null;
                }

                else if(detObj.getId().equals("ERROR"))
                    return null;
            }
        }
        return result;
    }

    public boolean foundError(int time) {
        for (StampedCloudPoints stampedCloudPoints : dataBaseInstance.getCloudPoints()) {
            if (stampedCloudPoints.getTime() == time) {
                if (stampedCloudPoints.getId().equals("ERROR")) {
                    return true;
                }
            }
        }
        return false;
    }
    public List<StampedCloudPoints> getCloudPoints() {
        return dataBaseInstance.getCloudPoints();
    }

    public int getFrequency(){
        return frequency;
    }

    public int getId(){
        return id;
    }

}
