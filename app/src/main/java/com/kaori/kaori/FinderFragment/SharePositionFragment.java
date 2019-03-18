package com.kaori.kaori.FinderFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.List;

/**
 * This class represent the layout to share the user position
 */
public class SharePositionFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    /**
     * Elements from view
     */
    private MapView mapView;
    private EditText activityEdit;
    private double latitude, longitude;

    /**
     * Variables
     */
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_acces_token));

        View view = inflater.inflate(R.layout.share_position, container, false);
        activityEdit = view.findViewById(R.id.editText2);
        mapView = view.findViewById(R.id.shareMapView);

        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            new AlertDialog.Builder(getActivity())
                    .setMessage("Devi attivare il GPS per la posizione")
                    .setPositiveButton("OK", (d, which)-> {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        d.cancel();
                    })
                    .setNegativeButton("NO", (d, which)-> d.dismiss())
                    .show();
        }else{
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        view.findViewById(R.id.shareButton).setOnClickListener(v -> {
            GeoPoint point = new GeoPoint(latitude, longitude);
            sharePosition(point, String.valueOf(activityEdit.getText()));
        });

        return view;
    }

    /**
     * This method is called when the map is ready
     */
    @Override
    public void onMapReady(MapboxMap m) {
        this.mapboxMap = m;
        mapboxMap.setStyle(Style.OUTDOORS, style -> enableLocationComponent(style));
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(getContext(), style);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);

            latitude = locationComponent.getLastKnownLocation().getLatitude();
            longitude = locationComponent.getLastKnownLocation().getLongitude();

            CameraPosition newCameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(13)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 500);
            mapboxMap.addMarker(new MarkerOptions().setSnippet("Sono qui").position(new LatLng(latitude, longitude)));

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    /**
     * Save the position in database and return to the previous fragment.
     */
    private void sharePosition(GeoPoint geoPoint, String activity){
        DataManager.getInstance().uploadPosition(new Position(DataManager.getInstance().getMiniUser(), geoPoint, activity, Timestamp.now()));
        getFragmentManager().popBackStackImmediate();
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
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

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getContext(), R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(style -> enableLocationComponent(style));
        } else {
            Toast.makeText(getContext(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            getFragmentManager().popBackStackImmediate();
        }
    }
}