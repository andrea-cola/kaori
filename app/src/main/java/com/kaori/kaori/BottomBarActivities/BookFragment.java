package com.kaori.kaori.BottomBarActivities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaori.kaori.DBObjects.Book;
import com.kaori.kaori.R;

import static android.support.constraint.Constraints.TAG;

public class BookFragment extends Fragment {
    private String title;
    private String author;

    public BookFragment(){}

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.book_layout, container, false);
        TextView bookTitle = view.findViewById(R.id.bookTitle);
        TextView bookAuthor = view.findViewById(R.id.bookAuthor);
        bookTitle.setText(this.title);
        bookAuthor.setText(this.author);
        return view;
    }

    public void setParameters(String author, String title){
        this.author = author;
        this.title = title;
    }


}
