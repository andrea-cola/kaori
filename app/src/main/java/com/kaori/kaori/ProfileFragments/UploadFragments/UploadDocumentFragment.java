package com.kaori.kaori.ProfileFragments.UploadFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.ProfileFragments.MyFilesFragment;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class UploadDocumentFragment extends Fragment {

    private final int PICK_PDF_CODE = 4564;
    private Document oldDocument, document;
    private TextView exams, title, note;
    private List<String> examsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_file_layout, null);
        view.findViewById(R.id.button).setOnClickListener(v -> createDocument());
        view.findViewById(R.id.button_exams).setOnClickListener(v -> selectExams());

        exams = view.findViewById(R.id.exams);
        title = view.findViewById(R.id.title);
        note = view.findViewById(R.id.note);

        examsList = new ArrayList<>();
        if(oldDocument != null)
            preset();

        return view;
    }

    public void setOldDocument(Document document){
        this.oldDocument = document;
    }

    private void preset(){
        title.setText(oldDocument.getTitle());
        note.setText(oldDocument.getNote());

        String tmp = oldDocument.getExams().get(0);
        for(int i = 1; i < oldDocument.getExams().size(); i++)
            tmp = tmp + ", " + oldDocument.getExams().get(i);
        exams.setText(tmp);
        examsList = oldDocument.getExams();
    }

    private void createDocument(){
        document = new Document();
        document.setTitle(title.getText().toString());
        document.setUser(DataManager.getInstance().getMiniUser());
        document.setNote(note.getText().toString());
        document.setCourse(DataManager.getInstance().getUser().getCourse());
        document.setUniversity(DataManager.getInstance().getUser().getUniversity());
        document.setExams(examsList);
        document.setType(Constants.FILE);
        document.setModified(false);
        document.setTimestamp(Timestamp.now());

        getFile();
    }

    /**
     * This method start the activity to get the pdf file
     */
    private void getFile() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleziona il tuo file"), PICK_PDF_CODE);
    }

    /**
     * This method is called when the intent is returned for getting the pdf file
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            DataManager.getInstance().uploadFileOnTheServer(data.getData().toString(), document);
            endProcess();
        }
        else
            LogManager.getInstance().showVisualMessage("Non hai selezionato nessun file.");
    }

    /**
     * Return to profile
     */
    private void endProcess() {
        if(getActivity() != null && getActivity().getSupportFragmentManager() != null) {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new MyFilesFragment())
                    .commit();
        }
    }

    /**
     * Create the popup to choose the exams.
     */
    private void selectExams(){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_exams, null);
        LinearLayout checkboxSpace = dialogView.findViewById(R.id.checkboxSpace);
        List<CheckBox> checkBoxes = new ArrayList<>();

        for(int i = 0; i < DataManager.getInstance().getAllExams().size(); i++){
            CheckBox c = new CheckBox(getContext());
            c.setText(DataManager.getInstance().getAllExams().get(i));
            checkBoxes.add(c);
            checkboxSpace.addView(checkBoxes.get(checkBoxes.size()-1));
        }

        builder.setView(dialogView);
        dialog = builder.create();

        dialogView.findViewById(R.id.button).setOnClickListener(v -> {
            exams.setText("Nessun esame selezionato");
            examsList.clear();
            boolean flag = true;
            for(int i = 0; i < checkBoxes.size(); i++){
                if(checkBoxes.get(i).isChecked()) {
                    if(flag) {
                        flag = false;
                        exams.setText(DataManager.getInstance().getAllExams().get(i));
                    }
                    else
                        exams.setText(exams.getText() + ", " + DataManager.getInstance().getAllExams().get(i));
                    examsList.add(DataManager.getInstance().getAllExams().get(i));
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

}
