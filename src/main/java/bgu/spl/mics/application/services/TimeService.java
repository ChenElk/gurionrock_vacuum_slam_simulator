package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ServicesCounter;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private final int tickTime; //speed in seconds
    private final int duration;
    private final AtomicInteger countTick; //counts the ticks since the system initialization
    private final StatisticalFolder SFolderInstance= StatisticalFolder.getInstance();
    private final ServicesCounter counterInstance = ServicesCounter.getInstance();

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.tickTime= TickTime;
        this.duration = Duration;
        this.countTick = new AtomicInteger(0);
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        try{
            while(countTick.get()< duration && counterInstance.getAllServices()>0 ) {
                countTick.incrementAndGet();
                SFolderInstance.incrementSystemRuntime();
                sendBroadcast(new TickBroadcast(countTick.get()));
                Thread.sleep(tickTime*1000);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finally {
            sendBroadcast(new TerminatedBroadcast(this, "TimeService"));
            terminate();

        }

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
            //the only time a time service should terminate is when fusion slam finished its work
            if(terminate.getServiceClass() == FusionSlamService.class) {
                terminate();
            }
        });

        //subscribe to crashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            terminate();
        });

    }
}
