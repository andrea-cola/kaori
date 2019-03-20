package com.kaori.kaori.ProfileFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.KaoriApp;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

public class ProfileFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout, container, false);
        setHasOptionsMenu(true);
        ((KaoriApp)getActivity()).hideBottomBar(false);

        // get views from layout.
        Button logout = view.findViewById(R.id.profile_logout_button);
        ImageView profileImageView = view.findViewById(R.id.profile_image);
        ImageView backgroundImageView = view.findViewById(R.id.background_image);
        TextView mName = view.findViewById(R.id.profile_name);
        TextView mUniversity = view.findViewById(R.id.profile_university);
        TextView mCourseType = view.findViewById(R.id.profile_course_type);
        TextView mMail = view.findViewById(R.id.profile_mail);

        // get the instance of Datahub
        DataManager hub = DataManager.getInstance();

        // load profile image
        if(getContext() != null) {
            DataManager.getInstance().loadImageIntoView(hub.getUser().getPhotosUrl(), profileImageView, getContext());
            DataManager.getInstance().loadImageIntoBackgroundView(hub.getUser().getPhotosUrl(), backgroundImageView, getContext());
        }

        // set the text in the text view.
        mName.setText(hub.getUser().getName());
        mMail.setText(hub.getUser().getEmail());
        mUniversity.setText(hub.getUser().getUniversity());
        mCourseType.setText(hub.getUser().getCourse());

        // add click listener to logout button
        logout.setOnClickListener(view12 -> {
            FirebaseAuth.getInstance().signOut();

            // logout also from facebook
            if(DataManager.getInstance().getUser().getAuthMethod() == Constants.FACEBOOK) {
                LogManager.getInstance().printConsoleMessage("Facebook logout");
                LoginManager.getInstance().logOut();
            }

            // flush data manager
            DataManager.getInstance().clean(getActivity());

            if(getActivity() != null && isAdded()) {
                startActivity(new Intent(getActivity(), Kaori.class));
                getActivity().finish();
            }
        });

        view.findViewById(R.id.piano_di_studi).setOnClickListener(view1 -> invokeNextFragment(new EditPlanFragment()));
        view.findViewById(R.id.my_uploads).setOnClickListener(view1 -> invokeNextFragment(new MyFilesFragment()));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.profile_action_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_info)
            return invokeNextFragment(new EditProfileInfo());
        return super.onOptionsItemSelected(item);
    }

    private boolean invokeNextFragment(Fragment fragment) {
        if(getActivity() != null && getActivity().getSupportFragmentManager() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        return true;
    }

}
