package bgu.spl.mics.application;

import java.util.List;

public class Configuration {
    private CamerasConfig Cameras;
    private LiDarWorkersConfig LiDarWorkers;
    private List<Pose> poses;       // Holds the list of poses
    private String poseJsonFile;      // Path to the poses JSON file
    private int TickTime;
    private int Duration;

    public int getNumOfLidars(){
        return LiDarWorkers.getNumOfLidars();
    }

    public int getNumOfCameras(){
        return Cameras.getNumOfCameras();
    }


    // Inner classes for Cameras and LiDAR
    public static class CamerasConfig {
        private List<CameraConfig> CamerasConfigurations;
        private String camera_datas_path;

        public List<CameraConfig> getCamerasConfigurations() {
            return CamerasConfigurations;
        }

        public String getCameraDatasPath() {
            return camera_datas_path;
        }

        public int getNumOfCameras() {
            return CamerasConfigurations.size();
        }
    }


    public static class CameraConfig {
        private int id;
        private int frequency;
        private String camera_key;

        public int getId() {
            return id;
        }

        public int getFrequency() {
            return frequency;
        }

        public String getCameraKey() {
            return camera_key;
        }
    }


    public static class LiDarWorkersConfig {
        private List<LidarConfig> LidarConfigurations;
        private String lidars_data_path;

        public List<LidarConfig> getLidarConfigurations() {
            return LidarConfigurations;
        }

        public String getLidarsDataPath() {
            return lidars_data_path;
        }

        public int getNumOfLidars(){
            return LidarConfigurations.size();
        }
    }

    public static class LidarConfig {
        private int id;
        private int frequency;

        public int getId() {
            return id;
        }

        public int getFrequency() {
            return frequency;
        }

    }

    // Pose class to represent individual pose data
    public static class Pose {
        private int time;
        private double x;
        private double y;
        private double yaw;

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getYaw() {
            return yaw;
        }

        public void setYaw(double yaw) {
            this.yaw = yaw;
        }

        @Override
        public String toString() {
            return "Pose{" +
                    "time=" + time +
                    ", x=" + x +
                    ", y=" + y +
                    ", yaw=" + yaw +
                    '}';
        }
    }

    // Getters for Configuration fields
    public CamerasConfig getCameras() {
        return Cameras;
    }

    public LiDarWorkersConfig getLiDarWorkers() {
        return LiDarWorkers;
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public String getPosesPath() {
        return poseJsonFile;
    }

    public int getTickTime() {
        return TickTime;
    }

    public int getDuration() {
        return Duration;
    }
}
