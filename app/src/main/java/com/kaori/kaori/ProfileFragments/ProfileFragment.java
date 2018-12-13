package com.kaori.kaori.ProfileFragments;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.DataManager;

import java.util.List;

/**
 * This class represents the Profile entry of the bottom bar menu.
 */
public class ProfileFragment extends Fragment {

    /**
     * Constants.
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Variables.
     */
    private ImageView profileImageView;
    private DataManager hub;
    private RecyclerView mExamsList;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout, container, false);
        setHasOptionsMenu(true);

        // get views from layout.
        Button editCourses = view.findViewById(R.id.profile_button_edit);
        Button logout = view.findViewById(R.id.profile_button_logout);
        profileImageView = view.findViewById(R.id.profile_image);
        TextView mName = view.findViewById(R.id.profile_name);
        TextView mUniversity = view.findViewById(R.id.profile_university);
        TextView mCourseType = view.findViewById(R.id.profile_course_type);
        mExamsList = view.findViewById(R.id.profile_exams_list);

        // get the instance of Datahub
        hub = DataManager.getInstance();

        // load profile image
        loadProfileImageView();

        mName.setText(hub.getUser().getName());
        mUniversity.setText(hub.getUser().getUniversity());
        mCourseType.setText(hub.getUser().getCourse());

        mExamsList.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mExamsList.setLayoutManager(mLayoutManager);
        mAdapter = new ListAdapter(hub.getUser().getExams());
        mExamsList.setAdapter(mAdapter);

        // attach listener to the logout button
        editCourses.setOnClickListener(view1 -> showEditExams());

        logout.setOnClickListener(view12 -> {
            FirebaseAuth.getInstance().signOut();
            if(getActivity() != null && isAdded()) {
                startActivity(new Intent(getActivity(), Kaori.class));
                getActivity().finish();
            }
        });

        return view;
    }

    /**
     * Add the menu to the layout.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.profile_action_bar, menu);
    }

    /**
     * Check the selection of the menu option.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_info)
            return showEditProfile();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the fragment that allows to edit the profile.
     */
    private boolean showEditProfile() {
        if(getActivity() != null && getActivity().getSupportFragmentManager() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new EditProfileInfo())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        return true;
    }

    /**
     * Show the fragment that allows to edit the exams.
     */
    private void showEditExams() {
        if(getActivity() != null & getActivity().getSupportFragmentManager() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new EditCoursesFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
    }

    /**
     * Load the image profile.
     */
    private void loadProfileImageView(){
        Glide.with(getContext())
                .load(hub.getUser().getPhotosUrl())
                .apply(hub.getGetGlideRequestOptionsCircle())
                .into(profileImageView);
    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

        private List<String> mDataset;

        class ListViewHolder extends RecyclerView.ViewHolder {
            TextView mTextView;
            ListViewHolder(View v) {
                super(v);
                mTextView = v.findViewById(R.id.simple_list_item_textview);
            }
        }

        ListAdapter(List<String> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ListAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_item, parent, false);
            return new ListViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ListViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mTextView.setText(mDataset.get(position));

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

}
