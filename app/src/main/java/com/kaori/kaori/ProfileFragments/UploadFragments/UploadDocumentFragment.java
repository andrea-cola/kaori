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

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

    private final String GS_URL = "gs://kaori-c5a43.appspot.com";
    private final int PICK_PDF_CODE = 4564;
    private Document document;
    private StorageReference storage;
    private TextView exams;
    private ArrayList<String> examsList;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upload_file_layout, null);
        view.findViewById(R.id.button).setOnClickListener(v -> createDocument());
        view.findViewById(R.id.button_exams).setOnClickListener(v -> selectExams());
        exams = view.findViewById(R.id.exams);

        storage = FirebaseStorage.getInstance().getReferenceFromUrl(GS_URL);
        examsList = new ArrayList<>();

        return view;
    }

    private void createDocument(){
        TextView title, note;

        title = view.findViewById(R.id.title);
        note = view.findViewById(R.id.note);

        document = new Document();
        document.setTitle(title.getText().toString());
        document.setUser(DataManager.getInstance().getMiniUser());
        document.setNote(note.getText().toString());
        document.setCourse(DataManager.getInstance().getUser().getCourse());
        document.setUniversity(DataManager.getInstance().getUser().getUniversity());
        document.setExams(examsList);
        document.setModified(false);

        getFile();
    }

    /**
     * This method create the new material with file type
     */
    private void createNewFile(String url) {
        StorageReference reference = storage.child(Constants.STORAGE_PATH_UPLOADS + DataManager.getInstance().getMiniUser().getName() + "_" + String.valueOf(document.getTitle()) + ".pdf");
        if(document.getModified())
            reference.delete().addOnFailureListener(e -> LogManager.getInstance().printConsoleError("Error deleting the old document."));
        document.setUrl(url);
        DataManager.getInstance().uploadDocument(document);
        endProcess();
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
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null)
            createNewFile(data.getData().toString());
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
                    if(flag)
                        exams.setText(DataManager.getInstance().getAllExams().get(i));
                    else {
                        flag = false;
                        exams.setText(exams.getText() + ", " + DataManager.getInstance().getAllExams().get(i));
                    }
                    examsList.add(DataManager.getInstance().getAllExams().get(i));
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

}
