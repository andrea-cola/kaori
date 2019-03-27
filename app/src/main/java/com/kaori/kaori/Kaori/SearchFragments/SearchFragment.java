package com.kaori.kaori.Kaori.SearchFragments;

import android.app.Activity;
import android.app.FragmentTransaction;
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
import android.widget.TextView;

import com.kaori.kaori.App;
import com.kaori.kaori.Kaori.HomeFragments.MaterialFragment;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;

import java.util.List;

/**
 * Search Fragment is used to search materials.
 */
public class SearchFragment extends Fragment {

    /**
     * Constants
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Views from layout
     */
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_fragment_layout, container, false);
        view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

        RecyclerView recyclerView = view.findViewById(R.id.searchList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerAdapter(DataManager.getInstance().getSearchElements()));

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ((TextView)view.findViewById(R.id.empty_view_text)).setText(R.string.empty_text_search);
                App.setEmptyView(view.findViewById(R.id.empty_view));
                DataManager.getInstance().queryMaterials(query, recyclerView);
                hideKeyboard();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }

        });

        return view;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * This method invokes the book fragment when the card is clicked
     */
    private void invokeFragment(Fragment fragment) {
        if(getActivity()!= null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

        private List<Document> materials;

        /*package-private*/ RecyclerAdapter(List<Document> materials){
            this.materials = materials;
        }

        @NonNull
        @Override
        public RecyclerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            return new RecyclerAdapter.Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.Holder holder, int i) {
            holder.setDetails(materials.get(i).getTitle(), materials.get(i).getUser().getName());
            holder.mView.setOnClickListener(view -> {
                MaterialFragment materialFragment = new MaterialFragment();
                materialFragment.setMaterial(materials.get(i));
                invokeFragment(materialFragment);
            });
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        /**
         * Private adapter for the RecyclerView
         */
        /*package-private*/ class Holder extends RecyclerView.ViewHolder {
            private View mView;
            private TextView title, author, course;

            private Holder(View itemView) {
                super(itemView);
                mView = itemView;
            }

            private void setDetails(String t, String a){ //, String c){
                title = mView.findViewById(R.id.title);
                author = mView.findViewById(R.id.author);
                //course = mView.findViewById(R.id.course);

                title.setText(t);
                author.setText(a);
                //course.setText(c);
            }
        }
    }

}
