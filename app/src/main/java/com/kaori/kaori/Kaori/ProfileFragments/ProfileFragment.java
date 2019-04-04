package com.kaori.kaori.Kaori.ProfileFragments;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Kaori.MyMaterialFragments.MyMaterialFragment;
import com.kaori.kaori.MainActivity;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

public class ProfileFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();
    private DataManager hub;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout, container, false);
        setHasOptionsMenu(true);

        hub = DataManager.getInstance();
        ((TextView) view.findViewById(R.id.status_layout)).setText(hub.getUser().getName());
        ((TextView) view.findViewById(R.id.university)).setText(hub.getUser().getUniversity());
        ((TextView) view.findViewById(R.id.course)).setText(hub.getUser().getCourse());
        ((TextView) view.findViewById(R.id.profile_mail)).setText(hub.getUser().getEmail());

        setMyInfoView(view.findViewById(R.id.counterBooks), view.findViewById(R.id.counterDocs), view.findViewById(R.id.counterUrls));
        setStarredInfoView(view.findViewById(R.id.starredBooksCounter), view.findViewById(R.id.starredDocsCounter));

        // load profile image
        if(getContext() != null) {
            hub.loadImageIntoView(hub.getUser().getPhotosUrl(), view.findViewById(R.id.profile_image), getContext());
            hub.loadImageIntoBackgroundView(hub.getUser().getPhotosUrl(), view.findViewById(R.id.background_image), getContext());
        }

        // add click listener to logout button
        view.findViewById(R.id.logoutButton).setOnClickListener(view12 -> {
            FirebaseAuth.getInstance().signOut();

            // logout also from facebook
            if(hub.getUser().getAuthMethod() == Constants.FACEBOOK) {
                LogManager.getInstance().printConsoleMessage("Facebook logout");
                LoginManager.getInstance().logOut();
            }

            // flush data manager
            hub.clean();

            if(getActivity() != null) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });

        view.findViewById(R.id.profileMaterialCardButton).setOnClickListener(v -> invokeNextFragment(new StudyPlanFragment()));
        view.findViewById(R.id.uploadMaterialCardButton).setOnClickListener(v -> invokeNextFragment(new MyFilesFragment()));
        view.findViewById(R.id.starredMaterialCardButton).setOnClickListener(v -> invokeNextFragment(new MyMaterialFragment()));

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
                .addToBackStack(BACK_STATE_NAME)
                .commit();
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void setMyInfoView(TextView booksView, TextView docsView, TextView linksView){
        int books = 0, docs = 0, links = 0;

        for(Document doc : DataManager.getInstance().getMyFiles())
            if(doc.getSubtype() == Constants.BOOK)
                books++;
            else if(doc.getSubtype() == Constants.FILE)
                docs++;
            else
                links++;

        booksView.setText(Integer.toString(books));
        docsView.setText(Integer.toString(docs));
        linksView.setText(Integer.toString(links));
    }

    @SuppressLint("SetTextI18n")
    private void setStarredInfoView(TextView starredBooks, TextView starredDocs){
        starredBooks.setText(Integer.toString(hub.getUser().getBookStarred().size()));
        starredDocs.setText(Integer.toString(hub.getUser().getDocStarred().size()));
    }

}