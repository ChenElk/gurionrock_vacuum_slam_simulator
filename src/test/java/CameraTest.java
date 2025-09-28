import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.LiDarService;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {
    private Camera camera;


    @BeforeEach
    void setUp() {
        camera = new Camera(1, 3);
        List<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();
        List<StampedDetectedObjects> detectedObjectsToSend = new ArrayList<>();

    }

    @AfterEach
    void tearDown() {
        camera.clearInstance();
    }


    /**
     * PRE:
     * - A stamped object must have been added to the camera's list of objects to send.
     * - The requested timestamp must match or exceed the time of the stamped object.
     * POST:
     * - The correct stamped object is retrieved based on the given time.
     * - The retrieved stamped object is not null.
     * - The number of detected objects and their IDs match the expected values.
     */
    @Test
    void testGetStampedToSend() {
        List<DetectedObject> objects = new ArrayList<>();
        objects.add(new DetectedObject("Object1", "Description1"));
        objects.add(new DetectedObject("Object2", "Description2"));

        StampedDetectedObjects stampedObject = new StampedDetectedObjects(5, objects);
        camera.addObjectsToSend(stampedObject);

        // Verify that the correct data is transferred at the appropriate time
        StampedDetectedObjects result = camera.getStampedToSend(8);
        assertNotNull(result, "The retrieved stamped object should not be null.");
        assertEquals(8, result.getTime(), "The time of the stamped object should match the request.");
        assertEquals(2, result.getDetectedObjects().size(), "The number of detected objects should match.");
        assertEquals("Object1", result.getDetectedObjects().get(0).getId(), "The first object's ID should match.");
        assertEquals("Object2", result.getDetectedObjects().get(1).getId(), "The second object's ID should match.");
    }

    /**
     * PRE:
     * - A stamped object with detected objects must have been added to the camera's list of objects to send.
     * - The requested timestamp must match or exceed the time of the stamped object.
     * POST:
     * - The transferred stamped object matches the given timestamp.
     * - The retrieved detected objects contain the correct IDs and descriptions.
     * - The number of detected objects matches the expected count.
     */
    @Test
    void testDataTransferToService() {
        List<DetectedObject> detectedObjects = new ArrayList<>();
        detectedObjects.add(new DetectedObject("Object1", "Detected at position A"));
        detectedObjects.add(new DetectedObject("Object2", "Detected at position B"));

        StampedDetectedObjects initialStamp = new StampedDetectedObjects(4, detectedObjects);

        camera.addObjectsToSend(initialStamp);

        StampedDetectedObjects transferredStamp = camera.getStampedToSend(7);

        assertNotNull(transferredStamp);
        assertEquals(7, transferredStamp.getTime());
        assertEquals(2, transferredStamp.getDetectedObjects().size());
        assertEquals("Object1", transferredStamp.getDetectedObjects().get(0).getId());
        assertEquals("Object2", transferredStamp.getDetectedObjects().get(1).getId());
        assertEquals("Detected at position A", transferredStamp.getDetectedObjects().get(0).getDescription());
        assertEquals("Detected at position B", transferredStamp.getDetectedObjects().get(1).getDescription());
    }
}