package com.kaori.kaori.ProfileFragments;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.Model.Professor;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class UploadFragment extends Fragment {

    /**
     * Constants
     */
    private final String BACK_STATE_NAME = getClass().getName();
    private final static int PICK_PDF_CODE = 2342;

    /**
     * Views from layout
     */
    private ChipGroup examsChipGroup, professorChipGroup;
    private EditText mTitle, mAuthor, mComment, mLink, mStatus;
    private FirebaseFirestore db;
    private String exam, professor, tag;
    private Material newMaterial;
    private List<Professor> professors;
    private RadioGroup radioGroup;
    private View view;
    private boolean isMaterialModified = false, validFields[] = new boolean[2];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upload_layout, container, false);
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

        radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            linearLayout.removeAllViews();

            if(!isMaterialModified)
                initializeNewMaterial();

            RadioButton radioButton = view.findViewById(radioGroup1.getCheckedRadioButtonId());
            tag = String.valueOf(radioButton.getText());
            if (tag.equalsIgnoreCase(Constants.LIBRO))
                linearLayout.addView(inflater.inflate(R.layout.upload_libro_layout, null));
            else if (tag.equalsIgnoreCase(Constants.FILE))
                linearLayout.addView(inflater.inflate(R.layout.upload_file_layout, null));
            else
                linearLayout.addView(inflater.inflate(R.layout.upload_link_layout, null));

            initializeSubView();
        });
        if(newMaterial == null)
            radioGroup.check(R.id.radioButton);
        else
            preset();
    }

    private void preset(){
        radioGroup.setEnabled(false);
        radioGroup.setAlpha(0.5f);

        if(newMaterial.getType().equalsIgnoreCase(Constants.LIBRO)) {
            radioGroup.check(R.id.radioButton);
            mTitle.setText(newMaterial.getTitle());
            mAuthor.setText(newMaterial.getProfessors().get(0));
            mStatus.setText(splitComment(newMaterial.getComment())[0]);
            mComment.setText(splitComment(newMaterial.getComment())[1]);
        }
        else if(newMaterial.getType().equalsIgnoreCase(Constants.FILE)) {
            radioGroup.check(R.id.radioButton2);
            mTitle.setText(newMaterial.getTitle());
            mComment.setText(newMaterial.getComment());
        }
        else if(newMaterial.getType().equalsIgnoreCase(Constants.URL)) {
            radioGroup.check(R.id.radioButton3);
            mTitle.setText(newMaterial.getTitle());
            mLink.setText(newMaterial.getUrl());
            mComment.setText(newMaterial.getComment());
        }
    }

    private String[] splitComment(String comment){
        return comment.split("\n\n");
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

        if(!isMaterialModified && view.findViewById(R.id.button_save) != null)
            view.findViewById(R.id.button_save).setVisibility(View.GONE);
        else if(isMaterialModified && newMaterial.getType().equalsIgnoreCase(Constants.LIBRO))
            view.findViewById(R.id.button_save).setOnClickListener(v -> {
                if (mTitle.getText().toString().equals("") || mComment.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Hai lasciato dei campi vuoti", Toast.LENGTH_SHORT).show();
                } else {
                    createNewFile(newMaterial.getUrl());
                }
            });

        setExamChipGroup();
        setUploadButton();
    }

    private void setUploadButton() {
        view.findViewById(R.id.button_ok).setOnClickListener(v -> {
            if (mTitle.getText().toString().equals("") || mComment.getText().toString().equals("")){
                Toast.makeText(getContext(), "Hai lasciato dei campi vuoti", Toast.LENGTH_SHORT).show();
            } else {
                if (tag.equalsIgnoreCase(Constants.LIBRO))
                    createNewBook();
                else if (tag.equalsIgnoreCase(Constants.FILE))
                    getPDF();
                else if(tag.equalsIgnoreCase(Constants.URL))
                    createNewLink();
            }
        });
    }

    /**
     * This method create the new material with book type
     */
    private void createNewBook() {
        if (mAuthor.getText().toString().equals("") || mStatus.getText().toString().equals("")) {
            Toast.makeText(getContext(), "Hai lasciato dei campi vuoti", Toast.LENGTH_SHORT).show();
        } else {
            newMaterial.setTitle(String.valueOf(mTitle.getText()));
            newMaterial.addProfessor(String.valueOf(mAuthor.getText()));
            newMaterial.setType(tag);

            String comment = "Lo stato è: " + String.valueOf(mStatus.getText()) + "\n\n" + "Il commento è: " + String.valueOf(mComment.getText());
            newMaterial.setComment(comment);

            endProcess();
        }
    }

    /**
     * This method create the new material with file type
     */
    private void createNewFile(String url){
        newMaterial.setTitle(String.valueOf(mTitle.getText()));
        newMaterial.setComment(String.valueOf(mComment.getText()));
        newMaterial.setType(tag);
        newMaterial.setUrl(url);
        Log.d(Constants.TAG, "CIAONE " + url );

        endProcess();
    }

    /**
     * This method create the new material with link type
     */
    private void createNewLink(){
        if (Patterns.WEB_URL.matcher(mLink.getText()).matches()){
            newMaterial.setTitle(String.valueOf(mTitle.getText()));
            newMaterial.setType(tag);
            newMaterial.setUrl(String.valueOf(mLink.getText()));
            endProcess();
        } else {
            Toast.makeText(getContext(), "Url non valido", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method start the activity to get the pdf file
     */
    private void getPDF() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_PDF_CODE);
    }

    /**
     * This method is called when the intent is returned for getting the pdf file
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null)
            if (data.getData() != null) {
                createNewFile(data.getData().toString());
            }
            else
                Toast.makeText(getContext(), "No file chosen", Toast.LENGTH_SHORT).show();
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

                    if (tag.equalsIgnoreCase(Constants.FILE) && chip.isCheckedIconVisible()) {
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
                if (chip != null) {
                    chip.setOnClickListener(view -> {
                        if (!newMaterial.getProfessors().contains(String.valueOf(chip.getText())))
                            newMaterial.addProfessor(String.valueOf(chip.getText()));
                    });
                }
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
    public void isMaterialModified(Material m){
        isMaterialModified = true;
        newMaterial = m;
    }

    /**
     * This method calls the wait fragment to manage the upload of
     * the material in Firebase Firestore and Storage
     */
    private void endProcess(){
        UploadWaitFragment uploadWaitFragment = new UploadWaitFragment();
        uploadWaitFragment.setParameters(tag, newMaterial);

        if(getActivity() != null && getActivity().getSupportFragmentManager() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, uploadWaitFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
    }

}