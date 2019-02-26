package com.kaori.kaori.ProfileFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.KaoriApp;
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
    private List<Material> myUploads;
    private ListAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_profile_uploads, container, false);
        setHasOptionsMenu(true);
        ((KaoriApp)getActivity()).hideBottomBar(true);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        view.findViewById(R.id.FAB).setOnClickListener(view -> invokeNextFragment(new UploadFragment()));
        emptyTextView = view.findViewById(R.id.empty_view_text);
        emptyTextView.setText("Ops, non hai alcuna condivisione.");

        myUploads = new ArrayList<>();
        adapter = new ListAdapter(myUploads);
        recyclerView.setAdapter(adapter);

        loadExams();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.empty_menu, menu);
    }

    private void loadExams() {
        FirebaseFirestore.getInstance().collection(Constants.DB_COLL_MATERIALS)
                .get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot result = task.getResult();
                    if (task.isSuccessful() && result != null){
                        for (QueryDocumentSnapshot document : result) {
                            String uid = document.toObject(Material.class).getUser().getUid();
                            if (uid.equals(DataManager.getInstance().getUser().getUid())) {
                                myUploads.add(document.toObject(Material.class));
                            }
                        }
                        adapter.notifyDataSetChanged();
                        view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
                        if (myUploads.size() == 0)
                            view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                    } else {
                        view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
                        view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                        LogManager.getInstance().showVisualError(null, "Impossibile caricare i corsi, riprovare.");
                    }
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
            TextView title;
            TextView date;

            ListViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.chat_user);
                date = v.findViewById(R.id.date);
            }
        }

        ListAdapter(List<Material> materials) {
            mDataset = materials;
        }

        @NonNull
        @Override
        public ListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
            v.setOnClickListener(view -> {
                UploadFragment uploadBookFragment = new UploadFragment();
                uploadBookFragment.isMaterialModified(mDataset.get(recyclerView.getChildAdapterPosition(v)));
                invokeNextFragment(uploadBookFragment);
            });
            return new ListViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
            holder.title.setText(mDataset.get(position).getTitle());
            holder.date.setText(mDataset.get(position).getComment());
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}