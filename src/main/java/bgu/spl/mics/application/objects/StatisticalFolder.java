package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private final AtomicInteger systemRuntime;
    private final AtomicInteger numDetectedObjects;
    private final AtomicInteger numTrackedObjects;
    private final AtomicInteger numLandmarks;
    private List<LandMark> landMarks;

    // Singleton instance
    private static final StatisticalFolder instance = new StatisticalFolder();

    // Private constructor
    private StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
        this.landMarks = new ArrayList<>();
    }

    // Get singleton instance
    public static StatisticalFolder getInstance() {
        return instance;
    }

    // Increment methods
    public void incrementSystemRuntime() {
        systemRuntime.incrementAndGet();
    }

    public void incrementDetectedObjects() {
        numDetectedObjects.incrementAndGet();
    }

    public void incrementTrackedObjects() {
        numTrackedObjects.incrementAndGet();
    }

    public void incrementLandmarks() {
        numLandmarks.incrementAndGet();
    }

    // Get methods
    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getNumLandmarks() {
        return numLandmarks.get();
    }

    public void addLandMark(LandMark landMark) {
        landMarks.add(landMark);
    }

    public List<LandMark> getLandmarks() {
        return landMarks;
    }
}
