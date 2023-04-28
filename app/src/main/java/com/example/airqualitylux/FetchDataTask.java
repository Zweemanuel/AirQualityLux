package com.example.airqualitylux;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class FetchDataTask extends AsyncTask<String, Void, JSONArray> {

    private final MapView mapView;
    private Context mContext;
    FolderOverlay pollutionOverlay;

    public FetchDataTask(MapView mapView, Context context, FolderOverlay markersOverlay) {
        this.mapView = mapView;
        this.mContext = context;
        this.pollutionOverlay = markersOverlay;
    }

    @Override
    protected JSONArray doInBackground(String... urls) {
        try {
            URL url = new URL(urls[0]);
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONArray jsonData) {
        if (jsonData == null) {
            return;
        }

        // A HashMap to store sensor data for each unique latitude and longitude pair
        HashMap<String, JSONObject> uniqueLocations = new HashMap<>();

        try {
            for (int i = 0; i < jsonData.length() - 1; i++) {
                JSONObject sensorData = jsonData.getJSONObject(i);
                JSONObject location = sensorData.getJSONObject("location");
                double latitude = location.getDouble("latitude");
                double longitude = location.getDouble("longitude");

                String locationKey = String.format("%f_%f", latitude, longitude);

                if (!uniqueLocations.containsKey(locationKey)) {
                    uniqueLocations.put(locationKey, location);
                }
            }

            for (JSONObject uniqueLocation : uniqueLocations.values()) {
                double latitude = uniqueLocation.getDouble("latitude");
                double longitude = uniqueLocation.getDouble("longitude");

                GeoPoint point = new GeoPoint(latitude, longitude);
                Marker marker = new Marker(mapView);
                marker.setPosition(point);

                pollutionOverlay.add(marker);
            }
            // Add the pollutionOverlay to the mapView and refresh the map
            mapView.getOverlays().add(pollutionOverlay);
            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
