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

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();
    private RecyclerView mExamsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout, container, false);
        setHasOptionsMenu(true);

        // get views from layout.
        Button logout = view.findViewById(R.id.profile_button_logout);
        ImageView profileImageView = view.findViewById(R.id.profile_image);
        TextView mName = view.findViewById(R.id.profile_name);
        TextView mUniversity = view.findViewById(R.id.profile_university);
        TextView mCourseType = view.findViewById(R.id.profile_course_type);
        mExamsList = view.findViewById(R.id.profile_exams_list);

        // get the instance of Datahub
        DataManager hub = DataManager.getInstance();

        // load profile image
        if(getContext() != null)
            Glide.with(getContext())
                    .load(hub.getUser().getPhotosUrl())
                    .apply(hub.getGetGlideRequestOptionsCircle())
                    .into(profileImageView);

        // set the text in the text view.
        mName.setText(hub.getUser().getName());
        mUniversity.setText(hub.getUser().getUniversity());
        mCourseType.setText(hub.getUser().getCourse());

        // setup the recycler view
        mExamsList.setHasFixedSize(true);
        mExamsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mExamsList.setAdapter(new ListAdapter());

        // add click listener to logout button
        logout.setOnClickListener(view12 -> {
            DataManager.getInstance().clean();
            FirebaseAuth.getInstance().signOut();
            if(getActivity() != null && isAdded()) {
                startActivity(new Intent(getActivity(), Kaori.class));
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.profile_action_bar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_info)
            return invokeNextFragment(new EditProfileInfo());
        return super.onOptionsItemSelected(item);
    }

    /**
     * Invoke the next fragment.
     */
    private boolean invokeNextFragment(Fragment fragment) {
        if(getActivity() != null && getActivity().getSupportFragmentManager() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        return true;
    }

    /**
     * Recycler view adapter.
     */
    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

        private List<String> mDataset;

        /*package-private*/ class ListViewHolder extends RecyclerView.ViewHolder {
            TextView mTextView;

            ListViewHolder(View v) {
                super(v);
                mTextView = v.findViewById(R.id.chat_user);
            }
        }

        private ListAdapter() {
            mDataset = new ArrayList<>();
            mDataset.add(getString(R.string.my_exams));
            mDataset.add(getString(R.string.my_uploads));
        }

        @NonNull
        @Override
        public ListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
            v.setOnClickListener(view -> invokeNextFragment(mExamsList.getChildLayoutPosition(v) == 0 ? new EditCoursesFragment() : new EditFilesFragment()));
            return new ListViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
            holder.mTextView.setText(mDataset.get(position));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

}
