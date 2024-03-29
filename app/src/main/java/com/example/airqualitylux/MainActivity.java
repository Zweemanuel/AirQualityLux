package com.example.airqualitylux;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageButton centerButton, infoButton,markerlistButton;
    private TextView markerNumberText;
    private Switch pollutionSwitch,focusSwitch;
    private SeekBar seekBar;
    private CheckBox centerButtonCheck,seekBarCheck;
    private float currentZoomLevel;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private MyLocationNewOverlay locationOverlay;
    private FetchDataTask fetchDataTask;
    private MapView mapView;
    private GpsMyLocationProvider locationProvider;
    private FolderOverlay markersOverlay;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupMapView();
        setupOverlays();
        setupDataRefresh();

        setupSharedPreferences();
        setupUI();
    }
    private void setupMapView(){
        //Initialize mapView
        mapView = findViewById(R.id.mapView);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        //Set up tile source with Mapnik
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        /**
        //Set up custom tile source with Geoapify API
        String geoapifyApiKey = "...";//api key
        String geoapifyStyle = "osm-bright"; //map style

        XYTileSource geoapifyTileSource = new XYTileSource(
                "Geoapify",
                1,
                19,
                256,
                ".png",
                new String[]{"https://maps.geoapify.com/v1/tile/" + geoapifyStyle + "/{z}/{x}/{y}.png?apiKey=" + geoapifyApiKey},
                "© OpenStreetMap contributors © Geoapify"
        ) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                String url = getBaseUrl()
                        .replace("{x}", MapTileIndex.getX(pMapTileIndex) + "")
                        .replace("{y}", MapTileIndex.getY(pMapTileIndex) + "")
                        .replace("{z}", MapTileIndex.getZoom(pMapTileIndex) + "");
                return url;
            }
        };

        // Configure mapView
        mapView.getTileProvider().setTileSource(geoapifyTileSource);
        mapView.getTileProvider().clearTileCache();
         */
        // Remove default zoom buttons and set multi-touch controls
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setMultiTouchControls(true);

        // Set initial view and zoom level
        mapView.getController().setZoom(10.5);
        mapView.getController().setCenter(new GeoPoint(49.8153, 6.1296)); // Set a default center point
    }
    private void setupOverlays(){
        // Create a location overlay and add it to the map
        markersOverlay = new FolderOverlay();
        locationProvider = new GpsMyLocationProvider(this);
        locationOverlay = new MyLocationNewOverlay(locationProvider, mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);
    }
    private void setupDataRefresh(){
        // Initialize the handler and runnable to refresh the data
        refreshHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshData(mapView,  markersOverlay);
                refreshHandler.postDelayed(this, 60 * 60 * 1000); // 60 minutes
            }
        };

        // Start the periodic updates
        refreshHandler.post(refreshRunnable);

    }
    private void setupSharedPreferences(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
    }
    private void loadSharedPreferences(){
        // Load saved states
        centerButtonCheck.setChecked(sharedPreferences.getBoolean("centerButtonCheck", true));
        seekBarCheck.setChecked(sharedPreferences.getBoolean("seekBarCheck", false));
        focusSwitch.setChecked(sharedPreferences.getBoolean("focusSwitch", false));
    }
    private void setupUI(){
        setupSwitches();
        setupBottomCard();
        setupButtons();
        setupSeekbars();
        setupChecks();
        loadSharedPreferences();
    }
    private void setupSwitches(){
        //Pollution switch
        pollutionSwitch = findViewById(R.id.switch1);
        pollutionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            markersOverlay.setEnabled(isChecked);
            mapView.invalidate();
        });

        //Focus View with padding
        focusSwitch = findViewById(R.id.focusSwitch);
        double padding = 0.6f;
        if(focusSwitch.isChecked()){
            BoundingBox luxembourgBounds = new BoundingBox(50.182820+padding, 6.528500+padding, 49.447781-padding, 5.735700-padding);
            mapView.setScrollableAreaLimitDouble(luxembourgBounds);
        }
        focusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){

                BoundingBox luxembourgBounds = new BoundingBox(50.182820+padding, 6.528500+padding, 49.447781-padding, 5.735700-padding);
                mapView.setScrollableAreaLimitDouble(luxembourgBounds);
            }else{
                mapView.setScrollableAreaLimitDouble(null);
            }
            editor.putBoolean("focusSwitch", focusSwitch.isChecked());
            editor.apply();
        });
    }
    private void setupBottomCard(){
        // Number of markers
        markerNumberText = findViewById(R.id.markerNumberText);
        // Bottom card
        MaterialCardView cardView = findViewById(R.id.cardView);
        BottomSheetBehavior<MaterialCardView> bottomSheetBehavior = BottomSheetBehavior.from(cardView);
        bottomSheetBehavior.setPeekHeight(180); // Set the peek height in pixels
        bottomSheetBehavior.setHideable(false); // Disallow hiding the bottom sheet
        bottomSheetBehavior.setHalfExpandedRatio(0.5f);
    }
    private void setupButtons(){
        // Center Button
        centerButton = findViewById(R.id.centerButton);
        centerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                float newZoomLevel = 10.5f;
                mapView.getController().zoomTo(newZoomLevel);
                mapView.getController().animateTo(new GeoPoint(49.8153, 6.1296));
                // Update the current zoom level in the seekbar
                currentZoomLevel = newZoomLevel;
                // Update the progress of seekBar based on the new zoom level
                seekBar.setProgress((int) (newZoomLevel * 10)); // assuming that progress = zoomLevel * 10, adjust as needed
            }
        });
        // Info Button
        infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.print("BUTTON");
                Intent infoActivityIntent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(infoActivityIntent);
            }
        });
        // Marker List Button
        markerlistButton = findViewById(R.id.markerlistButton);
        markerlistButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent markerListIntent = new Intent(MainActivity.this, MarkerListActivity.class);
                startActivity(markerListIntent);
            }
        });
    }
    private void setupSeekbars(){
        //Seekbar
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setProgress(150);
        currentZoomLevel = mapView.getZoomLevel();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean initialTouch = true;
            private int referencePoint;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (initialTouch) {
                        referencePoint = progress;
                        initialTouch = false;
                    } else {
                        if (progress > referencePoint) {
                            currentZoomLevel += 0.1f; // Increase zoom level
                        } else if (progress < referencePoint) {
                            currentZoomLevel -= 0.1f; // Decrease zoom level
                        }
                        mapView.getController().setZoom(currentZoomLevel);
                        referencePoint = progress;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                initialTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
    private void setupChecks(){
        // Center Check Button
        centerButtonCheck = findViewById(R.id.centerButtonCheck);
        centerButtonCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                centerButton.setVisibility(View.VISIBLE);
            } else {
                centerButton.setVisibility(View.INVISIBLE);
            }
            editor.putBoolean("centerButtonCheck", centerButtonCheck.isChecked());
            editor.apply();
        });


        // Seekbar check
        seekBarCheck = findViewById(R.id.seekBarCheck);
        seekBarCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                seekBar.setVisibility(View.VISIBLE);
            } else {
                seekBar.setVisibility(View.INVISIBLE);
            }
            editor.putBoolean("seekBarCheck", seekBarCheck.isChecked());
            editor.apply();
        });
    }
    private void refreshData(MapView mapView, FolderOverlay markersOverlay) {
        System.out.println("Data refreshed");
        markersOverlay.getItems().clear();
        if (fetchDataTask != null) {
            fetchDataTask.cancel(true); // Cancel the previous task if it's not null
        }
        fetchDataTask = new FetchDataTask(mapView, MainActivity.this, markersOverlay);
        fetchDataTask.execute("https://data.sensor.community/airrohr/v1/filter/country=LU");
    }
    public void updateMarkerNumberText(int n) {
        markerNumberText.setText(" " + n);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the runnable from the handler when the activity is destroyed
        refreshHandler.removeCallbacks(refreshRunnable);
    }

}