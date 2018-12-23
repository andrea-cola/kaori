package com.kaori.kaori.MaterialFragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kaori.kaori.FeedFragments.MaterialFragment;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    /**
     * Constants
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Views from layout
     */
    private View view;
    private SearchView searchView;
    private FloatingActionButton filterButton;

    /**
     * Variables
     */
    private ArrayList<Material> materials;
    private ArrayList<Material> subMaterials;
    private RecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;

    /**
     * Constructor
     */
    public SearchFragment(){ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_layout, container, false);
        materials = new ArrayList<>();

        if(DataManager.getInstance().getAllMaterials().size() > 0)
            materials = DataManager.getInstance().getAllMaterials();
        else
            downloadMaterialFromDatabase();
        setupView();
        setupButtons();

        return view;
    }

    private void downloadMaterialFromDatabase(){
        FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_MATERIALS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult())
                            materials.add(document.toObject(Material.class));
                        DataManager.getInstance().setAllMaterials(materials);
                    } else {
                        view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                        LogManager.getInstance().showVisualError(task.getException(), getString(R.string.generic_error));
                    }
                });
    }

    /**
     * This method sets up the View
     */
    private void setupView(){
        subMaterials = new ArrayList<>();
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.searchList);
        //filterButton = view.findViewById(R.id.filterFAB);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new RecyclerAdapter(subMaterials);
        recyclerView.setAdapter(recyclerAdapter);
        view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
    }

    /**
     * This method sets up the buttons in the View
     */
    private void setupButtons(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseSearch(newText);
                return false;
            }
        });

        //filterButton.setOnClickListener(v -> invokeFragment(new FilterFragment()));
    }

    /**
     * This method is used to search the title in the db
     * Filtering the materials according to the chips clicked
     */
    private void firebaseSearch(String sequence) {
        if(sequence.length() > 0) {
            subMaterials.clear();
            recyclerAdapter.notifyDataSetChanged();

            for(Material m : materials)
                if (m.getTitle().toLowerCase().contains(sequence.toLowerCase())
                        || m.getCourse().toLowerCase().contains(sequence.toLowerCase())
                        || containsExams(m, sequence)
                        || containsProfessor(m, sequence)) {
                    subMaterials.add(m);
                    recyclerAdapter.notifyDataSetChanged();
                }

            view.findViewById(R.id.empty_view).setVisibility(subMaterials.size() == 0 ? View.VISIBLE : View.GONE);
        } else
            view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
    }

    private boolean containsExams(Material m, String sequence){
        for(String e : m.getExams())
            if(e.toLowerCase().contains(sequence.toLowerCase()))
                return true;
        return false;
    }

    private boolean containsProfessor(Material m, String sequence){
        for(String e : m.getProfessors())
            if(e.toLowerCase().contains(sequence.toLowerCase()))
                return true;
        return false;
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

        List<Material> materials;

        /*package-private*/ RecyclerAdapter(List<Material> materials){
            this.materials = materials;
        }

        @NonNull
        @Override
        public RecyclerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list_item, parent, false);
            v.setOnClickListener(view -> {
                MaterialFragment materialFragment = new MaterialFragment();
                materialFragment.setMaterial(this.materials.get(recyclerView.getChildAdapterPosition(v)));
                invokeFragment(materialFragment);
            });
            return new RecyclerAdapter.Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.Holder holder, int i) {
            holder.setDetails(materials.get(i).getTitle(), materials.get(i).getCourse());
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        /**
         * Private adapter for the RecyclerView
         */
        class Holder extends RecyclerView.ViewHolder {
            View mView;
            TextView book_title;
            TextView book_author;

            private Holder(View itemView) {
                super(itemView);
                mView = itemView;
            }

            private void setDetails(String bookTitle, String bookAuthor){
                book_title = mView.findViewById(R.id.itemTitle);
                book_author = mView.findViewById(R.id.itemAuthor);

                book_title.setText(bookTitle);
                book_author.setText(bookAuthor);
            }
        }
    }

}
