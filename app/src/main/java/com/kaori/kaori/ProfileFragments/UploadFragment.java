package com.kaori.kaori.ProfileFragments;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.Model.Material;
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
    private final String GS_URL = "gs://kaori-c5a43.appspot.com";

    /**
     * Views from layout
     */
    private EditText mTitle, mAuthor, mComment, mLink, mStatus;
    private StorageReference storage;
    private String tag;
    private Material newMaterial;
    private RadioGroup radioGroup;
    private View view;
    private List<String> exams;
    private List<CheckBox> checkBoxes;
    private boolean isMaterialModified;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upload_layout, container, false);
        storage = FirebaseStorage.getInstance().getReferenceFromUrl(GS_URL);
        initializeView();

        exams = DataManager.getInstance().getExams();
        view.findViewById(R.id.wait_layout).setVisibility(View.GONE);

        return view;
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

        view.findViewById(R.id.exams_button).setOnClickListener(view -> selectExams());
    }

    private void preset(){
        radioGroup.setVisibility(View.INVISIBLE);
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
        isMaterialModified = false;
        newMaterial = new Material();
        newMaterial.setUser(DataManager.getInstance().getMiniUser());
        newMaterial.setCourse(DataManager.getInstance().getUser().getCourse());
        newMaterial.setModified(isMaterialModified);
    }

    private void initializeSubView(){
        mTitle = view.findViewById(R.id.title);
        mAuthor = view.findViewById(R.id.author);
        mComment = view.findViewById(R.id.course);
        mLink = view.findViewById(R.id.url);
        mStatus = view.findViewById(R.id.status);
        if(isMaterialModified && newMaterial.getType().equalsIgnoreCase(Constants.LIBRO))
            view.findViewById(R.id.button_ok).setOnClickListener(v -> {
                if (mTitle.getText().toString().equals("") || mComment.getText().toString().equals("")){
                    LogManager.getInstance().showVisualMessage("Hai lasciato dei campi vuoti");
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
                LogManager.getInstance().showVisualMessage("Hai lasciato dei campi vuoti");
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
            LogManager.getInstance().showVisualMessage("Hai lasciato dei campi vuoti");
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
    private void createNewFile(String url) {
        StorageReference reference = storage
                .child(Constants.STORAGE_PATH_UPLOADS + DataManager.getInstance().getMiniUser().getName() + "_" + String.valueOf(mTitle.getText()) + ".pdf");
        if(isMaterialModified)
            reference.delete()
                    .addOnFailureListener(e -> LogManager.getInstance().printConsoleError("Error creating new document: " + e.toString()));
        newMaterial.setTitle(String.valueOf(mTitle.getText()));
        newMaterial.setComment(String.valueOf(mComment.getText()));
        newMaterial.setType(tag);
        newMaterial.setUrl(url);
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
            newMaterial.setComment(String.valueOf(mComment.getText()));
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
        /*examsChipGroup = view.findViewById(R.id.exams_label);
        for (String e : DataManager.getInstance().getUser().getExams()) {
            Chip chip = setChip(e);
            if (chip != null) {
                examsChipGroup.addView(chip);
                chip.setOnClickListener(view1 -> {
                    if (!newMaterial.getExams().contains(String.valueOf(chip.getText())))
                        newMaterial.addExam(String.valueOf(chip.getText()));
                });
            }
        }*/
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
        return null;
    }

    /**
     * This method sets if the material has been modified
     */
    public void isMaterialModified(Material m){
        isMaterialModified = true;
        newMaterial = m;
        newMaterial.setModified(true);
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

    private void selectExams(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_exams, null);
        LinearLayout checkboxSpace = dialogView.findViewById(R.id.checkboxSpace);
        checkBoxes = new ArrayList<>();

        for(int i = 0; i < exams.size(); i++){
            checkBoxes.add(new CheckBox(getContext()));
            checkboxSpace.addView(checkBoxes.get(checkBoxes.size()-1));
        }

        builder.setView(dialogView);
        builder.create().show();

    }

}