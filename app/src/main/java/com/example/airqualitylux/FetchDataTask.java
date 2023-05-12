package com.example.airqualitylux;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FetchDataTask extends AsyncTask<String, Void, JSONArray> {

    private List<JSONObject> sensorDataList = new ArrayList<>();
    private final MapView mapView;
    private Context mContext;
    private FolderOverlay pollutionOverlay;

    public FetchDataTask(MapView mapView, Context context, FolderOverlay markersOverlay) {
        this.mapView = mapView;
        this.mContext = context;
        this.pollutionOverlay = markersOverlay;
    }

    @Override
    protected JSONArray doInBackground(String... urls) {
        try {
            return fetchJsonFromUrl(urls[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONArray fetchJsonFromUrl(String urlStr) throws IOException, JSONException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder jsonStringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonStringBuilder.append(line);
        }
        reader.close();
        return new JSONArray(jsonStringBuilder.toString());
    }

    @Override
    protected void onPostExecute(JSONArray jsonData) {
        if (jsonData == null) {
            return;
        }

        try {
            processJsonData(jsonData);
            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processJsonData(JSONArray jsonData) throws JSONException {
        HashMap<String, JSONObject> uniqueLocations = extractUniqueLocations(jsonData);

        for (JSONObject uniqueSensorData : uniqueLocations.values()) {
            addMarkerForSensorData(uniqueSensorData);
        }
    }

    private HashMap<String, JSONObject> extractUniqueLocations(JSONArray jsonData) throws JSONException {
        HashMap<String, JSONObject> uniqueLocations = new HashMap<>();

        for (int i = 0; i < jsonData.length() - 1; i++) {
            JSONObject sensorData = jsonData.getJSONObject(i);
            JSONObject location = sensorData.getJSONObject("location");
            double latitude = location.getDouble("latitude");
            double longitude = location.getDouble("longitude");

            String locationKey = String.format("%f_%f", latitude, longitude);

            if (uniqueLocations.containsKey(locationKey)) {
                updateExistingData(uniqueLocations.get(locationKey), sensorData);
            } else {
                uniqueLocations.put(locationKey, sensorData);
            }
        }
        return uniqueLocations;
    }

    private void updateExistingData(JSONObject existingData, JSONObject newSensorData) throws JSONException {
        JSONArray newSensorDataValues = newSensorData.getJSONArray("sensordatavalues");
        JSONArray existingSensorDataValues = existingData.getJSONArray("sensordatavalues");

        for (int j = 0; j < newSensorDataValues.length(); j++) {
            JSONObject newSensorDataValue = newSensorDataValues.getJSONObject(j);
            String newValueType = newSensorDataValue.getString("value_type");

            boolean updated = false;
            for (int k = 0; k < existingSensorDataValues.length(); k++) {
                JSONObject existingSensorDataValue = existingSensorDataValues.getJSONObject(k);
                String existingValueType = existingSensorDataValue.getString("value_type");

                if (newValueType.equals(existingValueType)) {
                    String newTimestamp = newSensorData.getString("timestamp");
                    String existingTimestamp = existingData.getString("timestamp");

                    if (newTimestamp.compareTo(existingTimestamp) > 0) {
                        existingSensorDataValue.put("value", newSensorDataValue.getString("value"));
                        existingData.put("timestamp", newTimestamp);
                    }
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                existingSensorDataValues.put(newSensorDataValue);
            }
        }
    }

    private void addMarkerForSensorData(JSONObject sensorData) throws JSONException {
        JSONObject location = sensorData.getJSONObject("location");
        JSONObject sensor = sensorData.getJSONObject("sensor");
        double latitude = location.getDouble("latitude");
        double longitude = location.getDouble("longitude");

        JSONArray sensorDataValues = sensorData.getJSONArray("sensordatavalues");
        StringBuilder descriptionBuilder = new StringBuilder();

        for (int j = 0; j < sensorDataValues.length(); j++) {
            JSONObject sensorDataValue = sensorDataValues.getJSONObject(j);
            // Add a filter for the values
            String valueType = sensorDataValue.getString("value_type");
            if(!(valueType.contains("P1") || valueType.contains("P2")||valueType.contains("temperature")||valueType.contains("humidity"))){
                continue;
            }
            descriptionBuilder.append(sensorDataValue.getString("value_type"))
                    .append(": ")
                    .append(sensorDataValue.getString("value"))
                    .append("\n");
        }

        descriptionBuilder.append("Timestamp: ").append(sensorData.getString("timestamp"));

        GeoPoint point = new GeoPoint(latitude, longitude);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);

        marker.setTitle("Sensor ID: " + sensor.getString("id"));
        marker.setSnippet(descriptionBuilder.toString());

        marker.setInfoWindow(new CustomInfoWindowAdapter(R.layout.custom_marker_info_window, mapView));

        pollutionOverlay.add(marker);

        marker.setOnMarkerClickListener((marker1, mapView1) -> {
            if (marker1.isInfoWindowShown()) {
                marker1.closeInfoWindow();
            } else {
                marker1.showInfoWindow();
                mapView1.getController().animateTo(marker1.getPosition());
            }
            return true;
        });
        // Add sensorData for future use
        sensorDataList.add(sensorData);
        DataHolder.getInstance().setSensorDataList(sensorDataList);
    }


    private void updateUI() {
        mapView.post(() -> {
            mapView.getOverlays().remove(pollutionOverlay);
            mapView.getOverlays().add(pollutionOverlay);
            mapView.invalidate(); // Refresh the map
        });
        // Update the number of pollution markers
        ((MainActivity) mContext).updateMarkerNumberText(pollutionOverlay.getItems().size());
    }
}
