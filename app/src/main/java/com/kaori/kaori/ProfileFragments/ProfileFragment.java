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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.kaori.kaori.DataHub;
import com.kaori.kaori.R;
import com.kaori.kaori.Kaori;

import java.util.List;

/**
 * This class represents the Profile entry of the bottom bar menu.
 */
public class ProfileFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();

    private ImageView profileImageView;
    private DataHub hub;
    private TextView mName, mUniversity, mCourseType;
    private RecyclerView mExamsList;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout, container, false);

        // get views from layout.
        Button editCourses = view.findViewById(R.id.profile_button_edit);
        Button logout = view.findViewById(R.id.profile_button_logout);
        profileImageView = view.findViewById(R.id.profile_image);
        mName = view.findViewById(R.id.profile_name);
        mUniversity = view.findViewById(R.id.profile_university);
        mCourseType = view.findViewById(R.id.profile_course_type);
        mExamsList = view.findViewById(R.id.profile_exams_list);

        // get the instance of Datahub
        hub = DataHub.getInstance();

        // load profile image
        loadProfileImageView();

        mName.setText(hub.getUser().getName() + " " + hub.getUser().getSurname());
        mUniversity.setText(hub.getUser().getUniversity());
        mCourseType.setText(hub.getUser().getCourseType());

        mExamsList.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mExamsList.setLayoutManager(mLayoutManager);
        mAdapter = new ListAdapter(hub.getUser().getExams());
        mExamsList.setAdapter(mAdapter);

        // attach listener to the logout button
        editCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new EditCoursesFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack(BACK_STATE_NAME)
                        .commit();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                if(getActivity() != null && isAdded()) {
                    startActivity(new Intent(getActivity(), Kaori.class));
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
