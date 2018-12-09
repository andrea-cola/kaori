package com.kaori.kaori.BottomBarFragments;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.BuildConfig;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.FileManager;

import java.io.File;

import retrofit2.http.Url;

public class BookFragment extends Fragment {

    /**
     * Constants
     */
    private String pathname = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator;

    /**
     * View from layout
     */
    private TextView bookTitle;
    private TextView bookAuthor;
    private Button downloadButton;
    private View view;

    /**
     * Variables
     */
    private StorageReference storage;
    private String title;
    private String author;

    /**
     * Constructor
     */
    public BookFragment(){}

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.book_layout, container, false);

        // setting the Firebase objects
        storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://kaori-c5a43.appspot.com");

        setUpView();

        setUpButtons();

        return view;
    }

    /**
     * This method sets up the View
     */
    private void setUpView(){
        bookTitle = view.findViewById(R.id.bookTitle);
        bookAuthor = view.findViewById(R.id.bookAuthor);
        downloadButton = view.findViewById(R.id.downloadButton);

        bookTitle.setText(this.title);
        bookAuthor.setText(this.author);
    }

    /**
     * This method sets up the buttons from the view
     */
    private void setUpButtons(){
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storage.child(Constants.STORAGE_PATH_UPLOADS + author + "_" + title + ".pdf").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        File pdfFile = new File(pathname + title + ".pdf");
                        if (!pdfFile.exists()){
                            FileManager fileManager = new FileManager(title, uri, getActivity(), getContext());
                            boolean downloaded = fileManager.download();
                            if(downloaded)
                                show();
                        }else
                            show();
                    }
                });
            }
        });
    }

    public void setParameters(String author, String title){
        this.author = author;
        this.title = title;
    }

    private void show() {
        File pdfFile = new File(pathname + title + ".pdf");
        if (pdfFile.exists()) {
            Uri path = FileProvider.getUriForFile(getContext(),BuildConfig.APPLICATION_ID + ".fileprovider",pdfFile);
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (getActivity() != null)
                startActivity(pdfIntent);
        }
    }
}