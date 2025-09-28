package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//this is a singleton class that represents how much activate services there are from each type
//the types are cameras, lidars and pose service
public class ServicesCounter {
    private final AtomicInteger cameraServices;
    private final AtomicInteger lidarServices;
    private final AtomicInteger allServices; //include pose
    private final AtomicBoolean isFusionSlamService;

    //singleton instance
    private static final ServicesCounter instance = new ServicesCounter();

    private ServicesCounter() {
        cameraServices = new AtomicInteger(0);
        lidarServices = new AtomicInteger(0);
        allServices = new AtomicInteger(0);
        isFusionSlamService= new AtomicBoolean(false);
    }

    public static ServicesCounter getInstance() {
        return instance;
    }

    public void incrementCameraServices() {
        cameraServices.incrementAndGet();
        allServices.incrementAndGet();
    }

    public void incrementLidarServices() {
        lidarServices.incrementAndGet();
        allServices.incrementAndGet();
    }

    public void incrementAllServices() {
        allServices.incrementAndGet();
    }

    public void decrementCameraServices() {
        cameraServices.decrementAndGet();
        allServices.decrementAndGet();
    }

    public void decrementLidarServices() {
        lidarServices.decrementAndGet();
        allServices.decrementAndGet();
    }

    public void decrementPoseServices() {
        allServices.decrementAndGet();
    }

    public int getCameraServices() {
        return cameraServices.get();
    }

    public int getLidarServices() {
        return lidarServices.get();
    }

    public int getAllServices() {
        return allServices.get();
    }

    public void setCameraServices(int num) {
        this.cameraServices.set(num);
    }

    public void setLidarServices(int num) {
        this.lidarServices.set(num);
    }

    public void zeroAllServices() {

        this.allServices.set(0);
        this.cameraServices.set(0);
        this.lidarServices.set(0);
    }

    public void offFusion(){
        this.isFusionSlamService.set(false);
    }

    public void onFusion(){
        this.isFusionSlamService.set(true);
    }

    public boolean isFusionActivate(){
        return isFusionSlamService.get();
    }

    public boolean areServicesFinished(){
        return (getAllServices()==0);
    }


}
