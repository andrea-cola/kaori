package com.kaori.kaori.BottomBarFragments;

import android.app.FragmentTransaction;
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
import com.kaori.kaori.Utils.LogManager;

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
    private final String BACK_STATE_NAME = getClass().getName();
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
    private boolean validFields[] = new boolean[2];

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
        // set to false all the field validity flags.
        for (int i = 0; i < validFields.length; i++)
            validFields[i] = false;

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
                boolean flag = true;
                for(boolean field : validFields) flag = flag && field;
                if(!flag){
                    Toast.makeText(getContext(), "Controlla gli errori", Toast.LENGTH_SHORT).show();
                } else if(exams.isEmpty()){
                    Toast.makeText(getContext(), "Seleziona almeno un corso", Toast.LENGTH_SHORT).show();
                } else {
                    getPDF();
                }
            }
        });

        fileNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() < 1) {
                    fileNameView.setError("Hai lasciato il campo vuoto");
                    validFields[0] = false;
                } else
                    validFields[0] = true;
            }
        });

        commentView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() < 1) {
                    commentView.setError("Hai lasciato il campo vuoto");
                    validFields[1] = false;
                } else if (editable.length() > 30){
                    commentView.setError("Commento troppo lungo");
                    validFields[1] = false;
                }else
                    validFields[1] = true;
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
    private void uploadFile(Uri data) {
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
                                LogManager.getInstance().printConsoleMessage("DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                LogManager.getInstance().printConsoleError("Error adding document: " + e.toString());
                            }
                        });
                getActivity().getFragmentManager().popBackStack();
                returnToFeedFragment();
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

    private void returnToFeedFragment(){
        if(getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new FeedFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
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
        mBook.setComment(commentView.getText().toString());
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
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setCloseIconVisible(false);
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!exams.contains(chip.getText().toString()) && chip.isCheckedIconVisible()) {
                    exams.add(chip.getText().toString());
                }
            }
        });
        return chip;
    }

}