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
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.kaori.kaori.Model.MiniUser;
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
    private final String GS_URL = "gs://kaori-c5a43.appspot.com";

    /**
     * Views from layout
     */
    private ChipGroup examsChipGroup, professorChipGroup;
    private EditText titleView, authorView, commentView, linkView, statusView;
    private StorageReference storage;
    private FirebaseFirestore db;
    private String exam, professor, flag;
    private boolean isMaterialModified = false, validFields[] = new boolean[2];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_layout, container, false);

        storage = FirebaseStorage.getInstance().getReferenceFromUrl(GS_URL);
        db = FirebaseFirestore.getInstance();

        for (int i = 0; i < validFields.length; i++)
            validFields[i] = false;

        initializeView(view);

        return view;
    }

    private void initializeView(View view) {
        LinearLayout linearLayout = view.findViewById(R.id.linearlayout);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            int selectedId = radioGroup1.getCheckedRadioButtonId();

            // find the radiobutton by returned id
            RadioButton radioButton = view.findViewById(selectedId);

            linearLayout.removeAllViews();

            flag = String.valueOf(radioButton.getText());
            if (flag.equalsIgnoreCase(Constants.LIBRO))
                linearLayout.addView(inflater.inflate(R.layout.upload_libro_layout, null));
            else if (flag.equalsIgnoreCase(Constants.FILE))
                linearLayout.addView(inflater.inflate(R.layout.upload_file_layout, null));
            else
                linearLayout.addView(inflater.inflate(R.layout.upload_link_layout, null));

            initializeSubView(view);
        });

        setUploadButton(view);

        radioGroup.check(R.id.radioButton);

    }

    private void initializeSubView(View view){
        titleView = view.findViewById(R.id.title);
        authorView = view.findViewById(R.id.author);
        commentView = view.findViewById(R.id.comment);
        linkView = view.findViewById(R.id.url);
        statusView = view.findViewById(R.id.status);

        setExamChipGroup(view);
    }

    private void setUploadButton(View view) {
        Button uploadButton = view.findViewById(R.id.button_ok);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(titleView.getText().toString().equals("") || commentView.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Hai lasciato dei campi vuoti", Toast.LENGTH_SHORT).show();
                }else {
                    if (flag.equalsIgnoreCase(Constants.LIBRO)) {
                        String comment = "Lo stato è " + String.valueOf(statusView) + "\n\n" + "Il commento è " + String.valueOf(commentView);
                        uploadFileIntoDb(null, Constants.LIBRO,  comment );
                    } else if (flag.equalsIgnoreCase(Constants.FILE)) {
                        getPDF();
                    } else
                        if(Patterns.WEB_URL.matcher(linkView.getText()).matches()){
                            uploadFileIntoDb(String.valueOf(linkView), Constants.URL, String.valueOf(commentView));
                        }else {
                            Toast.makeText(getContext(), "Url non valido", Toast.LENGTH_SHORT).show();
                        }
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

        StorageReference reference = storage.child(Constants.STORAGE_PATH_UPLOADS + DataManager.getInstance().getMiniUser().getName() + "_" + titleView.getText().toString() + ".pdf");
        UploadTask task = reference.putFile(data);

        // register observers to listen for when the download is done or if it fails
        task.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();

            uploadFileIntoDb(taskSnapshot.getUploadSessionUri().toString(), Constants.FILE, String.valueOf(commentView));

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
    private void uploadFileIntoDb(String uploadUri, String constant, String comment){
        Material material = createNewBook(uploadUri, constant, comment);

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
    private Material createNewBook(String uploadUri, String constant, String comment) {
        MiniUser miniUser = DataManager.getInstance().getMiniUser();
        String course = DataManager.getInstance().getUser().getCourse();
        return new Material(miniUser, String.valueOf(titleView.getText()), uploadUri, constant,
                    Timestamp.now(), exam, course, professor, comment, isMaterialModified );
    }

    /**
     * This method adds to the Chip Group related to exams in the View chips with exams names
     */
    private void setExamChipGroup(View view) {
        examsChipGroup = view.findViewById(R.id.exams);
        for(String e : DataManager.getInstance().getUser().getExams()){
            Chip chip = setChip(e);
            examsChipGroup.addView(chip);
            chip.setOnClickListener(v -> {
                professorChipGroup.removeAllViews();
                if (chip.isChecked()) {
                    exam = chip.getText().toString();
                    if(flag.equalsIgnoreCase(Constants.FILE))
                        setProfessorChipGroup(view);
                }
            });
        }
    }

    /**
     * This method adds to the Chip Group related to professor in the View chips with exams names
     */
    private void setProfessorChipGroup(View view) {
        if (professorChipGroup == null)
            professorChipGroup = view.findViewById(R.id.professors);
        db.collection(Constants.DB_COLL_PROFESSORS)
                .whereEqualTo(Constants.FIELD_EXAM, exam)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Chip chip = setChip(document.getString(Constants.FIELD_NAME));
                            professorChipGroup.addView(chip);
                            chip.setOnClickListener(v -> {
                                if (chip.isChecked())
                                    professor = chip.getText().toString();
                            });
                        }
                    }
                });
    }

    /**
     * This method sets up the chips on event listener
     */
    private Chip setChip(String text) {
        if(getContext()!= null) {
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

}