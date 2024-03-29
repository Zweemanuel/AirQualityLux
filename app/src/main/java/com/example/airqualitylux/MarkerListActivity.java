package com.example.airqualitylux;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.location.Geocoder;
import android.location.Address;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MarkerListActivity extends AppCompatActivity {
    private ListView listView;
    private ImageButton backButton;
    private EditText searchEditText;
    private List<JSONObject> sensorDataList = new ArrayList<>();
    private ArrayList<String> markerInfos = new ArrayList<>();
    private ArrayList<String> filteredMarkerInfos;
    private ArrayAdapter<String> adapter;

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

                double latitude = location.getDouble("latitude");
                double longitude = location.getDouble("longitude");

                String cityName = "";
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        cityName = addresses.get(0).getLocality();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String info =(cityName != null ? cityName : "Unknown");

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
                info += "\nTime: " + sensorData.getString("timestamp");

                markerInfos.add(info);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Set the adapter for the list view & instantiate the filteredList
        filteredMarkerInfos = new ArrayList<>(markerInfos);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredMarkerInfos);
        listView.setAdapter(adapter);

        //Back button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Search function

        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMarkers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not used
            }
        });
    }

    private void filterMarkers(String searchText) {
        filteredMarkerInfos.clear();

        if (TextUtils.isEmpty(searchText)) {
            filteredMarkerInfos.addAll(markerInfos);
        } else {
            for (String markerInfo : markerInfos) {
                if (markerInfo.toLowerCase().contains(searchText.toLowerCase())) {
                    filteredMarkerInfos.add(markerInfo);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}
