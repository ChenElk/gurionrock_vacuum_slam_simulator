//package bgu.spl.mics.application;
//
//import bgu.spl.mics.application.objects.DetectedObject;
//import bgu.spl.mics.application.objects.StampedDetectedObjects;
//import bgu.spl.mics.application.objects.Pose;
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * Readers class is responsible for reading and parsing data from JSON files for both camera and pose information.
// */
//public class Readers {
//
//    /**
//     * Reads and parses camera data from a JSON file.
//     */
//    public static class ReaderJsonCamera {
//        private final String path;
//        private final List<StampedDetectedObjects> stampedDetectedObjects;
//
//        public ReaderJsonCamera(String path) {
//            this.path = path;
//            this.stampedDetectedObjects = new ArrayList<>();
//            loadData();
//        }
//
//        private void loadData() {
//            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
//                Gson gson = new Gson();
//                StringBuilder jsonBuilder = new StringBuilder();
//
//                // Read the file line by line
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    jsonBuilder.append(line.trim());
//                }
//
//                // Parse JSON data
//                JsonObject cameraData = gson.fromJson(jsonBuilder.toString(), JsonObject.class);
//
//                // Iterate through camera entries
//                for (Map.Entry<String, JsonElement> cameraEntry : cameraData.entrySet()) {
//                    JsonArray cameraEntries = cameraEntry.getValue().getAsJsonArray();
//                    for (JsonElement entryElement : cameraEntries) {
//                        JsonObject entry = entryElement.getAsJsonObject();
//                        int time = entry.get("time").getAsInt();
//                        JsonArray detectedObjects = entry.getAsJsonArray("detectedObjects");
//                        List<DetectedObject> detectedObjectList = new ArrayList<>();
//
//                        for (JsonElement objectElement : detectedObjects) {
//                            JsonObject detectedObject = objectElement.getAsJsonObject();
//                            String id = detectedObject.get("id").getAsString();
//                            String description = detectedObject.get("description").getAsString();
//                            detectedObjectList.add(new DetectedObject(id, description));
//                        }
//
//                        StampedDetectedObjects stampedObject = new StampedDetectedObjects(time, detectedObjectList);
//                        stampedDetectedObjects.add(stampedObject);
//                    }
//                }
//            } catch (IOException e) {
//                throw new RuntimeException("Error reading the camera data file: " + path, e);
//            }
//        }
//
//        public List<StampedDetectedObjects> getStampedDetectedObjects() {
//            return stampedDetectedObjects;
//        }
//    }
//
//    /**
//     * Reads and parses pose data from a JSON file.
//     */
//    public static class ReaderJsonPose {
//        private final List<Pose> poses;
//        private final String path;
//
//        public ReaderJsonPose(String path) {
//            this.path = path;
//            this.poses = new ArrayList<>();
//            loadData();
//        }
//
//        private void loadData() {
//            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
//                Gson gson = new Gson();
//                StringBuilder jsonBuilder = new StringBuilder();
//
//                // Read the file line by line
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    jsonBuilder.append(line.trim());
//                }
//
//                // Parse JSON array data
//                JsonArray jsonArray = gson.fromJson(jsonBuilder.toString(), JsonArray.class);
//
//                for (JsonElement element : jsonArray) {
//                    JsonObject jsonObject = element.getAsJsonObject();
//                    int time = jsonObject.get("time").getAsInt();
//                    float x = jsonObject.get("x").getAsFloat();
//                    float y = jsonObject.get("y").getAsFloat();
//                    float yaw = jsonObject.get("yaw").getAsFloat();
//
//                    poses.add(new Pose(x, y, yaw, time));
//                }
//            } catch (IOException e) {
//                throw new RuntimeException("Error reading the pose data file: " + path, e);
//            }
//        }
//
//        public List<Pose> getPoses() {
//            return poses;
//        }
//    }
//}
