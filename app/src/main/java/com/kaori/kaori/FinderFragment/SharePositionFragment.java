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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.JsonObject;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.UUID;

/**
 * This class represent the layout to share the user position
 */
public class SharePositionFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Constants
     */
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final String geojsonSourceLayerId = "geojsonSourceLayerId";
    private static final String symbolIconId = "symbolIconId";
    private static final String leonardo = "Piazzale Leonardo da Vinci 32";
    private static final String polimi = "Politecnico di Milano";
    private static final String idMap = "mapbox-dc";
    private static final double longPolimi = 9.22771728762848;
    private static final double latPolimi = 45.478547684301645;
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Elements from view
     */
    private MapView mapView;
    private View view;
    private Button shareButton;
    private TextView nameView;
    private CardView searchCard;
    private CardView shareCard;

    /**
     * Variables
     */
    private MapboxMap mapboxMap;
    private CarmenFeature work;
    private FirebaseFirestore db;
    private CarmenFeature feature;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_acces_token));

        view = inflater.inflate(R.layout.share_position, container, false);
        mapView = view.findViewById(R.id.shareMapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        setUpView();
        db = FirebaseFirestore.getInstance();
        addUserLocations();
        setUpButtons();

        return view;
    }

    /**
     * This method is called when the map is ready
     */
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        setUpSource();
        setUpLayer();
    }

    /**
     * This method sets up the elements of the view
     */
    private void setUpView(){
        shareButton = view.findViewById(R.id.shareButton);
        nameView = view.findViewById(R.id.positionName);
        shareCard = view.findViewById(R.id.shareCardView);
        searchCard = view.findViewById(R.id.searchCardView);

        shareCard.setVisibility(View.INVISIBLE);
    }

    /**
     * This method sets up the elements' listeners of this fragment
     */
    private void setUpButtons(){
        searchCard.setOnClickListener(v -> {
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken(Mapbox.getAccessToken())
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(ResourcesCompat.getColor(getResources(), R.color.grey_light, null))
                            .limit(10)
                            .hint(getString(R.string.search_hint))
                            .addInjectedFeature(work)
                            .build(PlaceOptions.MODE_CARDS))
                    .build(getActivity());
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        });

        shareButton.setOnClickListener(v -> {
            if(shareButton.isEnabled()) {
                Double latitude = ((Point)feature.geometry()).latitude();
                Double longitude = ((Point)feature.geometry()).longitude();
                GeoPoint point = new GeoPoint(latitude, longitude);
                String name = feature.placeName();
                sharePosition(name, point);
                Toast.makeText(getActivity(), "Your position is shared", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), "No place selected", Toast.LENGTH_SHORT).show();
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
     * This method invokes the book fragment when the card is clicked
     */
    private void invokeNextFragment() {
        if(getActivity() != null)
            getActivity().getSupportFragmentManager().popBackStackImmediate();
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
            feature = PlaceAutocomplete.getPlace(data);
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
                    .snippet(feature.address()));

            // Clear the recent history
            PlaceAutocomplete.clearRecentHistory(getContext());

            shareCard.setVisibility(View.VISIBLE);
            nameView.setText(feature.placeName().substring(0, 15).concat("..."));
        }
    }

    private void sharePosition(String locationName, GeoPoint geoPoint){
        Position position = new Position(DataManager.getInstance().getMiniUser(), geoPoint, locationName, Timestamp.now());

        OnCompleteListener onCompleteListener = task -> {
            if(task.isSuccessful())
                invokeNextFragment();
        };

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

}