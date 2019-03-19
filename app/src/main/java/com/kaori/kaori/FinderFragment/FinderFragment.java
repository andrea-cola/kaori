package com.kaori.kaori.FinderFragment;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.DataManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FinderFragment extends Fragment {

    /**
     * Constants
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Variables
     */
    private List<Position> positions;
    private FirebaseFirestore db;
    private RecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private Context context;
    private Date now;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.finder_fragment_layout, container, false);

        db = FirebaseFirestore.getInstance();
        context = getContext();
        positions = new ArrayList<>();

        view.findViewById(R.id.positionFAB).setOnClickListener(v -> invokeFragment(new SharePositionFragment()));

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        now = today.getTime();

        recyclerView = view.findViewById(R.id.user_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter(positions);
        recyclerView.setAdapter(adapter);

        ((TextView)view.findViewById(R.id.empty_view_text)).setText(R.string.finder_empty_view_text);

        loadActivePositions();

        return view;
    }

    private void loadActivePositions(){
        DataManager.getInstance().downloadCurrentActivePositions(recyclerView, view);
    }

    /**
     * This method invokes the share position fragment when the floating action button is clicked
     */
    private void invokeFragment(Fragment mapFragment) {
        if(getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mapFragment)
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
            View view = LayoutInflater.from(getContext()).inflate(R.layout.user_position_item, parent, false);

            view.setOnClickListener(view1 -> {
                MapFragment mapFragment = new MapFragment();
                mapFragment.setParameters(positions, recyclerView.getChildLayoutPosition(view));
                invokeFragment(mapFragment);
            });

            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, int i) {
            holder.user.setText(positions.get(i).getUser().getName());
            holder.activity.setText(positions.get(i).getActivity());
            //holder.position.setText(positions.get(i).getLocation());
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

