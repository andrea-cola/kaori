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

import com.kaori.kaori.Model.Material;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.DataManager;

import java.util.List;

public class EditFilesFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.edit_profile_uploads, container, false);
        setHasOptionsMenu(true);

        recyclerView = view1.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ListAdapter(DataManager.getInstance().getMyFiles()));

        view1.findViewById(R.id.FAB).setOnClickListener(view -> invokeNextFragment(new UploadFragment()));
        TextView emptyTextView = view1.findViewById(R.id.empty_view_text);
        emptyTextView.setText("Ops, non hai alcuna condivisione.");

        DataManager.getInstance().loadMyFiles(recyclerView, view1);

        return view1;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.empty_menu, menu);
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

        /*package-private*/  ListAdapter(List<Material> materials) {
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

        /*package-private*/ class ListViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView date;

            /*package-private*/  ListViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.chat_user);
                date = v.findViewById(R.id.date);
            }
        }
    }
}