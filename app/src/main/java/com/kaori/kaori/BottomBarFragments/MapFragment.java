package com.kaori.kaori.BottomBarFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.DBObjects.Position;
import com.kaori.kaori.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

/**
 * This fragment shows the shared relative positions of the users
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    /**
     * Constants
     */
    private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "custom-marker";

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
    private ArrayList<Position> positions;
    private FirebaseFirestore db;
    private Icon icon;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(),getString(R.string.mapbox_acces_token));

        view = inflater.inflate(R.layout.map_layout, container, false);

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        MapboxMapOptions options = new MapboxMapOptions()
                .styleUrl(Style.OUTDOORS)
                .camera(new CameraPosition.Builder()
                        .target(new LatLng(43.7383, 7.4094))
                        .zoom(12)
                        .build());


        return view;
    }

    public void setParameters(Position position){
        this.mPosition = position;
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap){
        this.mapboxMap = mapboxMap;
        icon = IconFactory.getInstance(getContext()).fromResource(R.drawable.ic_position);
        setUpFirebase();
        addMarkers();
    }

    /**
     * This method queries the Firebase Cloud
     */
    private void setUpFirebase(){
        db.collection("positions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot snapshot: task.getResult()){
                                setPositionList(snapshot);
                            }
                        }else{
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    /**
     * This method adds the markers on the map
     */
    private void addMarkers(){
        for(Position position: positions) {
            Double latitute = position.getPoint().getLatitude();
            Double longitude = position.getPoint().getLongitude();
            mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitute, longitude))
                    .setTitle(position.getLocationName())
                    .setSnippet(position.getUsername())
                    .setIcon(icon));
        }

        Double mLatitude = mPosition.getPoint().getLatitude();
        Double mLongitude = mPosition.getPoint().getLongitude();
        CameraPosition newCameraPosition = new CameraPosition.Builder()
                .target(new LatLng(mLatitude, mLongitude))
                .zoom(14)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 4000);
    }

    /**
     * This method sets the list of positions from Firebase
     */
    private void setPositionList(DocumentSnapshot snapshot){
        Position position = new Position();
        position.setLocationName((String) snapshot.get("name"));
        position.setPoint((GeoPoint) snapshot.get("position"));
        position.setUid((String) snapshot.get("uid"));
        position.setUsername((String) snapshot.get("username"));
        positions.add(position);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


}
