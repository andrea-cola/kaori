package com.kaori.kaori.BottomBarFragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaori.kaori.Constants;
import com.kaori.kaori.DBObjects.Book;
import com.kaori.kaori.R;

import static android.app.Activity.RESULT_OK;
import static android.support.constraint.Constraints.TAG;

public class UploadBookDialog extends DialogFragment {

    // this is the pic pdf code used in file chooser
    final static int PICK_PDF_CODE = 2342;
    private final float PROGRESS_BAR_CONSTANT = 100f;

    /**
     * Views from layout
     */
    private EditText fileNameView;
    private Button updateButton;
    private ProgressBar progressBar;
    private TextView textProgress;

    /**
     * Firebase storing constants
     */
    private StorageReference storage;
    private FirebaseFirestore db;
    private String user = "Pippo";

    /**
     * Empty constructor
     */
    public UploadBookDialog() {
    }

    public static UploadBookDialog newInstance() {
        UploadBookDialog dialog = new UploadBookDialog();
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // setting the view
        View view = inflater.inflate(R.layout.upload_pdf_dialog, container, false);

        // getting the Firebase objects
        storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://kaori-c5a43.appspot.com");
        db = FirebaseFirestore.getInstance();

        // getting the views
        fileNameView = view.findViewById(R.id.fileName);
        textProgress = view.findViewById(R.id.textProgress);
        updateButton = view.findViewById(R.id.uploadButton);
        progressBar = view.findViewById(R.id.progressUploadBar);

        // listener for the button
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileNameView.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Input the file name", Toast.LENGTH_SHORT).show();
                }else{
                    updateButton.setBackgroundColor(Color.GREEN);
                    getPDF();

                }
            }
        });

        return view;
    }

    /**
     * Create intent for getting the pdf document
     */
    private void getPDF() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PDF_CODE);
    }

    /**
     * When the intent is returned for getting the pdf file
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // when the user chooses the file
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // if a file is selected
            if (data.getData() != null) {
                // uploading the file
                uploadFile(data.getData());
            } else {
                Toast.makeText(getContext(), "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * To upload the file
     */
    public void uploadFile(Uri data) {
        progressBar.setVisibility(View.VISIBLE);

        // TODO: get the current user
        StorageReference reference = storage.child(Constants.STORAGE_PATH_UPLOADS + user + "_" + fileNameView.getText().toString() + ".pdf");
        UploadTask task = reference.putFile(data);

        // register observers to listen for when the download is done or if it fails
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressBar.setVisibility(View.GONE);
                textProgress.setVisibility(View.VISIBLE);
                textProgress.setText("File Uploaded Successfully");

                // upload pdf file in Firebase database
                String fileName = fileNameView.getText().toString();

                // TODO: get the current user

                Book newBook = new Book();
                newBook.setTitle(fileName);
                newBook.setAuthor("pippo");
                newBook.setUrl(taskSnapshot.getUploadSessionUri().toString());
                db.collection("books")
                        .add(newBook)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                    }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
                getActivity().getFragmentManager().popBackStack();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (PROGRESS_BAR_CONSTANT * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                textProgress.setVisibility(View.VISIBLE);
                textProgress.setText((int) progress + "% Uploading...");
            }
        });
    }

}