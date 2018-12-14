package com.kaori.kaori.BottomBarFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kaori.kaori.ChatFragments.ChatFragment;
import com.kaori.kaori.DBObjects.Position;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.LogManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment shows the shared relative positions of the users
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Elements from view
     */
    private View view;

    /**
     * Variables
     */
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Position mPosition;
    private Query positions;

    /**
     * Constants
     */
    private final String snippet = "Contact me";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_acces_token));

        view = inflater.inflate(R.layout.map_layout, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }

    public void setParameters(Position position){
        this.mPosition = position;
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap){
        this.mapboxMap = mapboxMap;
        setUpFirebase();
        moveCamera();
    }

    /**
     * This method queries the Firebase Cloud.
     */
    private void setUpFirebase(){
        positions = FirebaseFirestore.getInstance().collection("positions");
        positions.get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        List<Position> positions = new ArrayList<>();
                        for(DocumentSnapshot snapshot: task.getResult()) {
                            positions.add(snapshot.toObject(Position.class));
                        }
                        setUpMarkers(positions);
                    }else{
                        LogManager.getInstance().printConsoleError("Error getting documents: " + task.getException());
                    }

                });
    }

    /**
     * This method set up the markers representing the users on the map
     */
    private void setUpMarkers(List<Position> positions){
        for(Position position: positions){
            Double latitude = position.getPoint().getLatitude();
            Double longitude = position.getPoint().getLongitude();
            mapboxMap.addMarker(new MarkerOptions()
                    .setTitle(position.getUsername())
                    .setSnippet(snippet)
                    .setPosition(new LatLng(latitude, longitude)));
            mapboxMap.setOnInfoWindowClickListener(marker -> false);
            addCustomInfoWindowAdapter(position);
        }
    }

    /**
     * Add a custom view to the marker in the mapbox.
     */
    private void addCustomInfoWindowAdapter(Position position) {
        mapboxMap.setInfoWindowAdapter(marker -> {
            View v = getLayoutInflater().inflate(R.layout.mapbox_custom_info_window, null);
            ((TextView)v.findViewById(R.id.name)).setText(position.getUsername());
            v.findViewById(R.id.button_ok).setOnClickListener(view -> {
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.newChatParams(position.getUid(), position.getUsername());

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, chatFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack(BACK_STATE_NAME)
                        .commit();
            });
            return v;
        });
    }

    /**
     * This method moves the map camera
     */
    private void moveCamera(){
        Double mLatitude = mPosition.getPoint().getLatitude();
        Double mLongitude = mPosition.getPoint().getLongitude();
        CameraPosition newCameraPosition = new CameraPosition.Builder()
                .target(new LatLng(mLatitude, mLongitude))
                .zoom(14)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 4000);
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
