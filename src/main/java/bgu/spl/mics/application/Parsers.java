package bgu.spl.mics.application;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import bgu.spl.mics.application.objects.*;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class Parsers {

    private final Gson gson;

    public Parsers(Gson gson) {
        this.gson = gson;
    }

    public Configuration parseConfiguration(String configFilePath) throws IOException, JsonSyntaxException {
        try (FileReader reader = new FileReader(configFilePath)) {
            Configuration config = gson.fromJson(reader, Configuration.class);
            return config;
        }
    }

    /**
     * Parses camera data JSON and returns a list of StampedDetectedObjects.
     *
     * @param filePath Path to the camera data JSON file.
     * @param cameraKey The key for the specific camera's data.
     * @return A list of StampedDetectedObjects for the specified camera.
     */
    public List<StampedDetectedObjects> camerasParser(String filePath, String cameraKey) {
        try {
            JsonObject jsonObject = gson.fromJson(new FileReader(filePath), JsonObject.class);
            JsonElement cameraData = jsonObject.get(cameraKey);

            if (cameraData == null) {
                throw new RuntimeException("Camera key not found: " + cameraKey);
            }


            // Convert the JSON array for the specific camera to a list of StampedDetectedObjects
            List<StampedDetectedObjects> parsedData = gson.fromJson(cameraData, new TypeToken<List<StampedDetectedObjects>>() {}.getType());
            return parsedData;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse camera data from " + filePath, e);
        }
    }

    /**
     * Parses LiDAR data JSON and returns an instance of LiDarDataBase.
     *
     * @param filePath Path to the LiDAR data JSON file.
     * @return An instance of LiDarDataBase populated with the parsed data.
     */
    public LiDarDataBase lidarsParser(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {

            // Define the type for the list of StampedCloudPoints
            Type lidarDataType = new TypeToken<List<StampedCloudPoints>>() {}.getType();

            // Parse the JSON file into a list of StampedCloudPoints
            List<StampedCloudPoints> cloudPoints = gson.fromJson(reader, lidarDataType);

            if (cloudPoints == null || cloudPoints.isEmpty()) {
                throw new RuntimeException("LiDAR data is empty or invalid in file: " + filePath);
            }


            // Create and populate the LiDarDataBase instance
            LiDarDataBase lidarDataBase = LiDarDataBase.getInstance(filePath);

            return lidarDataBase;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse LiDAR data from " + filePath, e);
        }
    }

    public List<Pose> posesParser(String posePath) {
        try {
            List<Pose> poses = gson.fromJson(new FileReader(posePath), new TypeToken<List<Pose>>() {}.getType());
            return poses;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse pose data", e);
        }
    }
}
