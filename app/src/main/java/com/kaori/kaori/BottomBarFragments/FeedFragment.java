package com.kaori.kaori.BottomBarFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.DBObjects.Book;
import com.kaori.kaori.R;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class FeedFragment extends Fragment {

    /**
     * Variables.
     */
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Book> mBookList;
    private FloatingActionButton fab;
    private final String BACK_STATE_NAME = getClass().getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_layout, container, false);
        mBookList = new ArrayList<Book>();

        // Floating Action Button management for upload pdf files
        fab = view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadBookDialog uploadFragment = UploadBookDialog.newInstance();
                uploadFragment.show(getFragmentManager(), "Dialog Fragment");
            }
        });

        // put all books in the list, querying the database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String title = document.getString("title");
                        String author = document.getString("author");
                        String url = document.getString("url");
                        mBookList.add(new Book(title, author, url));
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }

                recyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
                recyclerView.setHasFixedSize(true);

                layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);

                mAdapter = new recyclerAdapter(mBookList);
                recyclerView.setAdapter(mAdapter);
            }
        });

        return view;
    }

    /**
     * Private Adapter for RecyclerView
     */
    private class recyclerAdapter extends RecyclerView.Adapter<recyclerAdapter.recyclerHolder> {

        ArrayList<Book> books;

        public recyclerAdapter(ArrayList<Book> bookList){
            this.books = bookList;
        }

        /**
         * This method inflates the CardView in the ReciclerView
         */
        @NonNull
        @Override
        public recyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            recyclerHolder holder = new recyclerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull final recyclerHolder holder, int i) {
            holder.author.setText(books.get(i).getAuthor());
            holder.title.setText(books.get(i).getTitle());

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    invokeFragmentWithParams(holder.author.getText().toString(),holder.title.getText().toString(), "ok");
                }
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        /**
         * Holder class contains the infos of the card and the book element selected or
         * the next book element in the recycler view
         */
        public class recyclerHolder extends RecyclerView.ViewHolder {
            public TextView title, author;
            public CardView cardView;
            //public ImageView image;

            public recyclerHolder(View view) {
                super(view);
                title = view.findViewById(R.id.cardTitle);
                author = view.findViewById(R.id.cardAuthor);
                cardView = view.findViewById(R.id.card_view);
                //image = (ImageView) view.findViewById(R.id.thumbnail);
            }
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
    }
}