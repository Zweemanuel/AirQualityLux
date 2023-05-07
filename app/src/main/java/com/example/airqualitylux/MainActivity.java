package com.example.airqualitylux;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;

public class MainActivity extends AppCompatActivity {

    private ImageButton centerButton, infoButton;
    private TextView markerNumberText;
    private Switch pollutionSwitch;
    private SeekBar seekBar;
    private CheckBox centerButtonCheck,seekBarCheck;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private FolderOverlay pollutionOverlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FolderOverlay markersOverlay = new FolderOverlay();

        //Initialize mapView
        MapView mapView = findViewById(R.id.mapView);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        //Set up custom tile source with Geoapify API
        String geoapifyApiKey = "2d507363156146c4b766af4e7c9be402";//api key
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

        // Remove default zoom buttons and set multi-touch controls
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setMultiTouchControls(true);

        // Set initial view and zoom level
        mapView.getController().setZoom(10.5);
        mapView.getController().setCenter(new GeoPoint(49.8153, 6.1296)); // Set a default center point

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

        // User Interface
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Pollution switch
        pollutionSwitch = findViewById(R.id.switch1);
        pollutionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            markersOverlay.setEnabled(isChecked);
            mapView.invalidate();
        });
        // Number of markers
        markerNumberText = findViewById(R.id.markerNumberText);
        // Bottom card
        MaterialCardView cardView = findViewById(R.id.cardView);
        BottomSheetBehavior<MaterialCardView> bottomSheetBehavior = BottomSheetBehavior.from(cardView);
        bottomSheetBehavior.setPeekHeight(180); // Set the peek height in pixels
        bottomSheetBehavior.setHideable(false); // Disallow hiding the bottom sheet
        bottomSheetBehavior.setHalfExpandedRatio(0.5f);
        // Center Button
        centerButton = findViewById(R.id.centerButton);
        centerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mapView.getController().zoomTo(10.5);
                mapView.getController().animateTo(new GeoPoint(49.8153, 6.1296));
            }
        });
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
        // Vertical Seekbar Zoom
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mapView.getController().setZoom(seekBar.getProgress() / 10f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
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

        // Load saved states
        centerButtonCheck.setChecked(sharedPreferences.getBoolean("centerButtonCheck", true));
        seekBarCheck.setChecked(sharedPreferences.getBoolean("seekBarCheck", false));
        Log.d("onCreate", "SeekBar isChecked: " + seekBarCheck.isChecked());

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the runnable from the handler when the activity is destroyed
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void refreshData(MapView mapView, FolderOverlay markersOverlay) {

        System.out.println("Data refreshed");
        markersOverlay.getItems().clear();
        new FetchDataTask(mapView, MainActivity.this, markersOverlay).execute("https://data.sensor.community/airrohr/v1/filter/country=LU");

    }

    public void updateMarkerNumberText(int n) {
        markerNumberText.setText("Markers: " + n);
    }

}