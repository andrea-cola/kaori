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
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;

public class MaterialFragment extends Fragment {

    /**
     * Constants
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Views from layout
     */
    private View view;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton filterButton;
    private LinearLayout emptyView;
    private TextView emptyTextView;

    /**
     * Variables
     */
    private ArrayList<Material> materials;
    private Query set, mQuery;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private boolean isFiltered = false;

    /**
     * Constructor
     */
    public MaterialFragment(){ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_layout, container, false);
        setUpView();

        materials = new ArrayList<>();

        setUpFirebase();

        setUpButtons();

        if(isFiltered) {
            emptyView.setVisibility(View.INVISIBLE);
            setAdapter();
        }
        return view;
    }

    /**
     * This method sets up the Firebase db
     */
    private void setUpFirebase(){
        db = FirebaseFirestore.getInstance();
        set = db.collection("materials").orderBy("title", Query.Direction.DESCENDING);
        set.get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot document : task.getResult()){
                            materials.add(document.toObject(Material.class));
                        }
                    }else{
                        LogManager.getInstance().printConsoleError("Error getting documents: " + task.getException());
                    }
                });
    }

    /**
     * This method sets up the View
     */
    private void setUpView(){
        // setting the items of the search_layout
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.searchList);
        emptyView = view.findViewById(R.id.emptyView);
        emptyView.setVisibility(View.VISIBLE);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        emptyTextView.setVisibility(View.INVISIBLE);

        filterButton = view.findViewById(R.id.filterFAB);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    /**
     * This method sets up the buttons in the View
     */
    private void setUpButtons(){
        // listener to the search view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseSearch(newText);
                return false;
            }
        });

        filterButton.setOnClickListener(v -> {
            FilterFragment fragment = new FilterFragment();
            invokeFragment(fragment);
        });
    }

    /**
     * This method is used to search the title in the db
     * Filtering the materials according to the chips clicked
     */
    private void firebaseSearch(String sequence) {
        for (Material material : materials) {
            if (material.getTitle().toLowerCase().contains(sequence.toLowerCase())) {
                mQuery = set.whereEqualTo("title", material.getTitle());
            }
        }

        if(sequence.equals("")){
            recyclerView.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.VISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.INVISIBLE);
        }
        setAdapter();
    }


    private void setAdapter(){

        FirestoreRecyclerOptions<Material> options = new FirestoreRecyclerOptions.Builder<Material>()
                .setQuery(mQuery, Material.class)
                .setLifecycleOwner(this)
                .build();

        adapter = new FirestoreRecyclerAdapter<Material, BookViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final BookViewHolder holder, int position, @NonNull Material model) {
                holder.setDetails(model.getTitle(), model.getAuthor());
                holder.mView.setOnClickListener(v -> {
                    com.kaori.kaori.FeedFragments.MaterialFragment fragment = new com.kaori.kaori.FeedFragments.MaterialFragment();
                    fragment.setParameters(holder.book_author.getText().toString(), holder.book_title.getText().toString());
                    invokeFragment(fragment);
                    hideKeyboard();
                });
            }

            @NonNull
            @Override
            public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.search_list_item, parent, false);
                return new BookViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
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

    public void filterBy(String field, String o){
        mQuery = FirebaseFirestore.getInstance().collection("materials");

        if(!o.equals("")){
            mQuery = mQuery.whereEqualTo(field, o);
            isFiltered = true;
        }
    }

    /**
     * Private adapter for the RecyclerView
     */
    private class BookViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView book_title;
        TextView book_author;

        private BookViewHolder(View itemView) {
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
