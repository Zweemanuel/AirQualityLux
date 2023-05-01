package com.example.airqualitylux;

import android.util.Log;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class CustomInfoWindowAdapter extends InfoWindow {

    public CustomInfoWindowAdapter(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
    }

    @Override
    public void onOpen(Object item) {
        Marker marker = (Marker) item;
        TextView title = mView.findViewById(R.id.title);
        TextView snippet = mView.findViewById(R.id.snippet);

        Log.d("InfoWindow", "Title: " + marker.getTitle());
        Log.d("InfoWindow", "Snippet: " + marker.getSnippet());

        title.setText(marker.getTitle());
        snippet.setText(marker.getSnippet());

        mView.setOnClickListener(v -> {
            if (marker.isInfoWindowShown()) {
                marker.closeInfoWindow();
            }
        });
    }

    @Override
    public void onClose() {
        // Handle closing the info window if necessary
    }
}
