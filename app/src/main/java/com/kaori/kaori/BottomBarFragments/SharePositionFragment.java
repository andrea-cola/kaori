package com.kaori.kaori.BottomBarFragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.JsonObject;
import com.kaori.kaori.DataHub;
import com.kaori.kaori.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class represent the layout to share the user position
 */
public class SharePositionFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    /**
     * Constants
     */
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    private String leonardo = "Piazzale Leonardo da Vinci 32";
    private String polimi = "Politecnico di Milano";
    private String idMap = "mapbox-dc";
    private double longPolimi = 9.22771728762848;
    private double latPolimi = 45.478547684301645;

    /**
     * Elements from view
     */
    private MapView mapView;
    private View view;
    private Button shareButton;
    private TextView nameView;
    private CardView card;

    /**
     * Variables
     */
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private CarmenFeature work;
    private FirebaseFirestore db;
    private String userUid;
    private String userName;
    private CarmenFeature feature;
    private Icon icon;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Create mapbox instance
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_acces_token));

        view = inflater.inflate(R.layout.share_position, container, false);

        // Get map view
        mapView = (MapView) view.findViewById(R.id.shareMapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        return view;
    }

    /**
     * This method is called when the map is ready
     */
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        enableLocationComponent();

        setUpFragment();
        setUpButtons();

        setUpSource();
        setUpLayer();
    }

    /**
     * This method enables the location component in order to see
     * the actual position of the user
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(){
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {

            LocationComponentOptions options = LocationComponentOptions.builder(getContext())
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(getContext(), R.color.mapbox_blue))
                    .build();

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(getContext(), options);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    /**
     * This method sets up the elements of this fragment
     */
    private void setUpFragment(){
        shareButton = view.findViewById(R.id.shareButton);
        card = view.findViewById(R.id.cardSearchView);
        nameView = view.findViewById(R.id.positionName);
        card.setVisibility(View.INVISIBLE);

        icon = IconFactory.getInstance(getContext()).defaultMarkerView();

        db = FirebaseFirestore.getInstance();
        DataHub dataHub = DataHub.getInstance();
        userUid = dataHub.getUser().getUid();
        userName = dataHub.getUser().getName();

        addUserLocations();
    }

    /**
     * This method sets up the elements' listeners of this fragment
     */
    private void setUpButtons(){
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken())
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(ResourcesCompat.getColor(getResources(), R.color.GREY_LIGHT, null))
                                .limit(10)
                                .hint(getString(R.string.search_hint))
                                .addInjectedFeature(work)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(getActivity());
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shareButton.isEnabled()) {
                    GeoPoint point = new GeoPoint(((Point)feature.geometry()).latitude(), ((Point)feature.geometry()).longitude());
                    String name = feature.placeName();
                    sharePosition(name, point);
                }else{
                    Toast.makeText(getActivity(), "No place selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * This method adds a preference location
     */
    private void addUserLocations() {
        work = CarmenFeature.builder().text(polimi)
                .placeName(leonardo)
                .geometry(Point.fromLngLat(longPolimi, latPolimi))
                .id(idMap)
                .properties(new JsonObject())
                .build();
    }

    /**
     * This method gets the location when the search of the position
     * is returned from the intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // When the activity is returned
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            CarmenFeature feature = PlaceAutocomplete.getPlace(data);
            Double latitude = ((Point) feature.geometry()).latitude();
            Double longitude = ((Point) feature.geometry()).longitude();

            // Create a new FeatureCollection and add a new Feature to it using feature above
            FeatureCollection featureCollection = FeatureCollection.fromFeatures(
                    new Feature[]{Feature.fromJson(feature.toJson())});

            // Retrieve and update the source designated for showing a selected location's symbol layer icon
            GeoJsonSource source = mapboxMap.getSourceAs(geojsonSourceLayerId);
            if (source != null) {
                source.setGeoJson(featureCollection);
            }

            // Move map camera to the selected location
            CameraPosition newCameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(14)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 4000);

            mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(feature.placeName())
                    .snippet(feature.address())
                    .setIcon(icon));

            // Clear the recent history
            PlaceAutocomplete.clearRecentHistory(getContext());

            card.setVisibility(View.VISIBLE);
            nameView.setText(feature.placeName());
            shareButton.setEnabled(true);
        }
    }

    /**
     * This method update the position of the user in Firebase Cloud
     */
    private void sharePosition(String locationName, GeoPoint geoPoint){
        // Fields to add the position document
        Map<String, GeoPoint> position = new HashMap<>();
        position.put("position", geoPoint);
        Map<String, String> data = new HashMap<>();
        data.put("name", locationName);
        data.put("username", userName);

        // Search for the document of the current user
        Task task = db.collection("positions").whereEqualTo("uid", userUid).get();

        // If there already exists the current user position
        // otherwise update of the position of the user
        if(task.isSuccessful()){
            task.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        String id = task.getResult().getDocuments().get(0).getId();
                        db.collection("positions")
                                .document(id)
                                .set(position, SetOptions.merge());
                        db.collection("positions")
                                .document(id)
                                .set(data, SetOptions.merge());
                    }
                }
            });
        }else{
            String uiCode = UUID.randomUUID().toString();
            data.put("uid",userUid);
            db.collection("positions")
                    .document(uiCode)
                    .set(position, SetOptions.merge());
            db.collection("positions")
                    .document(uiCode)
                    .set(data, SetOptions.merge());
        }
    }

    /**
     * This method sets up the resources
     */
    private void setUpSource() {
        GeoJsonSource geoJsonSource = new GeoJsonSource(geojsonSourceLayerId);
        mapboxMap.addSource(geoJsonSource);
    }

    /**
     * This method sets up the layer
     */
    private void setUpLayer() {
        SymbolLayer selectedLocationSymbolLayer = new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId);
        selectedLocationSymbolLayer.withProperties(PropertyFactory.iconImage(symbolIconId));
        mapboxMap.addLayer(selectedLocationSymbolLayer);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getContext(), R.string.mapbox_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent();
        } else {
            Toast.makeText(getContext(), R.string.mapbox_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }
}