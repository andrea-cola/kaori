package com.kaori.kaori.Kaori.FinderFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.kaori.kaori.App;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharePositionFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private double latitude, longitude;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private TextInputEditText activityEdit;
    private Location location;
    private TextView overlay;
    private Button shareButton;

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(App.getActiveContext(), getString(R.string.mapbox_acces_token));
        View view = inflater.inflate(R.layout.share_position, container, false);
        activityEdit = view.findViewById(R.id.share_editText);
        shareButton = view.findViewById(R.id.shareButton);
        overlay = view.findViewById(R.id.overlay);

        mapView = view.findViewById(R.id.shareMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        shareButton.setOnClickListener(v -> {
            if(activityEdit.getText() != null && activityEdit.getText().length() > 0)
                findPlace();
            else
                LogManager.getInstance().showVisualMessage("Specify what you are studying");
        });

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);

        return view;
    }

    @Override
    public void onMapReady(MapboxMap m) {
        this.mapboxMap = m;

        if(location != null)
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
                if(response.isSuccessful() && response.body().features().size() > 0)
                        sharePosition(response.body().features().get(0).placeName().substring(0, response.body().features().get(0).placeName().indexOf(",")),
                                new GeoPoint(latitude, longitude), String.valueOf(activityEdit.getText()));
                else
                    LogManager.getInstance().showVisualMessage("Problems during localization.");
            }
            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable t) {
                LogManager.getInstance().printConsoleError(call.toString() + " " + t.getMessage());
                LogManager.getInstance().showVisualMessage("It is not possible share your position.");
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style style) {
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(getContext(), style);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);

        try {
            overlay.setVisibility(View.GONE);
            shareButton.setAlpha(1f);
            shareButton.setEnabled(true);
            latitude = locationComponent.getLastKnownLocation().getLatitude();
            longitude = locationComponent.getLastKnownLocation().getLongitude();
            CameraPosition newCameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(13)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 500);

            if(mapboxMap.getMarkers().size() == 1)
                mapboxMap.getMarkers().get(0).setPosition(new LatLng(latitude, longitude));
            else
                mapboxMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
        } catch (Exception e){
            LogManager.getInstance().showVisualMessage("I cannot find your GPS position.");
        }
    }

    private void sharePosition(String placeName, GeoPoint geoPoint, String activity){
        DataManager.getInstance().uploadPosition(new Position(DataManager.getInstance().getMiniUser(), geoPoint, activity, Timestamp.now().getSeconds(), placeName));
        DataManager.getInstance().getUser().setPosition(new Position(DataManager.getInstance().getMiniUser(), geoPoint, activity, Timestamp.now().getSeconds(), placeName));

        if(getActivity() != null)
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