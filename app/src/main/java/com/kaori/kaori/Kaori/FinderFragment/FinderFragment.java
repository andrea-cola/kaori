package com.kaori.kaori.Kaori.FinderFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kaori.kaori.App;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

import java.util.List;

public class FinderFragment extends Fragment {

    /**
     * Constants
     */
    private final String BACK_STATE_NAME = getClass().getName();

    private RecyclerView recyclerView;
    private Context context;
    private LocationManager lm;
    private TextView noLocalize;
    private RecyclerAdapter adapter;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.finder_position_layout, container, false);
        lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        context = getContext();

        view.findViewById(R.id.positionFAB).setOnClickListener(v -> activateGPS());
        noLocalize = view.findViewById(R.id.nolocalize);

        recyclerView = view.findViewById(R.id.user_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter(DataManager.getInstance().getCurrentActivePositions());
        recyclerView.setAdapter(adapter);

        ((TextView)view.findViewById(R.id.empty_view_text)).setText(R.string.empty_text_finder);
        App.setEmptyView(view.findViewById(R.id.empty_view));
        DataManager.getInstance().downloadCurrentActivePositions(recyclerView);

        deactivePosition();

        return view;
    }

    private void deactivePosition() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    DataManager.getInstance().getUser().isPositioned()) {
                noLocalize.setVisibility(View.VISIBLE);
                noLocalize.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.dialog_disable_position))
                        .setPositiveButton("OK", (d, which) -> {
                            DataManager.getInstance().deletePosition();
                            invokeFragment(new FinderFragment(), FinderFragment.class.getSimpleName());
                            d.dismiss();
                        })
                        .setNegativeButton("NO", (d, which) -> d.dismiss())
                        .show());
            } else
                noLocalize.setVisibility(View.INVISIBLE);
        } else
            noLocalize.setVisibility(View.INVISIBLE);
    }

    private void activateGPS(){
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.dialog_enable_position))
                        .setPositiveButton("OK", (d, which)-> {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),1);
                            d.dismiss();
                        })
                        .setNegativeButton("NO", (d, which)-> d.dismiss())
                        .show();
            } else
                invokeFragment(new SharePositionFragment(), SharePositionFragment.class.getSimpleName());
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            invokeFragment(new SharePositionFragment(), SharePositionFragment.class.getSimpleName());
        else
            LogManager.getInstance().showVisualMessage(getString(R.string.position_not_active));
    }

    /**
     * This method invokes the share position fragment when the floating action button is clicked
     */
    private void invokeFragment(Fragment mapFragment, String tag) {
        if(getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mapFragment, tag)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
    }

    /**
     * Private Adapter for RecyclerView
     */
    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

        private List<Position> positions;

        /*package-private*/ RecyclerAdapter(List<Position> positions){
            this.positions = positions;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.finder_position_item, parent, false);

            view.setOnClickListener(view1 -> {
                MapFragment mapFragment = new MapFragment();
                mapFragment.setParameters(positions, recyclerView.getChildLayoutPosition(view));
                invokeFragment(mapFragment, "tag");
            });

            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, int i) {
            holder.user.setText(positions.get(i).getUser().getName());
            holder.activity.setText(getString(R.string.finder_item_studying) + positions.get(i).getActivity());
            holder.position.setText(positions.get(i).getPlaceName());
            Glide.with(context).load(positions.get(i).getUser().getThumbnail())
                    .apply(RequestOptions.circleCropTransform()).into(holder.thumbnail);
        }

        @Override
        public int getItemCount() {
            return positions.size();
        }


        /*package-private*/ class Holder extends RecyclerView.ViewHolder {
            private TextView user, position, activity;
            private ImageView thumbnail;

            /*package-private*/ Holder (View view) {
                super(view);
                user = view.findViewById(R.id.itemUser);
                position = view.findViewById(R.id.position);
                activity = view.findViewById(R.id.activity);
                thumbnail = view.findViewById(R.id.itemImage);
            }
        }
    }
}