package com.kaori.kaori.FinderFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaori.kaori.KaoriChat;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.List;

/**
 * This fragment shows the shared relative positions of the users
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Elements from view
     */
    private View view;

    /**
     * Variables
     */
    private int selectedPosition;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private List<Position> positions;

    /**
     * Constants
     */
    private final String snippet = "Contact me";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(getContext() != null) {
            Mapbox.getInstance(getContext(), getString(R.string.mapbox_acces_token));
            view = inflater.inflate(R.layout.map_layout, container, false);

            mapView = view.findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
            return view;
        }
        return null;
    }

    public void setParameters(List<Position> positions, int i){
        this.positions = positions;
        this.selectedPosition = i;
    }

    private void setUpMarkers(){
        for(Position position: positions){
            Double latitude = position.getGeoPoint().getLatitude();
            Double longitude = position.getGeoPoint().getLongitude();
            mapboxMap.addMarker(new MarkerOptions()
                    .setTitle(position.getUser().getName())
                    .setSnippet(snippet)
                    .setPosition(new LatLng(latitude, longitude)));
            mapboxMap.setOnInfoWindowClickListener(marker -> false);
            addCustomInfoWindowAdapter(position);
        }
    }

    private void addCustomInfoWindowAdapter(Position position) {
        mapboxMap.setInfoWindowAdapter(marker -> {
            View v = getLayoutInflater().inflate(R.layout.mapbox_custom_info_window, null);
            ((TextView)v.findViewById(R.id.name)).setText(position.getUser().getName());
            v.findViewById(R.id.button_ok).setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), KaoriChat.class);
                intent.putExtra("user", position.getUser());
                startActivity(intent);
            });
            return v;
        });
    }

    private void moveCamera(){
        Double mLatitude = positions.get(selectedPosition).getGeoPoint().getLatitude();
        Double mLongitude = positions.get(selectedPosition).getGeoPoint().getLongitude();
        CameraPosition newCameraPosition = new CameraPosition.Builder()
                .target(new LatLng(mLatitude, mLongitude))
                .zoom(14)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 4000);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap){
        this.mapboxMap = mapboxMap;
        moveCamera();
        setUpMarkers();
    }

    @Override
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
