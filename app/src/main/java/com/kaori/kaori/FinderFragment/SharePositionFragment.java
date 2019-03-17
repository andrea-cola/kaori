package com.kaori.kaori.FinderFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
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
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

/**
 * This class represent the layout to share the user position
 */
public class SharePositionFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    /**
     * Constants
     */
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final String geojsonSourceLayerId = "geojsonSourceLayerId";

    /**
     * Elements from view
     */
    private MapView mapView;
    private Button shareButton;
    private TextView pos;
    private CardView searchCard, shareCard;
    private EditText activityEdit;

    /**
     * Variables
     */
    private MapboxMap mapboxMap;
    private CarmenFeature feature;
    private PermissionsManager permissionsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_acces_token));

        View view = inflater.inflate(R.layout.share_position, container, false);
        shareButton = view.findViewById(R.id.shareButton);
        pos = view.findViewById(R.id.position);
        searchCard = view.findViewById(R.id.searchCardView);
        shareCard = view.findViewById(R.id.shareCard);
        activityEdit = view.findViewById(R.id.editText2);

        mapView = view.findViewById(R.id.shareMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        setUpButtons();

        return view;
    }

    /**
     * This method is called when the map is ready
     */
    @Override
    public void onMapReady(MapboxMap m) {
        this.mapboxMap = m;
        mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(Style style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            // Activate with options
            locationComponent.activateLocationComponent(getContext(), style);
            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            Double locLatitude = locationComponent.getLastKnownLocation().getLatitude();
            Double locLongitude = locationComponent.getLastKnownLocation().getLongitude();

            CameraPosition newCameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(locLatitude, locLongitude))
                    .zoom(14)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 500);

            mapboxMap.addMarker(new MarkerOptions()
                    .setSnippet("Sono qui")
                    .position(new LatLng(locLatitude, locLongitude)));

            shareCard.setVisibility(View.VISIBLE);
            pos.setText("La mia posizione attuale");

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    /**
     * This method sets up the elements' listeners of this fragment
     */
    private void setUpButtons() {
        searchCard.setOnClickListener(v -> {
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken(Mapbox.getAccessToken())
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(ResourcesCompat.getColor(getResources(), R.color.grey_light, null))
                            .limit(10)
                            .hint(getString(R.string.search_hint))
                            .build(PlaceOptions.MODE_CARDS))
                    .build(getActivity());
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        });

        shareButton.setOnClickListener(v -> {
            if (shareButton.isEnabled()) {
                Double latitude = ((Point) feature.geometry()).latitude();
                Double longitude = ((Point) feature.geometry()).longitude();
                GeoPoint point = new GeoPoint(latitude, longitude);
                String name = feature.placeName();

                sharePosition(name, point, String.valueOf(activityEdit.getText()));
                LogManager.getInstance().showVisualMessage("Your position is shared");
            } else {
                LogManager.getInstance().showVisualMessage("No place selected");
            }
        });
    }

    /**
     * This method gets the location when the search of the position
     * is returned from the intent.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // When the activity is returned
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            feature = PlaceAutocomplete.getPlace(data);
            Double latitude = ((Point) feature.geometry()).latitude();
            Double longitude = ((Point) feature.geometry()).longitude();

            // Create a new FeatureCollection and add a new Feature to it using feature above
            FeatureCollection featureCollection = FeatureCollection.fromFeatures(
                    new Feature[]{Feature.fromJson(feature.toJson())});

            // Retrieve and update the source designated for showing a selected location's symbol layer icon
            GeoJsonSource source = mapboxMap.getStyle().getSourceAs(geojsonSourceLayerId);
            if (source != null) {
                source.setGeoJson(featureCollection);
            }

            // Move map camera to the selected location
            CameraPosition newCameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(14)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 500);

            mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(feature.placeName())
                    .snippet(feature.address()));

            // Clear the recent history
            PlaceAutocomplete.clearRecentHistory(getContext());

            shareCard.setVisibility(View.VISIBLE);
            pos.setText(feature.placeName());
        }
    }

    private void sharePosition(String locationName, GeoPoint geoPoint, String activity){
        Position position = new Position(DataManager.getInstance().getMiniUser(), geoPoint, activity, locationName, Timestamp.now());
        /*
        CollectionReference ref = db.collection(Constants.DB_COLL_POSITIONS);
        ref.whereEqualTo("user.uid", position.getUser().getUid())
            .addSnapshotListener((value, e) -> {
                if(value != null)
                    if(value.getDocuments().size() == 0) {
                        position.setPositionID(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
                        ref.document(position.getPositionID()).set(position).addOnCompleteListener(onCompleteListener);
                    } else {
                        position.setPositionID(value.getDocuments().get(0).getId());
                        ref.document(position.getPositionID()).set(position).addOnCompleteListener(onCompleteListener);
                    }
            });
            */
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
            getActivity().finish();
        }
    }
}