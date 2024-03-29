package com.kaori.kaori.Kaori.FinderFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaori.kaori.Chat.KaoriChat;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.LogManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.List;

/**
 * This fragment shows the shared relative positions of the users
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Variables
     */
    private int selectedPosition;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Position position;

    /**
     * Constants
     */
    private final String snippet = "Contact me";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getContext() != null) {
            Mapbox.getInstance(getContext(), getString(R.string.mapbox_acces_token));
            View view = inflater.inflate(R.layout.map_layout, container, false);

            mapView = view.findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
            return view;
        }
        return null;
    }

    public void setParameters(Position position) {
        this.position = position;
    }

    private void setUpMarkers() {
        Double latitude = position.getGeoPoint().getLatitude();
        Double longitude = position.getGeoPoint().getLongitude();
        mapboxMap.addMarker(new MarkerOptions()
                .setTitle(position.getUser().getName())
                .setSnippet(snippet)
                .setPosition(new LatLng(latitude, longitude)));
        addCustomInfoWindowAdapter();
    }

    private void addCustomInfoWindowAdapter() {
        mapboxMap.setInfoWindowAdapter(marker -> {
            View v = getLayoutInflater().inflate(R.layout.mapbox_custom_info_window, null);
            ((TextView) v.findViewById(R.id.name)).setText(position.getUser().getName());
            v.findViewById(R.id.button).setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), KaoriChat.class);
                intent.putExtra("user", position.getUser());
                startActivity(intent);
            });
            return v;
        });
    }

    private void moveCamera() {
        Double mLatitude = position.getGeoPoint().getLatitude();
        Double mLongitude = position.getGeoPoint().getLongitude();
        CameraPosition newCameraPosition = new CameraPosition.Builder()
                .target(new LatLng(mLatitude, mLongitude))
                .zoom(14)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 500);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.OUTDOORS);
        moveCamera();
        setUpMarkers();
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
