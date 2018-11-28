package com.kaori.kaori.ProfileFragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.Constants;
import com.kaori.kaori.DataHub;
import com.kaori.kaori.R;
import com.kaori.kaori.SplashScreen;

/**
 * This class represents the Profile entry of the bottom bar menu.
 */
public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private StorageReference ref;
    private DataHub hub;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout, container, false);

        // get views from layout.
        Button logout = view.findViewById(R.id.profile_button_logout);
        profileImageView = view.findViewById(R.id.profile_image);

        // get the instance of Datahub.
        hub = DataHub.getInstance();

        StorageReference s = FirebaseStorage.getInstance().getReference();

        Glide.with(getContext())
                .load(hub.getUser().getPhotosUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(profileImageView);


        FirebaseStorage.getInstance().getReference().child(hub.getUser().getPhotosUrl()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(Constants.TAG, uri.toString());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // TODO: handle any errors
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                if(getActivity() != null && isAdded()) {
                    startActivity(new Intent(getActivity(), SplashScreen.class));
                    getActivity().finish();
                }
                else{
                    // TODO: fare qualcosa
                }
            }
        });

        return view;
    }

    private void loadProfileImageView(){
        /*Glide.with(getContext())
            .load(uri.toString())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(profileImageView);*/
    }

}
