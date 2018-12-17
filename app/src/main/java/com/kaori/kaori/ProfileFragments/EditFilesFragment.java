package com.kaori.kaori.ProfileFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.FeedFragments.UploadMaterialFragment;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
import java.util.List;

public class EditFilesFragment extends Fragment {

    /**
     * Constants.
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Variables.
     */
    private View view;
    private RecyclerView recyclerView;
    private List<Material> myUploads;
    private ListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_profile_uploads, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        myUploads = new ArrayList<>();
        adapter = new ListAdapter(myUploads);
        recyclerView.setAdapter(adapter);

        loadExams();

        return view;
    }

    private void loadExams() {
        FirebaseFirestore.getInstance().collection(Constants.DB_COLL_MATERIALS)
                .whereEqualTo(Constants.FIELD_AUTHOR, DataManager.getInstance().getUser().getName())
                .get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot result = task.getResult();
                    if (task.isSuccessful() && result != null)
                        for (QueryDocumentSnapshot document : result) {
                            myUploads.add(document.toObject(Material.class));
                            adapter.notifyDataSetChanged();
                        }
                    else
                        LogManager.getInstance().showVisualError(getContext(),null,"Impossibile caricare i corsi, riprovare.");
                    view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
                });
    }

    private void invokeNextFragment(Fragment fragment) {
        if(getActivity() != null && getActivity().getSupportFragmentManager() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
    }

    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

        private List<Material> mDataset;

        class ListViewHolder extends RecyclerView.ViewHolder {
            TextView mTextView;

            ListViewHolder(View v) {
                super(v);
                mTextView = v.findViewById(R.id.chat_user);
            }
        }

        ListAdapter(List<Material> materials) {
            mDataset = materials;
        }

        @NonNull
        @Override
        public ListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
            UploadMaterialFragment uploadBookFragment = new UploadMaterialFragment();

            v.setOnClickListener(view -> invokeNextFragment(uploadBookFragment));
            return new ListViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
            holder.mTextView.setText(mDataset.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

}