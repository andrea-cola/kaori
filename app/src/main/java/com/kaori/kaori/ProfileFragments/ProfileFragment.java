package com.kaori.kaori.ProfileFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.kaori.kaori.DataHub;
import com.kaori.kaori.R;
import com.kaori.kaori.SplashScreen;

/**
 * This class represents the Profile entry of the bottom bar menu.
 */
public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private DataHub hub;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout, container, false);

        // get views from layout.
        Button logout = view.findViewById(R.id.profile_button_logout);
        profileImageView = view.findViewById(R.id.profile_image);

        // get the instance of Datahub
        hub = DataHub.getInstance();

        // load profile image
        loadProfileImageView();

        // attach listener to the logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                if(getActivity() != null && isAdded()) {
                    startActivity(new Intent(getActivity(), SplashScreen.class));
                    getActivity().finish();
                }
            }
        });

        return view;
    }

    /**
     * Load profile image in the imageView.
     */
    private void loadProfileImageView(){
        Glide.with(getContext())
                .load(hub.getUser().getPhotosUrl())
                .apply(hub.getGetGlideRequestOptionsCircle())
                .into(profileImageView);
    }

}
