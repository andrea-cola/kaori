package com.kaori.kaori.Kaori.FinderFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.kaori.kaori.App;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharePositionFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private MapView mapView;
    private EditText activityEdit;
    private Location location;
    private MapboxMap mapboxMap;
    private double latitude, longitude;

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(App.getActiveContext(), getString(R.string.mapbox_acces_token));
        View view = inflater.inflate(R.layout.share_position, container, false);
        activityEdit = view.findViewById(R.id.exam_layout);
        view.findViewById(R.id.shareButton).setOnClickListener(v -> findPlace());

        mapView = view.findViewById(R.id.shareMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if(getActivity() != null)
            ((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE))
                    .requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);

        return view;
    }

    @Override
    public void onMapReady(MapboxMap m) {
        this.mapboxMap = m;
        onLocationChanged(location);
    }

    private void findPlace(){
        MapboxGeocoding geocoding = MapboxGeocoding.builder()
                .accessToken(getString(R.string.mapbox_acces_token))
                .query(Point.fromLngLat(longitude, latitude))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .build();

        geocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeocodingResponse> call, @NonNull Response<GeocodingResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<CarmenFeature> features = response.body().features();
                    if(features.size() > 0)
                        sharePosition(features.get(0).placeName().substring(0, features.get(0).placeName().indexOf(",")), new GeoPoint(latitude, longitude), String.valueOf(activityEdit.getText()));
                }
            }
            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable t) { }
        });
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style style) {
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
    }

    private void sharePosition(String placeName, GeoPoint geoPoint, String activity){
        DataManager.getInstance().uploadPosition(new Position(DataManager.getInstance().getMiniUser(), geoPoint, activity, Timestamp.now().getSeconds(), placeName));
        DataManager.getInstance().getUser().setPosition(new Position(DataManager.getInstance().getMiniUser(), geoPoint, activity, Timestamp.now().getSeconds(), placeName));
        if (getActivity() != null)
            getActivity().getSupportFragmentManager().popBackStackImmediate();
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if(mapboxMap != null)
            mapboxMap.setStyle(Style.OUTDOORS, this::enableLocationComponent);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        LogManager.getInstance().showVisualMessage("Hai disabilitato il GPS.");
        if(getActivity() != null)
            getActivity().getSupportFragmentManager().popBackStackImmediate();
    }
}