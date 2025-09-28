import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.services.CameraService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MessageBusImpl class using existing microservices and events.
 */
public class MessageBusTest {

    private MessageBusImpl messageBus;
    private CameraService cameraService;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance(); // Singleton instance
        messageBus.clearInstance();
        cameraService = new CameraService(new Camera(1, 2)); // Existing CameraService
    }

    @AfterAll
    static void tearDownAll() {
        MessageBusImpl.getInstance().clearInstance();
    }

    /**
     * Tests the registration of a microservice and sending an event.
     * Verifies:
     * 1. A registered microservice can receive a specific event type.
     * 2. A Future object is returned when an event is sent.
     * 3. The registered microservice correctly processes the received event.
     */
    @Test
    void testRegisterAndSendEvent() {
        messageBus.register(cameraService);
        messageBus.subscribeEvent(PoseEvent.class, cameraService);

        PoseEvent poseEvent = new PoseEvent(null);
        Future<Boolean> future = messageBus.sendEvent(poseEvent);

        assertNotNull(future, "Future should not be null when an event is sent.");

        try {
            Message receivedMessage = messageBus.awaitMessage(cameraService);
            assertEquals(poseEvent, receivedMessage, "CameraService should receive the PoseEvent.");
        } catch (InterruptedException e) {
            fail("InterruptedException occurred while waiting for the message.");
        }
    }

    /**
     * Tests broadcasting messages to subscribed microservices.
     * Verifies:
     * 1. A registered microservice receives a broadcast message of a specific type.
     * 2. The broadcast is processed without errors by all intended subscribers.
     */
    @Test
    void testSendBroadcast() {
        messageBus.register(cameraService);
        messageBus.subscribeBroadcast(TerminatedBroadcast.class, cameraService);

        TerminatedBroadcast terminatedBroadcast = new TerminatedBroadcast(cameraService, "Test Termination");
        messageBus.sendBroadcast(terminatedBroadcast);

        try {
            Message receivedMessage = messageBus.awaitMessage(cameraService);
            assertEquals(terminatedBroadcast, receivedMessage, "CameraService should receive the TerminatedBroadcast.");
        } catch (InterruptedException e) {
            fail("InterruptedException occurred while waiting for the broadcast.");
        }
    }

    /**
     * Tests unregistering a microservice.
     * Verifies:
     * 1. Unregistered microservices cannot receive messages.
     * 2. Attempting to send an event to an unregistered service results in a null Future.
     * 3. Awaiting messages on an unregistered service throws an exception.
     */
    @Test
    void testUnregisterMicroService() {
        messageBus.register(cameraService);
        messageBus.unregister(cameraService);

        PoseEvent poseEvent = new PoseEvent(null);
        messageBus.subscribeEvent(PoseEvent.class, cameraService);

        Future<Boolean> future = messageBus.sendEvent(poseEvent);
        assertNull(future, "Future should be null when sending an event to an unregistered service.");

        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(cameraService),
                "Awaiting a message for an unregistered service should throw an exception.");
    }
}
