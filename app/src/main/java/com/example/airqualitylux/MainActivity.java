package com.example.airqualitylux;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

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

    private ImageButton centerButton;
    private CheckBox centerButtonCheck;
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

        new FetchDataTask(mapView, MainActivity.this, markersOverlay).execute("https://data.sensor.community/airrohr/v1/filter/country=LU");

        // User Interface

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
                mapView.getController().setZoom(10.5);
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
        });
    }
}