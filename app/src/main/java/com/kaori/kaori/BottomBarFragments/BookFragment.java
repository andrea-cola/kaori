package com.kaori.kaori.BottomBarFragments;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.Constants;
import com.kaori.kaori.R;

import java.io.File;

public class BookFragment extends Fragment {

    /**
     * Constants
     */
    private String title;
    private String author;

    /**
     * View from layout
     */
    private TextView bookTitle;
    private TextView bookAuthor;
    private Button downloadButton;
    ProgressDialog progressDialog;

    /**
     * Firebase storing constants
     */
    private StorageReference storage;
    private String userName = "Pippo";
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    /**
     * Constructor
     */
    public BookFragment(){}

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.book_layout, container, false);

        // getting the views
        bookTitle = view.findViewById(R.id.bookTitle);
        bookAuthor = view.findViewById(R.id.bookAuthor);
        downloadButton = view.findViewById(R.id.downloadButton);

        // setting the views
        bookTitle.setText(this.title);
        bookAuthor.setText(this.author);

        // setting the Firebase objects
        storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://kaori-c5a43.appspot.com");
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // listener on the button
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadBook();
            }
        });

        return view;
    }

    public void setParameters(String author, String title){
        this.author = author;
        this.title = title;
    }

    private void downloadBook(){
        if(user == null){
            mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    StorageReference reference = storage.child(Constants.STORAGE_PATH_UPLOADS + userName + "_" + title + ".pdf");

                    // setting the dialog
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setTitle("Downloading...");
                    progressDialog.setMessage(null);
                    progressDialog.show();

                    // opening the pdf file
                    final File localFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), title + ".pdf" );
                    reference.getFile(localFile).
                            addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    progressDialog.dismiss();
                                    Intent openFile = new Intent(Intent.ACTION_VIEW);
                                    openFile.setDataAndType(FileProvider.getUriForFile(getContext(), "com.kaori.kaori.fileprovider", localFile), "application/pdf");
                                    openFile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    try {
                                        startActivity(openFile);
                                    }
                                    catch (ActivityNotFoundException e) {
                                        Toast.makeText(getContext(),"No Application Available to View PDF",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    progressDialog.setMessage("Downloaded " + (int) progress + "%..");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                }
            });

        }



    }
}
