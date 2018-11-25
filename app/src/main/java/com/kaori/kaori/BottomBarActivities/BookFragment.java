package com.kaori.kaori.BottomBarActivities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaori.kaori.R;

public class BookFragment extends Fragment {

    private String title;
    private String author;

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
