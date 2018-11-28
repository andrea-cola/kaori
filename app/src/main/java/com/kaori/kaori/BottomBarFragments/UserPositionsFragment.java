package com.kaori.kaori.BottomBarFragments;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.Constants;
import com.kaori.kaori.DBObjects.Book;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.R;

import java.util.ArrayList;

import retrofit2.http.Url;

import static android.support.constraint.Constraints.TAG;
import static android.support.v7.widget.OrientationHelper.HORIZONTAL;

public class UserPositionsFragment extends Fragment {

    /**
     * Views from layout
     */
    RecyclerView recyclerView;

    private Query users;
    private FirestoreRecyclerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_position_layout, container, false);

        // setting the parameters
        recyclerView = view.findViewById(R.id.user_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), HORIZONTAL));

        // setting the database
        users = FirebaseFirestore.getInstance().collection("users");
        users.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        User tmp = document.toObject(User.class);
                        Log.d(Constants.TAG, tmp.getName() + " " + tmp.getSurname() + " " + tmp.getPhotosUrl());
                    }
                }else{
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        populateRecyclerView();

        return view;
    }

    private void populateRecyclerView(){

        // Configure recycler adapter options:
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(users, new SnapshotParser<User>() {
                    @NonNull
                    @Override
                    public User parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        User user = new User();
                        user.setName((String) snapshot.get("name") + " " + snapshot.get("surname"));
                        return user;
                    }
                })
                .setLifecycleOwner(this)
                .build();

        adapter = new FirestoreRecyclerAdapter<User, UserPositionsFragment.UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final UserPositionsFragment.UserViewHolder holder, int position, @NonNull User model) {
                holder.setDetails(getContext(), model.getName() , "Hi, there! I'm studying here: ");
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(Constants.TAG, "MOSTRAMELA");
                    }
                });
            }

            @NonNull
            @Override
            public UserPositionsFragment.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.user_position_item, parent, false);
                return new UserPositionsFragment.UserViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
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
            mView = itemView;
        }

        private void setDetails(Context ctx, String userName , String message){

            itemUser = mView.findViewById(R.id.itemUser);
            itemMessage = mView.findViewById(R.id.itemMessage);
            itemImage = mView.findViewById(R.id.itemImage);

            itemUser.setText(userName);
            itemMessage.setText(message);

            //Glide.with(ctx).load(userImage).into(itemImage);

        }
    }
}

