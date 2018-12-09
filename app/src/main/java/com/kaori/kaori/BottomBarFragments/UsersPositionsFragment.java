package com.kaori.kaori.BottomBarFragments;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.DBObjects.Position;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.R;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class UsersPositionsFragment extends Fragment {

    /**
     * Views from layout
     */
    private RecyclerView recyclerView;
    private View view;
    private FloatingActionButton fab;

    /**
     * Constants
     */
    private String mStudy = "Hi, there! I'm studying here: ";
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Variables
     */
    private Query positions;
    private FirestoreRecyclerAdapter adapter;
    private ArrayList<User> users;
    private FirebaseFirestore db;
    private RecyclerView.LayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_position_layout, container, false);

        setUpView();

        // setting the database
        db = FirebaseFirestore.getInstance();
        positions = db.collection("positions");
        positions.get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
                populateRecyclerView();
        });

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot snapshot: task.getResult()) {
                            setUsersList(snapshot);
                        }
                    }else{
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        return view;
    }

    /**
     * This method is used to set up the view
     */
    private void setUpView(){
        // setting the parameters
        recyclerView = view.findViewById(R.id.user_recycler_view);
        fab = view.findViewById(R.id.positionFAB);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        users = new ArrayList<>();
    }

    private void setUpButtons(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeFragment(new SharePositionFragment());
            }
        });
    }

    /**
     * This method set up the list of the users from the Firebase Cloud
     */
    private void setUsersList(DocumentSnapshot snapshot){
        User user = new User();
        user.setUid((String) snapshot.get("uid"));
        user.setPhotosUrl((String) snapshot.get("photosUrl"));
        users.add(user);
    }

    /**
     * This method populate the recycler according to a Firebase query
     */
    private void populateRecyclerView(){

        // Configure recycler adapter options:
        FirestoreRecyclerOptions<Position> options = new FirestoreRecyclerOptions.Builder<Position>()
                .setQuery(positions, Position.class)
                .setLifecycleOwner(this)
                .build();

        // Creating the adapter
        adapter = new FirestoreRecyclerAdapter<Position, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final UserViewHolder holder, int position, @NonNull Position model) {
                holder.setDetails(getContext(), model.getUsername() , model.getLocation(), model.getUid());
                holder.mView.setOnClickListener(v -> {
                    MapFragment mapFragment = new MapFragment();
                    mapFragment.setParameters(model);
                    invokeFragment(mapFragment);
                });
            }
            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.user_position_item, parent, false);
                return new UserViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
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
     * Private adapter for the RecyclerView
     */
    private class UserViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView itemUser;
        TextView itemMessage;
        ImageView itemImage;

        private UserViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        // Setting the holder
        @SuppressLint("SetTextI18n")
        private void setDetails(Context ctx, String userName, String locationName, String mUid){

            itemUser = mView.findViewById(R.id.itemUser);
            itemMessage = mView.findViewById(R.id.itemMessage);
            itemImage = mView.findViewById(R.id.itemImage);

            itemUser.setText(userName);
            itemMessage.setText(mStudy + locationName);

            for(User user : users) {
                if (user.getUid().equals(mUid)) {
                    String userImage = user.getPhotosUrl();
                    Glide.with(ctx).load(userImage).apply(RequestOptions.circleCropTransform()).into(itemImage);
                }
            }
        }
    }
}

