package com.example.airqualitylux;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MarkerListActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> markerInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_list);

        listView = findViewById(R.id.listView);

        // Fetch the marker data from the intent
        markerInfos = getIntent().getStringArrayListExtra("markerInfos");

        // Set the adapter for the list view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, markerInfos);
        listView.setAdapter(adapter);
    }
}
