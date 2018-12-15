package com.kaori.kaori.FinderFragment;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
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
    private Context context;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_position_layout, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.user_recycler_view);
        FloatingActionButton fab = view.findViewById(R.id.positionFAB);

        // set up button listener.
        fab.setOnClickListener(v -> invokeFragment(new SharePositionFragment()));

        // setting the database
        db = FirebaseFirestore.getInstance();
        context = getContext();
        uid = DataManager.getInstance().getUser().getUid();

        positions = new ArrayList<>();

        // set up recycler view.
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter(positions);
        recyclerView.setAdapter(adapter);

        loadActivePositions();

        return view;
    }

    private void loadActivePositions(){
        LogManager.getInstance().printConsoleMessage("loadActivePositions");
        db.collection(Constants.DB_COLL_POSITIONS).orderBy("timestamp").get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null)
                for (QueryDocumentSnapshot document : task.getResult()) {
                    LogManager.getInstance().printConsoleMessage("eccoci");
                    if(!String.valueOf(document.get("user.uid")).equalsIgnoreCase(uid)) {
                        positions.add(document.toObject(Position.class));
                        adapter.notifyDataSetChanged();
                    }
                }
        });
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

        List<Position> positions;

        /*package-private*/ RecyclerAdapter(List<Position> positions){
            this.positions = positions;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.user_position_item, parent, false);

            view.setOnClickListener(view1 -> {
                MapFragment mapFragment = new MapFragment();
                mapFragment.setParameters(positions.get(i));
                invokeFragment(mapFragment);
            });

            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, int i) {
            holder.user.setText(positions.get(i).getUser().getName());

            String s = Constants.STATEMENT + positions.get(i).getLocation();
            holder.message.setText(s);

            Glide.with(context).load(positions.get(i).getUser().getThumbnail())
                    .apply(RequestOptions.circleCropTransform()).into(holder.thumbnail);
        }

        @Override
        public int getItemCount() {
            return positions.size();
        }

        /*package-private*/ class Holder extends RecyclerView.ViewHolder {
            TextView user;
            TextView message;
            ImageView thumbnail;

            Holder (View view) {
                super(view);
                user = view.findViewById(R.id.itemUser);
                message = view.findViewById(R.id.itemMessage);
                thumbnail = view.findViewById(R.id.itemImage);
            }
        }
    }

}

