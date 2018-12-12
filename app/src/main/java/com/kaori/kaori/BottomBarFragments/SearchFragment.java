package com.kaori.kaori.BottomBarFragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.DBObjects.Book;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;

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
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ChipGroup chipGroup;
    private FloatingActionButton filterButton;
    private LinearLayout emptyView;
    private TextView emptyTextView;

    /**
     * Variables
     */
    private ArrayList<Book> books;
    private ArrayList<String> exams;
    private Query set;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private boolean clicked;

    /**
     * Constructor
     */
    public SearchFragment(){ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_layout, container, false);
        setUpView();

        books = new ArrayList<>();
        exams = new ArrayList<>();

        setUpFirebase();

        setUpButtons();

        return view;
    }

    /**
     * This method sets up the Firebase db
     */
    private void setUpFirebase(){
        db = FirebaseFirestore.getInstance();
        set = db.collection("books").orderBy("title", Query.Direction.DESCENDING);
        set.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                       books.add(document.toObject(Book.class));
                    }
                }else{
                    LogManager.getInstance().printConsoleError("Error getting documents: " + task.getException());
                }
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
        chipGroup = view.findViewById(R.id.searchChipGroup);
        emptyView = view.findViewById(R.id.emptyView);
        emptyView.setVisibility(View.VISIBLE);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        emptyTextView.setVisibility(View.INVISIBLE);

        filterButton = view.findViewById(R.id.filterButton);

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

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeFragment(new FilterFragment());
            }
        });
    }

    /**
     * This method is used to search the title in the db
     * Filtering the books according to the chips clicked
     */
    private void firebaseSearch(String sequence) {
        Query query = set;

//        if(!exams.isEmpty()) {
//            for (Book book : books) {
//                if (book.getCourses().containsAll(exams)) {
//                    query = set.whereEqualTo("title", book.getTitle());
//                }
//            }
//        }else{
//            for (Book book : books) {
//                if (book.getTitle().toLowerCase().contains(sequence.toLowerCase())) {
//                    query = set.whereEqualTo("title", book.getTitle());
//                }
//            }
//        }

        if(sequence.equals("") || query.equals(set)){
            recyclerView.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.VISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.INVISIBLE);
        }

        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .setLifecycleOwner(this)
                .build();

        adapter = new FirestoreRecyclerAdapter<Book, BookViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final BookViewHolder holder, int position, @NonNull Book model) {
                holder.setDetails(model.getTitle(), model.getAuthor());
                holder.mView.setOnClickListener(v -> {
                    BookFragment fragment = new BookFragment();
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
        if(getActivity()!= null && isAdded()) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
    }

    public void filterBookQuery(String exam, String professor, String course){

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
