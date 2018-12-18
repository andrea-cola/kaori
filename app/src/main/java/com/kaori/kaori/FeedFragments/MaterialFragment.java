package com.kaori.kaori.FeedFragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.BuildConfig;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.FileManager;

import java.io.File;

public class MaterialFragment extends Fragment {

    private StorageReference storage;
    private String title, author;

    public MaterialFragment(){}

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.book_layout, container, false);
        storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://kaori-c5a43.appspot.com");

        initializeView(view);

        return view;
    }


    public void setParameters(String author, String title){
        this.author = author;
        this.title = title;
    }

    private void initializeView(View view){
        TextView bookTitle = view.findViewById(R.id.bookTitle);
        TextView bookAuthor = view.findViewById(R.id.bookAuthor);

        Button downloadButton = view.findViewById(R.id.downloadButton);

        bookTitle.setText(title);
        bookAuthor.setText(author);
        downloadButton.setOnClickListener(v -> storage.child(Constants.STORAGE_PATH_UPLOADS + author + "_" + title + ".pdf").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                File pdfFile = new File(Constants.STORAGE_PATH + title + ".pdf");
                if (!pdfFile.exists()){
                    FileManager fileManager = new FileManager(title, uri, getActivity(), getContext());
                    if(fileManager.download())
                        show();
                } else
                    show();
            }
        }));

    }

    private void show() {
        File pdfFile = new File(Constants.STORAGE_PATH + title + ".pdf");
        if (pdfFile.exists() && getContext() != null) {
            Uri path = FileProvider.getUriForFile(getContext(),BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (getActivity() != null)
                startActivity(pdfIntent);
        }
    }
}