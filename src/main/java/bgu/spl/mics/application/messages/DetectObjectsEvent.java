package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvent implements Event<Boolean> {
    private int detectionTime;
    private StampedDetectedObjects stampDetectedObjects;

    public DetectObjectsEvent(StampedDetectedObjects obj){
        this.detectionTime = obj.getTime();
        this.stampDetectedObjects= obj;
    }

    public int getDetectionTime(){
        return detectionTime;
    }

    public StampedDetectedObjects getStampDetectedObjects(){
        return stampDetectedObjects;
    }
}
