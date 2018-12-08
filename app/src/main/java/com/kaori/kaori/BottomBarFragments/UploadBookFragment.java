package com.kaori.kaori.BottomBarFragments;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaori.kaori.DBObjects.Book;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;

import java.nio.Buffer;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.support.constraint.Constraints.TAG;

public class UploadBookFragment extends Fragment {

    /**
     * Constants
     */
    final static int PICK_PDF_CODE = 2342;
    private final float PROGRESS_BAR_CONSTANT = 100f;
    private User currentUser = DataManager.getInstance().getUser();
    private static final float CHIP_TEXT_SIZE = 14;

    /**
     * Views from layout
     */
    private EditText fileNameView;
    private Button updateButton;
    private ProgressBar progressBar;
    private TextView textProgress;
    private EditText commentView;
    private CheckBox checkBox;
    private View view;
    private ChipGroup chipGroup;

    /**
     * Variables
     */
    private StorageReference storage;
    private FirebaseFirestore db;
    private ArrayList<String> exams;

    /**
     * Constructor
     */
    public UploadBookFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Setting the view
        view = inflater.inflate(R.layout.upload_pdf, container, false);

        // Getting the Firebase objects
        storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://kaori-c5a43.appspot.com");
        db = FirebaseFirestore.getInstance();
        exams = new ArrayList<>();
        setChipGroup();
        setUpView();
        setUpButton();

        return view;
    }

    /**
     * This method sets up the view
     */
    private void setUpView(){
        fileNameView = view.findViewById(R.id.fileName);
        textProgress = view.findViewById(R.id.textProgress);
        updateButton = view.findViewById(R.id.uploadButton);
        progressBar = view.findViewById(R.id.uploadProgressBar);
        progressBar.setVisibility(View.INVISIBLE);
        textProgress.setVisibility(View.INVISIBLE);
        checkBox = view.findViewById(R.id.uploadCheckBox);
        chipGroup = view.findViewById(R.id.uploadChipGroup);
        commentView = view.findViewById(R.id.commentView);
    }

    /**
     * This method sets up the button in the view
     */
    private void setUpButton(){
        // Listener for the button
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileNameView.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Input the file name", Toast.LENGTH_SHORT).show();
                } else if(chipGroup.getChildCount()>1){
                    Toast.makeText(getContext(), "Select at least one course", Toast.LENGTH_SHORT).show();
                } else if(commentView.getText().length()>30){
                    Toast.makeText(getContext(), "Write less the 30 characters", Toast.LENGTH_SHORT).show();
                } else {
                    updateButton.setBackgroundColor(Color.GREEN);
                    getPDF();
                }
            }
        });
    }

    /**
     * This method creates intent for getting the pdf document
     */
    private void getPDF() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PDF_CODE);
    }

    /**
     * This method is called when the intent is returned for getting the pdf file
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // When the user chooses the file
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            // If a file is selected
            if (data.getData() != null) {
                // Uploading the file
                uploadFile(data.getData());
            } else {
                Toast.makeText(getContext(), "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This method is used to upload the file in the Cloud
     */
    public void uploadFile(Uri data) {
        progressBar.setVisibility(View.VISIBLE);

        StorageReference reference = storage.child(Constants.STORAGE_PATH_UPLOADS + currentUser.getName() +  "_" + fileNameView.getText().toString() + ".pdf");
        UploadTask task = reference.putFile(data);

        // register observers to listen for when the download is done or if it fails
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressBar.setVisibility(View.GONE);
                textProgress.setVisibility(View.VISIBLE);
                textProgress.setText("File Uploaded Successfully");

                // upload pdf file in Firebase database
                Book book = createNewBook(taskSnapshot);

                db.collection("books")
                        .add(book)
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

    /**
     * This method set up the book in order to upload it to Firebase Firestore
     */
    private Book createNewBook(UploadTask.TaskSnapshot task){
        String fileName = fileNameView.getText().toString();
        Book mBook = new Book();
        mBook.setTitle(fileName);
        mBook.setAuthor(currentUser.getName());
        mBook.setUrl(task.getUploadSessionUri().toString());
        mBook.setSigned(checkBox.isChecked());
        mBook.setTimestamp(Timestamp.now());
        mBook.setCourses(exams);
        return mBook;
    }

    /**
     * This method adds to the Chip Group in the View chips with exams names
     */
    private void setChipGroup(){
        db.collection("exams")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                chipGroup.addView(setChip(document.getString("name")));
                            }

                        }
                    }
                });
        return;
    }

    /**
     * This method sets up the chips on event listener
     */
    private Chip setChip(String text){
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setTextSize(CHIP_TEXT_SIZE);
        chip.setCheckable(false);
        chip.setClickable(true);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.removeView(chip);
            }
        });
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!exams.contains(chip.getText().toString()))
                    exams.add(chip.getText().toString());
            }
        });
        return chip;
    }

}