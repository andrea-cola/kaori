package com.kaori.kaori.LoginRegistrationFragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class CreateAccount3 extends Fragment {

    /**
     * Variables.
     */
    private ArrayList<String> universities, courseTypes, exams;
    private LinearLayout linearLayout;
    private FirebaseFirestore db;
    private View waitLayout;
    private ArrayList<AutoCompleteTextView> autoCompleteTextViewArrayList;
    private User user;
    private AutoCompleteTextView acUni, acCourseType;
    private Button buttonAddSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg3, container, false);
        linearLayout = view.findViewById(R.id.spinner_space);
        acUni = view.findViewById(R.id.reg_ac_uni);
        acCourseType = view.findViewById(R.id.reg_ac_course_type);
        waitLayout = view.findViewById(R.id.wait_layout);

        db = FirebaseFirestore.getInstance();
        autoCompleteTextViewArrayList = new ArrayList<>();
        exams = new ArrayList<>();
        courseTypes = new ArrayList<>();
        universities = new ArrayList<>();

        acCourseType.setEnabled(false);
        acCourseType.setAlpha(0.5f);

        // add the first spinner
        View tmp = inflater.inflate(R.layout.registration_spinner, null, false);
        autoCompleteTextViewArrayList.add((AutoCompleteTextView) tmp.findViewById(R.id.reg_ac));
        autoCompleteTextViewArrayList.get(0).setEnabled(false);
        autoCompleteTextViewArrayList.get(0).setAlpha(0.5f);
        linearLayout.addView(tmp);

        // listener of the first button
        buttonAddSpinner = view.findViewById(R.id.button_add_spinner);
        buttonAddSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSpinner(inflater, linearLayout);
           }
        });

        // listener of the second button
        Button button = view.findViewById(R.id.button_create_new_account);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUser();
            }
        });

        addListenerUniversitiesSpinner();
        addListenerCourseTypesSpinner();

        getUniversitiesListFromDatabase();

        return view;
    }

    public void setUser(User user){
        this.user = user;
    }

    private void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    private void addListenerUniversitiesSpinner(){
        acUni.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                acCourseType.setEnabled(true);
                acCourseType.setAlpha(1f);
                closeKeyboard();
            }
        });
    }

    private void addListenerCourseTypesSpinner(){
        acCourseType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                autoCompleteTextViewArrayList.get(0).setEnabled(true);
                autoCompleteTextViewArrayList.get(0).setAlpha(1f);
                buttonAddSpinner.setEnabled(true);
                buttonAddSpinner.setAlpha(1f);
                closeKeyboard();
            }
        });
    }

    private void addSpinner(LayoutInflater inflater, LinearLayout linearLayout){
        if(autoCompleteTextViewArrayList.size() < exams.size()) {
            View tmp = inflater.inflate(R.layout.registration_spinner, null, false);
            AutoCompleteTextView tmpS = tmp.findViewById(R.id.reg_ac);
            tmpS.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, exams));
            autoCompleteTextViewArrayList.add(tmpS);
            linearLayout.addView(tmp);
        }
    }

    private void getUniversitiesListFromDatabase(){
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

    private void getCourseTypesFromDatabase(){
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

    private void getCoursesFromDatabase(){
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

    private void setSpinnerAdapters(){
        Context context = getContext();
        if(context != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, universities);
            acUni.setAdapter(adapter);

            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, courseTypes);
            acCourseType.setAdapter(adapter2);

            ArrayAdapter<String> adapter3 = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, exams);
            autoCompleteTextViewArrayList.get(0).setAdapter(adapter3);

            waitLayout.setVisibility(View.GONE);
        }
    }

    private void updateUser(){
        try {
            ArrayList<String> chosenExams = new ArrayList<>();
            for (AutoCompleteTextView spinner : autoCompleteTextViewArrayList) {
                chosenExams.add(spinner.getText().toString());
            }
            user.setExams(chosenExams);

            user.setUniversity(acUni.getText().toString());
            user.setCourseType(acCourseType.getText().toString());
        } finally {
            uploadNewUserOnTheServer();
        }
    }

    private void uploadNewUserOnTheServer(){
        db.collection("users").document()
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // TODO: vanno settate le preferenze come utente loggato.
                        Log.d(Constants.TAG, "DocumentSnapshot successfully written!");
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

}
