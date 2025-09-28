package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private final List<LandMark> landmarks;
    private final List<Pose> poses;
    private List<List<TrackedObject>> trackedObjects;
    private final StatisticalFolder SFolderInstance= StatisticalFolder.getInstance();

    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();
    }

    private FusionSlam() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        trackedObjects = new ArrayList<List<TrackedObject>>();
    }
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    /**
     * PRE: None.
     * POST: The lists of landmarks, poses, and tracked objects are cleared.
     */
    public void clearInstance() {
        landmarks.clear();
        poses.clear();
        trackedObjects.clear();
    }

    public LandMark convertLandmarkToGlobal(TrackedObject trackedObject, Pose currentPose) {
        LandMark landmark = getLandmarkById(trackedObject.getId());
        if (landmark != null) {
            return new LandMark(trackedObject.getId(), trackedObject.getDescription(), convertCoordinates(trackedObject, currentPose));
        }
        return null;
    }

    /**
     * PRE: TrackedObject and Pose cannot be null. The trackedObject must have a list of coordinates.
     * POST: Returns a list of CloudPoints representing the global coordinates of the tracked object.
     */
    public List<CloudPoint> convertCoordinates(TrackedObject trackedObject, Pose currentPose) {
        if (trackedObject == null || currentPose == null) {
            throw new IllegalArgumentException("TrackedObject and Pose cannot be null.");
        }

        List<CloudPoint> globalCloudPoints = new ArrayList<>();
        double YawInRadians = Math.toRadians(currentPose.getYaw());

        for (CloudPoint localPoint : trackedObject.getCoordinates()) {
            double xLocal = localPoint.getX();
            double yLocal = localPoint.getY();

            double xGlobal = currentPose.getX() + (Math.cos(YawInRadians) * xLocal - Math.sin(YawInRadians) * yLocal);
            double yGlobal = currentPose.getY() + (Math.sin(YawInRadians) * xLocal + Math.cos(YawInRadians) * yLocal);

            CloudPoint globalPoint = new CloudPoint(xGlobal, yGlobal);
            globalCloudPoints.add(globalPoint);

        }
    return globalCloudPoints;
    }

    /**
     * PRE: Both input lists should not be null. Each list must contain valid CloudPoint objects.
     * POST: Returns a new list with the averaged coordinates, or null if input lists are null.
     */

    public List<CloudPoint> averageCoordinates(List<CloudPoint> existing, List<CloudPoint> newPoints) {
        if(newPoints!= null){
            List<CloudPoint> averaged = new ArrayList<>();
            int size = Math.min(existing.size(), newPoints.size());

            for (int i = 0; i < size; i++) {
                double avgX = (existing.get(i).getX() + newPoints.get(i).getX()) / 2.0;
                double avgY = (existing.get(i).getY() + newPoints.get(i).getY()) / 2.0;
                averaged.add(new CloudPoint(avgX, avgY));
            }

            // Handle cases where the lists have different sizes
            if (existing.size() > size) {
                averaged.addAll(existing.subList(size, existing.size()));
            } else if (newPoints.size() > size) {
                averaged.addAll(newPoints.subList(size, newPoints.size()));
            }

            return averaged;
        }
        return null;
    }

    public void addLandMarkLogic(List<TrackedObject> trackedObjects, Pose currPose){

        for (TrackedObject trackedObject : trackedObjects) {
            LandMark landMarkToAdd = getLandmarkById(trackedObject.getId());

            if (landMarkToAdd != null) { //the landMark exits, and we need to average the coordinates
                //average the coordinates with the new ones
                List<CloudPoint> existingCoordinates = landMarkToAdd.getCoordinates();
                List<CloudPoint> newCoordinates = convertCoordinates(trackedObject, currPose);

                List<CloudPoint> averagedCoordinates = averageCoordinates(existingCoordinates, newCoordinates); //new method that average each coordinate
                landMarkToAdd.getCoordinates().clear(); // clear the old coordinates
                landMarkToAdd.getCoordinates().addAll(averagedCoordinates); // add the averaged coordinates

            }
            else { //the landMark doesn't exit, and we need to add a new landmark to the global map
                List<CloudPoint> globalCoordinates = convertCoordinates(trackedObject, currPose);
                LandMark newLandmark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), globalCoordinates);
                addLandmark(newLandmark);
                SFolderInstance.addLandMark(newLandmark);
                SFolderInstance.incrementLandmarks();
            }
        }

    }

    /**
     * PRE: LandMark cannot be null.
     * POST: The LandMark is added to the list of landmarks.
     */

    public void addLandmark(LandMark landMarkToAdd) {landmarks.add(landMarkToAdd);}

    /**
     * PRE: The ID cannot be null.
     * POST: Returns the LandMark object associated with the ID, or null if no such LandMark exists.
     */

    public LandMark getLandmarkById(String id) {
        for (LandMark landmark : landmarks) {
            if (landmark.getId().equals(id)) {
                return landmark;
            }
        }
        return null;
    }

    public List<Pose> getPoses() {
        return new ArrayList<>(poses);
    }

    /**
     * PRE: Pose cannot be null.
     * POST: The Pose is added to the list of poses.
     */

    public void addPose(Pose pose) {
        poses.add(pose);
    }

    /**
     * PRE: Tick must be greater than zero and within the range of stored poses.
     * POST: Returns the Pose for the given tick, or null if no such Pose exists.
     */

    public Pose getPoseForTick(int tick) {
        int index = tick - 1;
        if (index >= 0 && index < poses.size()) {
            return poses.get(index);
        }
        return null;
    }

    public void addList(List<TrackedObject> list){
        trackedObjects.add(list);
    }

    public List<List<TrackedObject>> getTrackedObjects() {
        return trackedObjects;
    }

}
