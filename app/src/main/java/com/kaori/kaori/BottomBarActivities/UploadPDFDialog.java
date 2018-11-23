package com.kaori.kaori.BottomBarActivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaori.kaori.Constants;
import com.kaori.kaori.R;

import static android.app.Activity.RESULT_OK;

public class UploadPDFDialog extends DialogFragment {

    // this is the pic pdf code used in file chooser
    final static int PICK_PDF_CODE = 2342;

    // related view in the layout
    private EditText fileNameView;
    private Button updateButton;
    private ProgressBar progressBar;
    private TextView textProgress;

    // Firebase storing datas
    private StorageReference storage;
    private Uri filePath;

    // Empty constructor
    public UploadPDFDialog() { }

    public static UploadPDFDialog newInstance() {
        UploadPDFDialog dialog = new UploadPDFDialog();
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // setting the view
        View view = inflater.inflate(R.layout.upload_pdf_dialog, container, false);
        getDialog().setTitle("Upload pdf file");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // getting the firebase objects
        storage = FirebaseStorage.getInstance().getReference();

        // getting the views
        fileNameView = view.findViewById(R.id.fileName);
        updateButton = view.findViewById(R.id.uploadButton);
        progressBar = view.findViewById(R.id.progressUploadBar);

        // listener for the button
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButton.setBackgroundColor(Color.GREEN);
                getPDF();
            }
        });

        String newFile = fileNameView.getText().toString();
        // TODO = add new fileName to Firebase
        return view;
    }

    /**
     * Intent for create intent for file chooser and updated android versions
     */
    private void getPDF() {
        showFileChooser();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // when the user chooses the file
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
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
     * Creating an intent for file chooser
     */
    private void showFileChooser(){
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_PDF_CODE);
    }

    /**
     * To upload the file
     */
    public void uploadFile(Uri data){
        progressBar.setVisibility(View.VISIBLE);

        StorageReference reference = storage.child(Constants.STORAGE_PATH_UPLOADS + System.currentTimeMillis() + "." + filePath.getLastPathSegment());
        UploadTask task = reference.putFile(data);

        // Register observers to listen for when the download is done or if it fails
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressBar.setVisibility(View.GONE);
                textProgress.setVisibility(View.VISIBLE);
                textProgress.setText("File Uploaded Successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                textProgress.setVisibility(View.VISIBLE);
                textProgress.setText((int) progress + "% Uploading...");

            }
        });
    }


}
