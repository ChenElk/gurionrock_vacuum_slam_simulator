package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.Error;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Output {
    private final StatisticalFolder SFolderInstance = StatisticalFolder.getInstance();
    private final Error errorInstance = Error.getInstance();
    public static String outputDirectoryPath;

    public void outputForTermination() {
        // Initialize Gson
        Gson gson = new Gson();

        // Collect data for statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("systemRuntime", SFolderInstance.getSystemRuntime());
        stats.put("numDetectedObjects", SFolderInstance.getNumDetectedObjects());
        stats.put("numTrackedObjects", SFolderInstance.getNumTrackedObjects());
        stats.put("numLandmarks", SFolderInstance.getNumLandmarks());

        // Collect data for landmarks
        Map<String, Object> landMarks = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order
        for (LandMark landmark : SFolderInstance.getLandmarks()) {
            Map<String, Object> landmarkDetails = new LinkedHashMap<>(); // Ensure the order of fields
            landmarkDetails.put("id", landmark.getId());
            landmarkDetails.put("description", landmark.getDescription());
            landmarkDetails.put("coordinates", landmark.getCoordinates());
            landMarks.put(landmark.getId(), landmarkDetails);
        }

        // Write the JSON to the file manually
        String output= outputDirectoryPath+ "/output_file.json";
        try (FileWriter writer = new FileWriter(output)) {
            // Write the statistics in one line
            writer.write("{");
            writer.write("\"systemRuntime\":" + stats.get("systemRuntime") + ",");
            writer.write("\"numDetectedObjects\":" + stats.get("numDetectedObjects") + ",");
            writer.write("\"numTrackedObjects\":" + stats.get("numTrackedObjects") + ",");
            writer.write("\"numLandmarks\":" + stats.get("numLandmarks") + ",\n");

            // Write landMarks label on a new line
            writer.write("\"landMarks\": {\n");

            // Write each landmark on a new line
            int landmarkCount = landMarks.size();
            int currentIndex = 0;
            for (Map.Entry<String, Object> entry : landMarks.entrySet()) {
                writer.write("    \"" + entry.getKey() + "\": ");
                writer.write(gson.toJson(entry.getValue())); // Serialize the landmark object
                currentIndex++;
                if (currentIndex < landmarkCount) {
                    writer.write(",\n"); // Add a comma if it's not the last landmark
                } else {
                    writer.write("\n"); // No comma for the last landmark
                }
            }

            // Close the JSON properly
            writer.write("}\n");
            writer.write("}");
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }
    }

    public void outputForCrashing() {
        // Initialize Gson with pretty printing
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Create the main output JSON object
        JsonObject output = new JsonObject();

        try {
            // Add error and faulty sensor
            output.addProperty("error", errorInstance.getError());
            output.addProperty("faultySensor", errorInstance.getFaultySensor());

            // Add last cameras frame
            JsonObject camerasFrame = new JsonObject();
            for (Map.Entry<String, StampedDetectedObjects> entry : errorInstance.getLastCamerasFrame().entrySet()) {
                JsonObject cameraData = new JsonObject();
                cameraData.addProperty("time", entry.getValue().getTime());
                JsonArray detectedObjectsArray = gson.toJsonTree(entry.getValue().getDetectedObjects()).getAsJsonArray();
                cameraData.add("detectedObjects", detectedObjectsArray);
                camerasFrame.add(entry.getKey(), cameraData);
            }
            output.add("lastCamerasFrame", camerasFrame);

            // Add last LiDAR worker trackers frame
            JsonObject lidarFrame = new JsonObject();
            for (Map.Entry<String, List<TrackedObject>> entry : errorInstance.getLastLiDarWorkerTrackersFrame().entrySet()) {
                JsonArray trackedObjectsArray = gson.toJsonTree(entry.getValue()).getAsJsonArray();
                lidarFrame.add(entry.getKey(), trackedObjectsArray);
            }
            output.add("lastLidarFrames", lidarFrame);

            // Add poses without yaw
            JsonArray posesArray = new JsonArray();
            for (Pose pose : errorInstance.getPoses()) {
                JsonObject poseObject = new JsonObject();
                poseObject.addProperty("time", pose.getTime());
                poseObject.addProperty("x", pose.getX());
                poseObject.addProperty("y", pose.getY());
                posesArray.add(poseObject);
            }
            output.add("poses", posesArray);

            // Add statistics and landmarks
            JsonObject stats = new JsonObject();
            stats.addProperty("systemRuntime", SFolderInstance.getSystemRuntime());
            stats.addProperty("numDetectedObjects", SFolderInstance.getNumDetectedObjects());
            stats.addProperty("numTrackedObjects", SFolderInstance.getNumTrackedObjects());
            stats.addProperty("numLandmarks", SFolderInstance.getNumLandmarks());

            // Transform landmarks to match the format and add
            JsonArray landmarks = gson.toJsonTree(SFolderInstance.getLandmarks()).getAsJsonArray();
            JsonObject transformedLandmarks = transformLandmarks(landmarks);
            stats.add("landMarks", transformedLandmarks);

            output.add("statistics", stats);

            // Write the formatted output to a file
            writeFormattedOutputToFile(gson, output);

        } catch (Exception e) {
            System.err.println("Error generating crash output: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Transforms landmarks array into a JSON object keyed by their IDs.
     */
    private JsonObject transformLandmarks(JsonArray landmarksArray) {
        JsonObject transformedLandmarks = new JsonObject();
        for (int i = 0; i < landmarksArray.size(); i++) {
            JsonObject landmark = landmarksArray.get(i).getAsJsonObject();
            String id = landmark.get("id").getAsString();
            transformedLandmarks.add(id, landmark);
        }
        return transformedLandmarks;
    }

    /**
     * Writes the JSON output to a file with proper indentation and line breaks.
     */
    private void writeFormattedOutputToFile(Gson gson, JsonObject output) {
        String outputDir= outputDirectoryPath+ "/OutputError.json";
        try (FileWriter writer = new FileWriter(outputDir)) {
            // Write the pretty-printed JSON directly to the file
            writer.write(gson.toJson(output));
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
        }
    }
}

