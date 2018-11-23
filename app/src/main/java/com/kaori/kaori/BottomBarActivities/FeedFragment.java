package com.kaori.kaori.BottomBarActivities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.DBObjects.Book;
import com.kaori.kaori.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static android.support.constraint.Constraints.TAG;

public class FeedFragment extends Fragment {

    /**
     * Constants
     */
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Book> mBookList;
    private FloatingActionButton fab;
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Constructor
     */
    public FeedFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.feed_layout, container, false);
        mBookList = new ArrayList<Book>();

        //Floating Action Button managment for upload pdf files
        fab = view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadPDFDialog uploadFragment = UploadPDFDialog.newInstance();
                uploadFragment.show(getFragmentManager(), "Dialog Fragment");
            }
        });

        //put all books in the list, querying the database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("books").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String title = document.getString("title");
                        String author = document.getString("author");
                        mBookList.add(new Book(title, author));
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }

                recyclerView = (RecyclerView) getView().findViewById(R.id.my_recicler_view);
                recyclerView.setHasFixedSize(true);

                layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);

                mAdapter = new MyAdapter(mBookList);
                recyclerView.setAdapter(mAdapter);
            }
        });

        return view;
    }

    /**
     * Private Adapter for ReciclerView
     */
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        ArrayList<Book> books;

        public MyAdapter(ArrayList<Book> bookList){
            this.books = bookList;
        }

        /**
         * This method inflates the CardView in the ReciclerView
         */
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {
            holder.author.setText(books.get(i).getAuthor());
            holder.title.setText(books.get(i).getTitle());

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    invokeFragmentWithParams(holder.author.getText().toString(),holder.title.getText().toString(), "ok");
                }
            });
            //
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        /**
         * Holder class contains the infos of the card and the book element selected or
         * the next book element in the recycler view
         */
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title, author;
            public CardView cardView;
            //public ImageView image;

            public MyViewHolder(View view) {
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