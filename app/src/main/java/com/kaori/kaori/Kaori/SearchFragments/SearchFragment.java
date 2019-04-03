package com.kaori.kaori.Kaori.SearchFragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaori.kaori.App;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Kaori.HomeFragments.MaterialActivity;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

import java.util.List;

public class SearchFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment_layout, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.searchList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerAdapter(DataManager.getInstance().getSearchElements()));

        App.setEmptyView(view.findViewById(R.id.empty_view));

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                view.findViewById(R.id.none_view).setVisibility(View.GONE);
                LogManager.getInstance().printConsoleMessage("print");
                DataManager.getInstance().queryMaterials(query, recyclerView);
                hideKeyboard(view);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s.equalsIgnoreCase("")) {
                    view.findViewById(R.id.none_view).setVisibility(View.VISIBLE);
                    App.setAuxiliarViewsStatus(Constants.NO_VIEW_ACTIVE);
                }
                return false;
            }

        });

        return view;
    }

    private void hideKeyboard(View view) {
        if(getActivity() != null)
            ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

        private List<Document> materials;

        RecyclerAdapter(List<Document> materials){
            this.materials = materials;
        }

        @NonNull
        @Override
        public RecyclerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            return new RecyclerAdapter.Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.Holder holder, int i) {
            holder.title.setText(materials.get(i).getTitle());
            holder.author.setText(materials.get(i).getUser().getName());

            String s = materials.get(i).getExams().get(0);
            for(int j = 1; j < materials.get(i).getExams().size(); j++)
                s = s + ", " + materials.get(i).getExams().get(j);
            holder.exams.setText(s);

            if(materials.get(i).getSubtype() == Constants.BOOK){
                holder.docIcon.setVisibility(View.GONE);
                holder.linkIcon.setVisibility(View.GONE);
                holder.bookIcon.setVisibility(View.VISIBLE);
            } else if(materials.get(i).getSubtype() == Constants.FILE){
                holder.bookIcon.setVisibility(View.GONE);
                holder.linkIcon.setVisibility(View.GONE);
                holder.docIcon.setVisibility(View.VISIBLE);
            } else {
                holder.docIcon.setVisibility(View.GONE);
                holder.bookIcon.setVisibility(View.GONE);
                holder.linkIcon.setVisibility(View.VISIBLE);
            }

            holder.view.setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), MaterialActivity.class);
                intent.putExtra("document", materials.get(i));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            private View view;
            private TextView title, author, exams;
            private ImageView docIcon, linkIcon, bookIcon;

            private Holder(View itemView) {
                super(itemView);
                view = itemView;
                title = view.findViewById(R.id.title);
                exams = view.findViewById(R.id.exams);
                author = view.findViewById(R.id.owner);
                docIcon = view.findViewById(R.id.label_doc);
                linkIcon = view.findViewById(R.id.label_url);
                bookIcon = view.findViewById(R.id.label_book);
            }
        }
    }

}
