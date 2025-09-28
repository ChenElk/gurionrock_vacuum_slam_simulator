import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FusionSlamTest {

    private FusionSlam fusionSlam;
    private TrackedObject trackedObject;
    private Pose pose;

    @BeforeEach
    void setUp() {
        fusionSlam = FusionSlam.getInstance();
        // Initialize trackedObject and pose before test
        ArrayList<CloudPoint> coordinates = new ArrayList<>();
        coordinates.add(new CloudPoint(1.23456, 9.87654));
        trackedObject = new TrackedObject("test", 1, "unitTest", coordinates);
        pose = new Pose( 2, 3, 4, 1);
    }

    @AfterEach
    void tearDown() {
        fusionSlam.clearInstance();
    }


    /**
     * Verifies that the convertCoordinates method correctly calculates global coordinates
     * from local coordinates given a specific robot pose.
     * Test case:
     * - Uses a pose at (10, 20) with no rotation.
     * - Local coordinates: [(5, 5), (-3, 2)].
     * - Expected global coordinates: [(15, 25), (7, 22)].
     */
    @Test
    void testConvertCoordinates_CalculatesCorrectGlobalPoints() {
        Pose pose = new Pose(10, 20, 0, 0); // Pose at (10, 20)
        List<CloudPoint> localPoints = new ArrayList<>();
        localPoints.add(new CloudPoint(5, 5));
        localPoints.add(new CloudPoint(-3, 2));
        TrackedObject trackedObject = new TrackedObject("test", 1, "desc", localPoints);

        List<CloudPoint> globalPoints = fusionSlam.convertCoordinates(trackedObject, pose);

        assertEquals(15, globalPoints.get(0).getX(), 0.0001);
        assertEquals(25, globalPoints.get(0).getY(), 0.0001);
        assertEquals(7, globalPoints.get(1).getX(), 0.0001);
        assertEquals(22, globalPoints.get(1).getY(), 0.0001);
    }

    /**
     * Verifies the behavior of the convertCoordinates method when the tracked object
     * has an empty list of coordinates.
     * Test case:
     * - Tracked object starts with no coordinates.
     * - Ensures the method handles this case without errors and leaves the list empty.
     */
    @Test
    void testTrackedObjectToGlobal_WithEmptyCoordinates() {
        trackedObject.setCoordinates(new ArrayList<>());
        fusionSlam.convertCoordinates(trackedObject, pose);
        assertTrue(trackedObject.getCoordinates().isEmpty(), "Coordinates should remain empty when no points exist.");
    }

}