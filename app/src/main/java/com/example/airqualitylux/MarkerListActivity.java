package com.example.airqualitylux;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MarkerListActivity extends AppCompatActivity {
    private ListView listView;
    private List<JSONObject> sensorDataList = new ArrayList<>();
    private ArrayList<String> markerInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_list);

        listView = findViewById(R.id.listView);

        // Get the sensor data from the singleton
        sensorDataList = DataHolder.getInstance().getSensorDataList();

        // Process the sensor data to create the strings for the list view
        for (JSONObject sensorData : sensorDataList) {
            try {
                JSONObject sensor = sensorData.getJSONObject("sensor");
                JSONObject location = sensorData.getJSONObject("location");

                String info = "Lat - " + location.getString("latitude") + ", " + "Long - " + location.getString("longitude");

                // Get sensor data values
                JSONArray sensorDataValues = sensorData.getJSONArray("sensordatavalues");
                for (int i = 0; i < sensorDataValues.length(); i++) {
                    JSONObject sensorDataValue = sensorDataValues.getJSONObject(i);
                    String valueType = sensorDataValue.getString("value_type");

                    // If the value type is P1, P2, or humidity, add it to the info string
                    if (valueType.equals("P1") || valueType.equals("P2") || valueType.equals("humidity")) {
                        info += "\n" + valueType + ": " + sensorDataValue.getString("value");
                    }
                }
                info += "\nSensor ID: " + sensor.getString("id")
                        + "\nTimestamp: " + sensorData.getString("timestamp");

                markerInfos.add(info);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Set the adapter for the list view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, markerInfos);
        listView.setAdapter(adapter);
    }
}
