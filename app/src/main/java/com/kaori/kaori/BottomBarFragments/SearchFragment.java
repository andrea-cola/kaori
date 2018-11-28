package com.kaori.kaori.BottomBarFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class SearchFragment extends Fragment {

    /**
     * Constants
     */
    private ArrayList<String> titles;
    private ArrayList<String> authors;
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Views from layout
     */
    private SearchView searchView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    /**
     * Firebase storing constants
     */
    private Query set;
    private FirestoreRecyclerAdapter adapter;

    public SearchFragment(){ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_layout, container, false);

        // setting the items of the search_layout
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.searchList);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // setting the database
        titles = new ArrayList<>();
        authors = new ArrayList<>();
        set = FirebaseFirestore.getInstance().collection("books").orderBy("title", Query.Direction.DESCENDING);
        set.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        titles.add((String) document.get("title"));
                        authors.add((String) document.get("author"));
                    }
                }else{
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        // listener to the search view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseUserSeach(newText);
                return false;
            }
        });
        return view;
    }

    private void firebaseUserSeach(String sequence) {
        Query query = set;
        for(int i=0; i<titles.size(); i++) {
            if (titles.get(i).toLowerCase().contains(sequence.toLowerCase())) {
                query = set.whereEqualTo("title", titles.get(i));
            }
        }

        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .setLifecycleOwner(this)
                .build();

        adapter = new FirestoreRecyclerAdapter<Book, BookViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final BookViewHolder holder, int position, @NonNull Book model) {
                holder.setDetails(model.getTitle(), model.getAuthor());
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        invokeFragmentWithParams(holder.book_author.getText().toString(),holder.book_title.getText().toString(), "ok");
                    }
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

    /**
     * This method invokes the book fragment when the card is cliked
     */
    private void invokeFragmentWithParams(String author, String title, String updateDate) {
        Fragment bookFragment = new BookFragment();
        ((BookFragment) bookFragment).setParameters(author, title);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, bookFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
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
