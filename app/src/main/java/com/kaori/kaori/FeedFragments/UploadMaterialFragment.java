package com.kaori.kaori.FeedFragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import static android.app.Activity.RESULT_OK;

public class UploadMaterialFragment extends Fragment {

    /**
     * Constants
     */
    private final static int PICK_PDF_CODE = 2342;
    private User currentUser = DataManager.getInstance().getUser();

    /**
     * Views from layout
     */
    private Button updateButton;
    private EditText fileNameView, commentView, linkView ;
    private CheckBox checkBox;
    private View view;
    private ChipGroup examsChipGroup, professorChipGroup;

    /**
     * Variables
     */
    private StorageReference storage;
    private FirebaseFirestore db;
    private String exam;
    private String professor;
    private boolean validFields[] = new boolean[2];
    private boolean isMaterialModified = false;

    /**
     * Constructor
     */
    public UploadMaterialFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upload_pdf, container, false);

        storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://kaori-c5a43.appspot.com");
        db = FirebaseFirestore.getInstance();

        for (int i = 0; i < validFields.length; i++)
            validFields[i] = false;

        initializeView();

        setupButtonListeners();

        setExamChipGroup();

        setProfessorChipGroup();

        return view;
    }

    /**
     * This method sets up the view
     */
    private void initializeView() {
        fileNameView = view.findViewById(R.id.fileName);
        updateButton = view.findViewById(R.id.uploadButton);
        linkView = view.findViewById(R.id.linkEditText);
        checkBox = view.findViewById(R.id.uploadCheckBox);
        examsChipGroup = view.findViewById(R.id.uploadExamsChipGroup);
        examsChipGroup.setSingleSelection(true);
        professorChipGroup = view.findViewById(R.id.uploadProfessorsChipGroup);
        professorChipGroup.setSingleSelection(true);
        commentView = view.findViewById(R.id.commentView);
    }

    private void setupButtonListeners() {
        updateButton.setOnClickListener(v -> {
            boolean flag = true;
            for (boolean field : validFields) flag = flag && field;
            if (!flag)
                Toast.makeText(getContext(), "Controlla gli errori", Toast.LENGTH_SHORT).show();
            else if (exam.equals(""))
                Toast.makeText(getContext(), "Seleziona un corso", Toast.LENGTH_SHORT).show();
            else if (professor.equals(""))
                Toast.makeText(getContext(), "Seleziona un professore", Toast.LENGTH_SHORT).show();
            else
                checkUploadLink();
        });

        fileNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

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
                } else if (editable.length() > 30) {
                    commentView.setError("Commento troppo lungo");
                    validFields[1] = false;
                } else
                    validFields[1] = true;
            }
        });
    }

    /**
     * This methos is used to check the link url: if it is valid upload it in the database,
     * otherwise the user upload a saved pdf
     */
    private void checkUploadLink(){
        if(linkView.getText().equals("")){
            getPDF();
        }else{
            if(Patterns.WEB_URL.matcher(linkView.getText()).matches()){
                uploadFileIntoDb(Uri.parse(linkView.getText().toString()));
            }else {
                Toast.makeText(getContext(), "Url non valido", Toast.LENGTH_SHORT).show();
            }
        }
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
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null)
            if (data.getData() != null)
                uploadFileIntoStorage(data.getData());
            else
                Toast.makeText(getContext(), "No file chosen", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method is used to upload the file in the Cloud
     */
    private void uploadFileIntoStorage(Uri data) {
        ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Uploading...", true);

        StorageReference reference = storage.child(Constants.STORAGE_PATH_UPLOADS + currentUser.getName() + "_" + fileNameView.getText().toString() + ".pdf");
        UploadTask task = reference.putFile(data);

        // register observers to listen for when the download is done or if it fails
        task.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();

            uploadFileIntoDb(taskSnapshot.getUploadSessionUri());

            getActivity().getFragmentManager().popBackStack();
            returnToFeedFragment();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    /**
     * This method is used to upload the material in Firebase Firestore
     */
    private void uploadFileIntoDb(Uri uploadUri){
        Material material = createNewBook(uploadUri);

        db.collection(Constants.DB_COLL_MATERIALS)
                .add(material)
                .addOnSuccessListener(documentReference -> LogManager.getInstance().printConsoleMessage("DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> LogManager.getInstance().printConsoleError("Error adding document: " + e.toString()));
    }


    /**
     * This method is used to return to the feed fragment
     */
    private void returnToFeedFragment() {
        if (getActivity() != null)
            getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    /**
     * This method set up the book in order to checkUploadLink it to Firebase Firestore
     */
    private Material createNewBook(Uri uploadUri) {
        return new Material(String.valueOf(fileNameView.getText()), currentUser.getName(), String.valueOf(uploadUri),
                checkBox.isChecked() ? "file" : "book", Timestamp.now(), exam, currentUser.getCourse(),
                professor, String.valueOf(commentView.getText()), currentUser.getPhotosUrl(), isMaterialModified);
    }

    /**
     * This method adds to the Chip Group related to exams in the View chips with exams names
     */
    private void setExamChipGroup() {
        for(String e : currentUser.getExams()){
            Chip chip = setChip(new Chip(getContext()), e);
            examsChipGroup.addView(chip);
            chip.setOnClickListener(v -> {
                professorChipGroup.removeAllViews();
                if (chip.isChecked()) {
                    exam = chip.getText().toString();
                    setProfessorChipGroup();
                }
            });
        }
    }

    /**
     * This method adds to the Chip Group related to professor in the View chips with exams names
     */
    private void setProfessorChipGroup() {
        db.collection(Constants.DB_COLL_PROFESSORS)
                .whereEqualTo(Constants.FIELD_EXAM, exam)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Chip chip = new Chip(getContext());
                            professorChipGroup.addView(setChip(chip, document.getString(Constants.FIELD_NAME)));
                            chip.setOnClickListener(v -> {
                                if(chip.isChecked())
                                    professor = chip.getText().toString();
                            });
                        }
                    }
                });
    }

    /**
     * This method sets up the chips on event listener
     */
    private Chip setChip(Chip chip, String text) {
        chip.setText(text);
        chip.setTextSize(getResources().getDimension(R.dimen.default_chip_text_size));
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setCloseIconVisible(false);
        return chip;
    }

    public void isMaterialModified(){
        isMaterialModified = true;
    }
}