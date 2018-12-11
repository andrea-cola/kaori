package com.kaori.kaori.ProfileFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.R;
import com.kaori.kaori.Kaori;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * This class represents the last phase of the
 */
public class CreateAccount4 extends Fragment {

    /**
     * Variables.
     */
    private ArrayList<String> universities, courseTypes, exams;
    private LinearLayout spinnerSpace;
    private FirebaseFirestore db;
    private ArrayList<AutoCompleteTextView> autoCompleteTextViewArrayList;
    private User user;
    private Button addSpinnerButton, createNewAccountButton;
    private StorageReference mStorage;
    private Bitmap localProfileBitmap;
    private String password;
    private ProgressDialog pd;
    private int method;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg3, container, false);
        View tmp = inflater.inflate(R.layout.autocompletetextview, null, false);

        // get views from layout
        spinnerSpace = view.findViewById(R.id.spinner_space);
        createNewAccountButton = view.findViewById(R.id.button_ok);

        // instantiate variables
        db = FirebaseFirestore.getInstance();
        autoCompleteTextViewArrayList = new ArrayList<>();
        exams = DataManager.getInstance().getExams();
        courseTypes = DataManager.getInstance().getCourseTypes();
        universities = DataManager.getInstance().getUniversities();

        autoCompleteTextViewArrayList.add((AutoCompleteTextView)view.findViewById(R.id.reg_ac_uni));
        autoCompleteTextViewArrayList.add((AutoCompleteTextView)view.findViewById(R.id.reg_ac_course_type));

        // add the first spinner
        autoCompleteTextViewArrayList.add((AutoCompleteTextView) tmp.findViewById(R.id.autocompleteview));
        autoCompleteTextViewArrayList.get(2).setEnabled(false);
        autoCompleteTextViewArrayList.get(2).setAlpha(0.5f);
        spinnerSpace.addView(tmp);

        // listener of the first button
        addSpinnerButton = view.findViewById(R.id.button_add_spinner);
        addSpinnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSpinner(inflater, spinnerSpace);
           }
        });

        // listener of the second button
        createNewAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildPopup();
            }
        });

        setSpinnerAdapters();

        addListenerUniversitiesSpinner();
        addListenerCourseTypesSpinner();
        addListenerCourseTypesSpinner2();

        return view;
    }

    public void setMethod(int method){
        this.method = method;
    }

    private void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    private void addListenerUniversitiesSpinner() {
        autoCompleteTextViewArrayList.get(0).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                autoCompleteTextViewArrayList.get(1).setEnabled(true);
                autoCompleteTextViewArrayList.get(1).setAlpha(1f);
                closeKeyboard();
            }
        });
    }

    private void addListenerCourseTypesSpinner() {
        autoCompleteTextViewArrayList.get(1).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                autoCompleteTextViewArrayList.get(2).setEnabled(true);
                autoCompleteTextViewArrayList.get(2).setAlpha(1f);
                addSpinnerButton.setEnabled(true);
                addSpinnerButton.setAlpha(1f);
                closeKeyboard();
            }
        });
    }

    private void addListenerCourseTypesSpinner2() {
        autoCompleteTextViewArrayList.get(2).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                createNewAccountButton.setEnabled(true);
                createNewAccountButton.setAlpha(1f);
                closeKeyboard();
            }
        });
    }

    private void addSpinner(LayoutInflater inflater, LinearLayout linearLayout) {
        if(autoCompleteTextViewArrayList.size() < exams.size()) {
            View tmp = inflater.inflate(R.layout.autocompletetextview, null, false);
            AutoCompleteTextView tmpS = tmp.findViewById(R.id.autocompleteview);
            tmpS.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, exams));
            autoCompleteTextViewArrayList.add(tmpS);
            linearLayout.addView(tmp);
        }
    }

    private void setSpinnerAdapters() {
        Context context = getContext();
        if(context != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, universities);
            autoCompleteTextViewArrayList.get(0).setAdapter(adapter);

            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, courseTypes);
            autoCompleteTextViewArrayList.get(1).setAdapter(adapter2);

            ArrayAdapter<String> adapter3 = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, exams);
            autoCompleteTextViewArrayList.get(2).setAdapter(adapter3);
        }
    }

    private void updateUser() {
        ArrayList<String> chosenExams = new ArrayList<>();
        for (AutoCompleteTextView actw : autoCompleteTextViewArrayList)
            chosenExams.add(actw.getText().toString());

        user.setExams(chosenExams);
        user.setUniversity(autoCompleteTextViewArrayList.get(0).getText().toString());
        user.setCourse(autoCompleteTextViewArrayList.get(1).getText().toString());
    }

    private void buildPopup(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Sei sicuro di procedere?")
                .setMessage("Premendo Ok completerai la tua registazione e riceverai una mail di conferma. Premendo su ANNULLA potrai modificare i tuoi dati.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        signInNewUser();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void signInNewUser() {
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Creating new user...");
        pd.show();

        updateUser();

        if(method == 0) {
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(user.getEmail(), password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(Constants.TAG, "createUserWithEmail:success");
                                user.setUid(task.getResult().getUser().getUid());

                                if (localProfileBitmap != null)
                                    uploadProfileImageOnTheServer();
                                else {
                                    user.setPhotosUrl(Constants.STORAGE_DEFAULT_PROFILE_IMAGE);
                                    uploadNewUserOnTheServer();
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(Constants.TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else if(method == 1 || method == 2)
            uploadNewUserOnTheServer();
    }

    private void uploadNewUserOnTheServer(){
        pd.setMessage("Uploading the user...");

        db.collection("users")
            .document()
            .set(user)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(Constants.TAG, "DocumentSnapshot successfully written!");
                    endRegistration();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO: fare riprovare l'utente.
                    Log.w(Constants.TAG, "Error writing document", e);
                }
            });
    }

    private void uploadProfileImageOnTheServer(){
        pd.setMessage("Uploading the profile image...");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        localProfileBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        mStorage = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PATH_PROFILE_IMAGES + user.getUid());
        UploadTask uploadTask = mStorage.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w(Constants.TAG, exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(Constants.TAG, uri.toString());
                        user.setPhotosUrl(uri.toString());
                        uploadNewUserOnTheServer();
                    }
                });
            }
        });
    }

    private void endRegistration(){
        pd.dismiss();
        // TODO: mandare mail in caso di conferma.
        if (getActivity() != null){
            startActivity(new Intent(getActivity(), Kaori.class));
            getActivity().finish();
        }
    }

}