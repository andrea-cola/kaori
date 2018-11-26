package com.kaori.kaori.LoginRegistrationFragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.Constants;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.R;

import java.util.ArrayList;

/**
 * This class represents the last phase of the
 */
public class CreateAccount3 extends Fragment {

    /**
     * Variables.
     */
    private ArrayList<String> universities, courseTypes, exams;
    private LinearLayout spinnerSpace;
    private FirebaseFirestore db;
    private View waitLayout;
    private ArrayList<AutoCompleteTextView> autoCompleteTextViewArrayList;
    private User user;
    private Button addSpinnerButton, createNewAccountButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg3, container, false);
        View tmp = inflater.inflate(R.layout.registration_spinner, null, false);

        // get views from layout
        spinnerSpace = view.findViewById(R.id.spinner_space);
        waitLayout = view.findViewById(R.id.wait_layout);
        createNewAccountButton = view.findViewById(R.id.button_create_new_account);

        // instantiate variables
        db = FirebaseFirestore.getInstance();
        autoCompleteTextViewArrayList = new ArrayList<>();
        exams = new ArrayList<>();
        courseTypes = new ArrayList<>();
        universities = new ArrayList<>();

        autoCompleteTextViewArrayList.add((AutoCompleteTextView)view.findViewById(R.id.reg_ac_uni));
        autoCompleteTextViewArrayList.add((AutoCompleteTextView)view.findViewById(R.id.reg_ac_course_type));

        createNewAccountButton.setEnabled(false);
        createNewAccountButton.setAlpha(0.5f);
        autoCompleteTextViewArrayList.get(1).setEnabled(false);
        autoCompleteTextViewArrayList.get(1).setAlpha(0.5f);

        // add the first spinner
        autoCompleteTextViewArrayList.add((AutoCompleteTextView) tmp.findViewById(R.id.reg_ac));
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
                updateUser();
            }
        });

        addListenerUniversitiesSpinner();
        addListenerCourseTypesSpinner();
        addListenerCourseTypesSpinner2();

        getUniversitiesListFromDatabase();

        return view;
    }

    public void setUser(User user){
        this.user = user;
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
            View tmp = inflater.inflate(R.layout.registration_spinner, null, false);
            AutoCompleteTextView tmpS = tmp.findViewById(R.id.reg_ac);
            tmpS.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, exams));
            autoCompleteTextViewArrayList.add(tmpS);
            linearLayout.addView(tmp);
        }
    }

    private void getUniversitiesListFromDatabase() {
        db.collection(Constants.DB_COLL_UNIVERSITIES).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult())
                        universities.add(document.getString("name"));
                }
                else
                    //TODO
                    Log.d(Constants.TAG, "Error getting documents: ", task.getException());

                getCourseTypesFromDatabase();
                }
            });
    }

    private void getCourseTypesFromDatabase() {
        db.collection(Constants.DB_COLL_COURSE_TYPES).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult())
                        courseTypes.add(document.getString("name"));
                }
                else
                    //TODO
                    Log.d(Constants.TAG, "Error getting documents: ", task.getException());

                getCoursesFromDatabase();
            }
        });
    }

    private void getCoursesFromDatabase() {
        db.collection(Constants.DB_COLL_COURSES).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                    for (QueryDocumentSnapshot document : task.getResult())
                        exams.add(document.getString("name"));
                else
                    //TODO
                    Log.d(Constants.TAG, "Error getting documents: ", task.getException());

                setSpinnerAdapters();
            }
        });
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

            waitLayout.setVisibility(View.GONE);
        }
    }

    private void updateUser() {
        try {
            ArrayList<String> chosenExams = new ArrayList<>();
            for (AutoCompleteTextView actw : autoCompleteTextViewArrayList)
                chosenExams.add(actw.getText().toString());

            user.setExams(chosenExams);
            user.setUniversity(autoCompleteTextViewArrayList.get(0).getText().toString());
            user.setCourseType(autoCompleteTextViewArrayList.get(1).getText().toString());
        } finally {
            uploadNewUserOnTheServer();
        }
    }

    private void uploadNewUserOnTheServer(){
        final AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Sei sicuro di procedere?")
            .setMessage("Premendo Ok completerai la tua registazione e riceverai una mail di conferma. Premendo su ANNULLA potrai modificare i tuoi dati.")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    db.collection("users")
                            .document()
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(Constants.TAG, "DocumentSnapshot successfully written!");
                                    // TODO: mandare mail in caso di conferma.
                                    if (getActivity() != null)
                                        getActivity().recreate();
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
            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

}