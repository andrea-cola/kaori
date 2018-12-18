package com.kaori.kaori.FeedFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.Model.Professor;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class UploadMaterialFragment extends Fragment {

    /**
     * Constants
     */
    private final static int PICK_PDF_CODE = 2342;
    private final String GS_URL = "gs://kaori-c5a43.appspot.com";

    /**
     * Views from layout
     */
    private ChipGroup examsChipGroup, professorChipGroup;
    private EditText mTitle, mAuthor, mComment, mLink, mStatus;
    private StorageReference storage;
    private FirebaseFirestore db;
    private String exam, professor, flag;
    private Material newMaterial;
    private List<Professor> professors;
    private View view;
    private boolean isMaterialModified = false, validFields[] = new boolean[2];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upload_layout, container, false);
        storage = FirebaseStorage.getInstance().getReferenceFromUrl(GS_URL);
        db = FirebaseFirestore.getInstance();

        initializeView();
        loadProfessors();

        return view;
    }

    private void loadProfessors() {
        professors = new ArrayList<>();
        db.collection(Constants.DB_COLL_PROFESSORS)
                .whereEqualTo(Constants.FIELD_UNIVERSITY, DataManager.getInstance().getUser().getUniversity())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        for (QueryDocumentSnapshot document : task.getResult())
                            professors.add(document.toObject(Professor.class));
                    view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
                });
    }

    private void initializeView() {
        LinearLayout linearLayout = view.findViewById(R.id.linearlayout);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setUploadButton();

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            linearLayout.removeAllViews();

            initializeNewMaterial();

            RadioButton radioButton = view.findViewById(radioGroup1.getCheckedRadioButtonId());
            flag = String.valueOf(radioButton.getText());
            if (flag.equalsIgnoreCase(Constants.LIBRO))
                linearLayout.addView(inflater.inflate(R.layout.upload_libro_layout, null));
            else if (flag.equalsIgnoreCase(Constants.FILE))
                linearLayout.addView(inflater.inflate(R.layout.upload_file_layout, null));
            else
                linearLayout.addView(inflater.inflate(R.layout.upload_link_layout, null));

            initializeSubView();
        });
        radioGroup.check(R.id.radioButton);
    }

    private void initializeNewMaterial(){
        newMaterial = new Material();
        newMaterial.setUser(DataManager.getInstance().getMiniUser());
        newMaterial.setCourse(DataManager.getInstance().getUser().getCourse());
        newMaterial.setModified(isMaterialModified);
    }

    private void initializeSubView(){
        mTitle = view.findViewById(R.id.title);
        mAuthor = view.findViewById(R.id.author);
        mComment = view.findViewById(R.id.comment);
        mLink = view.findViewById(R.id.url);
        mStatus = view.findViewById(R.id.status);

        setExamChipGroup();
    }

    private void setUploadButton() {
        view.findViewById(R.id.button_ok).setOnClickListener(v -> {
            if (mTitle.getText().toString().equals("") || mComment.getText().toString().equals("")){
                Toast.makeText(getContext(), "Hai lasciato dei campi vuoti", Toast.LENGTH_SHORT).show();
            } else {
                if (flag.equalsIgnoreCase(Constants.LIBRO))
                    createNewBook();
                else if (flag.equalsIgnoreCase(Constants.FILE))
                    getPDF();
                else if(flag.equalsIgnoreCase(Constants.URL))
                    createNewLink();
            }
        });
    }

    private void createNewBook(){
        if (mAuthor.getText().toString().equals("") || mStatus.getText().toString().equals("")) {
            Toast.makeText(getContext(), "Hai lasciato dei campi vuoti", Toast.LENGTH_SHORT).show();
        }else{
            newMaterial.addProfessor(String.valueOf(mAuthor.getText()));
            String comment = "Lo stato è: " + String.valueOf(mStatus.getText()) + "\n\n" + "Il commento è: " + String.valueOf(mComment.getText());
            newMaterial.setType(flag);
            newMaterial.setComment(comment);

            writeDatabase();
        }
    }

    private void createNewFile(String url){
        newMaterial.setComment(String.valueOf(mComment.getText()));
        newMaterial.setType(flag);
        newMaterial.setUrl(url);
        writeDatabase();
    }

    private void createNewLink(){
        if (Patterns.WEB_URL.matcher(mLink.getText()).matches()){
            newMaterial.setType(flag);
            newMaterial.setUrl(String.valueOf(mLink.getText()));
            writeDatabase();
        } else {
            Toast.makeText(getContext(), "Url non valido", Toast.LENGTH_SHORT).show();
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

        StorageReference reference = storage.child(Constants.STORAGE_PATH_UPLOADS + DataManager.getInstance().getMiniUser().getName() + "_" + mTitle.getText().toString() + ".pdf");
        UploadTask task = reference.putFile(data);

        // register observers to listen for when the download is done or if it fails
        task.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();

            createNewFile(taskSnapshot.getUploadSessionUri().toString());

            getActivity().getFragmentManager().popBackStack();

            //end
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    /**
     * This method is used to upload the material in Firebase Firestore
     */
    private void writeDatabase(){
        newMaterial.setTitle(String.valueOf(mTitle.getText()));
        newMaterial.setTimestamp(Timestamp.now());
        db.collection(Constants.DB_COLL_MATERIALS)
            .add(newMaterial)
            .addOnSuccessListener(documentReference -> goBackToPreviousFragment())
            .addOnFailureListener(e -> LogManager.getInstance().printConsoleError("Error adding document: " + e.toString()));
    }

    /**
     * This method adds to the Chip Group related to exams in the View chips with exams names
     */
    private void setExamChipGroup() {
        examsChipGroup = view.findViewById(R.id.exams);
        for (String e : DataManager.getInstance().getUser().getExams()) {
            Chip chip = setChip(e);
            if (chip != null) {
                examsChipGroup.addView(chip);
                chip.setOnClickListener(view1 -> {
                    if (!newMaterial.getExams().contains(String.valueOf(chip.getText())))
                        newMaterial.addExam(String.valueOf(chip.getText()));

                    if (flag.equalsIgnoreCase(Constants.FILE) && chip.isCheckedIconVisible()) {
                        if (professorChipGroup==null) {
                            professorChipGroup = view.findViewById(R.id.professors);
                            setProfessorChipGroup(chip.getText().toString());
                        }
                        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (!buttonView.isChecked())
                                professorChipGroup.removeAllViews();
                            else
                                setProfessorChipGroup(chip.getText().toString());

                        });
                    }
                });
            }
        }
    }

    /**
     * This method adds to the Chip Group related to professor in the View chips with exams names
     */
    private void setProfessorChipGroup(String exam) {
        for (Professor professor : professors) {
            if (professor.getExam().equals(exam)) {
                Chip chip = setChip(professor.getName());
                professorChipGroup.addView(chip);
                chip.setOnClickListener(view -> {
                    if (!newMaterial.getProfessors().contains(String.valueOf(chip.getText())))
                        newMaterial.addProfessor(String.valueOf(chip.getText()));
                });
            }
        }
    }

    /**
     * This method sets up the chips on event listener
     */
    private Chip setChip(String text) {
        if (getContext()!= null) {
            Chip chip = new Chip(getContext());
            chip.setText(text);
            chip.setTextSize(getResources().getDimension(R.dimen.default_chip_text_size));
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setCloseIconVisible(false);
            return chip;
        }
        LogManager.getInstance().printConsoleError("CIAONE Error");
        return null;
    }

    /**
     * This method sets if the material has been modified
     */
    public void isMaterialModified(){
        isMaterialModified = true;
    }

    private void goBackToPreviousFragment(){
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

}