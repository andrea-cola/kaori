package com.kaori.kaori.Kaori.ProfileFragments;

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

import com.kaori.kaori.App;
import com.kaori.kaori.Kaori.ProfileFragments.UploadFragments.UploadBookFragment;
import com.kaori.kaori.Kaori.ProfileFragments.UploadFragments.UploadFragment;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.Kaori.ProfileFragments.UploadFragments.UploadDocumentFragment;
import com.kaori.kaori.Kaori.ProfileFragments.UploadFragments.UploadUrlFragment;
import com.kaori.kaori.R;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Services.DataManager;

import java.util.List;

public class MyFilesFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_uploads, container, false);
        setHasOptionsMenu(true);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ListAdapter());

        ((TextView)view.findViewById(R.id.empty_view_text)).setText(R.string.empty_text_my_files);
        App.setEmptyView(view.findViewById(R.id.empty_view));
        DataManager.getInstance().downloadMyFiles(recyclerView);

        view.findViewById(R.id.FAB).setOnClickListener(v -> invokeNextFragment(new UploadFragment()));

        return view;
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

        private List<Document> materials;

        /*package-private*/ ListAdapter() {
            materials = DataManager.getInstance().getMyFiles();
        }

        @NonNull
        @Override
        public ListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
            holder.title.setText(materials.get(position).getTitle());
            holder.date.setText(materials.get(position).getNote());
            holder.view.setOnClickListener(view -> {
                if(materials.get(position).getType() == Constants.BOOK) {
                    UploadBookFragment f = new UploadBookFragment();
                    f.setOldBook(materials.get(position));
                    invokeNextFragment(f);
                }
                else if(materials.get(position).getType() == Constants.URL) {
                    UploadUrlFragment f = new UploadUrlFragment();
                    f.setOldDocument(materials.get(position));
                    invokeNextFragment(f);
                }
                else {
                    UploadDocumentFragment f = new UploadDocumentFragment();
                    f.setOldDocument(materials.get(position));
                    invokeNextFragment(f);
                }
            });
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        /*package-private*/ class ListViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView date;
            View view;

            /*package-private*/ ListViewHolder(View v) {
                super(v);
                view = v;
                title = v.findViewById(R.id.chat_user);
                date = v.findViewById(R.id.date);
            }
        }
    }
}