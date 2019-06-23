package com.kaori.kaori.Kaori.ProfileFragments.UploadFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.kaori.kaori.Kaori.ProfileFragments.MyFilesFragment;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

import java.util.ArrayList;
import java.util.List;

public class UploadUrlFragment extends Fragment {

    private TextView exams, title, note, link;
    private List<String> examsList;
    private Document oldDocument;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_link_layout, null);
        view.findViewById(R.id.button).setOnClickListener(v -> createNewLink());
        view.findViewById(R.id.button_exams).setOnClickListener(v -> selectExams());

        title = view.findViewById(R.id.title);
        note = view.findViewById(R.id.status);
        link = view.findViewById(R.id.url);

        exams = view.findViewById(R.id.exams);
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
        link.setText(oldDocument.getUrl());

        String tmp = oldDocument.getExams().get(0);
        for(int i = 1; i < oldDocument.getExams().size(); i++)
            tmp = tmp + ", " + oldDocument.getExams().get(i);
        exams.setText(tmp);
        examsList = oldDocument.getExams();
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
            exams.setText("No exam selected");
            examsList.clear();
            boolean flag = true;
            for(int i = 0; i < checkBoxes.size(); i++){
                if(checkBoxes.get(i).isChecked()) {
                    if(flag) {
                        flag = false;
                        exams.setText(DataManager.getInstance().getAllExams().get(i));
                    } else {
                        exams.setText(exams.getText() + ", " + DataManager.getInstance().getAllExams().get(i));
                    }
                    examsList.add(DataManager.getInstance().getAllExams().get(i));
                }
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private boolean checkUrlParameters(){
        return title.getText().length()>0 && link.getText().length()>0 &&
                note.getText().length()>0;
    }

    /**
     * This method create the new material with link type
     */
    private void createNewLink(){
        Document document;
        if(oldDocument != null) {
            document = oldDocument;
            document.setModified(true);
        }else {
            document = new Document();
            document.setModified(false);
        }
        if (!Patterns.WEB_URL.matcher(link.getText()).matches())
            LogManager.getInstance().showVisualMessage(getString(R.string.error_link_upload_msg));
        else if (!checkUrlParameters())
            LogManager.getInstance().showVisualMessage(getString(R.string.error_upload_msg));
        else {
            document.setTitle(String.valueOf(title.getText()));
            document.setUrl(String.valueOf(link.getText()));
            document.setNote(String.valueOf(note.getText()));
            document.setUser(DataManager.getInstance().getMiniUser());
            document.setCourse(DataManager.getInstance().getUser().getCourse());
            document.setUniversity(DataManager.getInstance().getUser().getUniversity());
            document.setExams(examsList);
            document.setType(Constants.FILE);
            document.setSubtype(Constants.URL);
            document.setTimestamp(Timestamp.now().getSeconds());
            DataManager.getInstance().uploadDocument(document,
                    response -> {
                        LogManager.getInstance().printConsoleMessage("Document uploaded.");
                        endProcess();
                    },
                    error -> {
                        LogManager.getInstance().printConsoleMessage("Upload failed. Retry.");
                    });
        }
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

}
