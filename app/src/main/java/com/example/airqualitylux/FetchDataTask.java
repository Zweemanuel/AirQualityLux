package com.example.airqualitylux;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.ContextCompat;

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
import java.util.Random;

public class FetchDataTask extends AsyncTask<String, Void, JSONArray> {

    private final MapView mapView;
    private Context mContext;
    FolderOverlay pollutionOverlay;


    public FetchDataTask(MapView mapView, Context context,FolderOverlay markersOverlay ) {
        this.mapView = mapView;
        this.mContext = context;
        this.pollutionOverlay =markersOverlay;

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

                if (uniqueLocations.containsKey(locationKey)) {
                    JSONObject existingData = uniqueLocations.get(locationKey);

                    JSONArray newSensorDataValues = sensorData.getJSONArray("sensordatavalues");
                    JSONArray existingSensorDataValues = existingData.getJSONArray("sensordatavalues");

                    for (int j = 0; j < newSensorDataValues.length(); j++) {
                        JSONObject newSensorDataValue = newSensorDataValues.getJSONObject(j);
                        String newValueType = newSensorDataValue.getString("value_type");
                        boolean updated = false;

                        for (int k = 0; k < existingSensorDataValues.length(); k++) {
                            JSONObject existingSensorDataValue = existingSensorDataValues.getJSONObject(k);
                            String existingValueType = existingSensorDataValue.getString("value_type");

                            if (newValueType.equals(existingValueType)) {
                                String newTimestamp = sensorData.getString("timestamp");
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
                } else {
                    uniqueLocations.put(locationKey, sensorData);
                }
            }

            for (JSONObject uniqueSensorData : uniqueLocations.values()) {
                JSONObject location = uniqueSensorData.getJSONObject("location");
                JSONObject sensor = uniqueSensorData.getJSONObject("sensor");
                double latitude = location.getDouble("latitude");
                double longitude = location.getDouble("longitude");

                JSONArray sensorDataValues = uniqueSensorData.getJSONArray("sensordatavalues");
                StringBuilder descriptionBuilder = new StringBuilder();

                for (int j = 0; j < sensorDataValues.length(); j++) {
                    JSONObject sensorDataValue = sensorDataValues.getJSONObject(j);
                    descriptionBuilder.append(sensorDataValue.getString("value_type"))
                            .append(": ")
                            .append(sensorDataValue.getString("value"))
                            .append("\n");
                }

                descriptionBuilder.append("Timestamp: ").append(uniqueSensorData.getString("timestamp"));



                GeoPoint point = new GeoPoint(latitude, longitude);
                Marker marker = new Marker(mapView);
                marker.setPosition(point);
                marker.setAnchor(0.2f, 0.2f);

                Drawable markerDrawable = ContextCompat.getDrawable(mContext, R.drawable.marker);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) markerDrawable;
                marker.setIcon(bitmapDrawable);

                marker.setTitle("Sensor ID: " + sensor.getString("id"));
                marker.setSnippet(descriptionBuilder.toString());

                marker.setInfoWindow(new CustomInfoWindowAdapter(R.layout.custom_marker_info_window, mapView));

                pollutionOverlay.add(marker);

                marker.setOnMarkerClickListener((marker1, mapView1) -> {
                    if (marker1.isInfoWindowShown()) {
                        marker1.closeInfoWindow();
                    } else {
                        marker1.showInfoWindow();
                    }
                    return true;
                });
            }
            //Add the pollutionOverlay to the mapView
            mapView.getOverlays().add(pollutionOverlay);
            mapView.invalidate(); // Refresh the map
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
