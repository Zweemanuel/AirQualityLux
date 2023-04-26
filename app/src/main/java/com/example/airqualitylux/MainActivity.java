package com.example.airqualitylux;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize mapView
        MapView mapView = findViewById(R.id.mapView);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        // Remove default zoom buttons and set multi-touch controls
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setMultiTouchControls(true);
        // Set initial view and zoom level
        mapView.getController().setZoom(10.5);
        mapView.getController().setCenter(new GeoPoint(49.8153, 6.1296)); // Set a default center point

        // Configure mapView
        mapView.getTileProvider().setTileSource(TileSourceFactory.MAPNIK);
        mapView.getTileProvider().clearTileCache();
    }
}