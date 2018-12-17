package com.kaori.kaori.ProfileFragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that allows to edit the attended exams.
 */
public class EditCoursesFragment extends Fragment {

    /**
     * Constants.
     */
    private final String ALREADY_SELECTED_ERROR = "Esame già selezionato";
    private final String GENERIC_ERROR = "Impossibile contattare il server";

    /**
     * Variables.
     */
    private Context context;
    private View view;
    private Button buttonOk;
    private List<String> importedCourses, selectedCourses;
    private LayoutInflater i;
    private ArrayAdapter standardAdapter;
    private LinearLayout addingSpace;
    private List<AutoCompleteTextView> autoCompleteTextViewList;
    private List<View> fieldsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_profile_exams, container, false);
        addingSpace = view.findViewById(R.id.adding_space);
        view.findViewById(R.id.button_add_field).setOnClickListener(view -> addAutoCompleteTextView(null));
        buttonOk = view.findViewById(R.id.button_ok);

        buttonOk.setOnClickListener(view -> saveData());

        autoCompleteTextViewList = new ArrayList<>();
        fieldsList = new ArrayList<>();
        selectedCourses = new ArrayList<>();
        context = getContext();
        i = inflater;

        loadExams();

        return view;
    }

    /**
     * Add a auto complete text view.
     */
    private void addAutoCompleteTextView(String e) {
        if (autoCompleteTextViewList.size() == 0 || !autoCompleteTextViewList.get(autoCompleteTextViewList.size() - 1).getText().toString().isEmpty()) {
            if (autoCompleteTextViewList.size() < importedCourses.size()) {
                View acwLayout = i.inflate(R.layout.autocompletetextview, null, false);
                AutoCompleteTextView acw = acwLayout.findViewById(R.id.autocompleteview);
                acw.setAdapter(standardAdapter);

                // delete button on click listener.
                acwLayout.findViewById(R.id.delete).setOnClickListener(view -> {
                    if (selectedCourses.size() > fieldsList.indexOf(acwLayout))
                        selectedCourses.remove(fieldsList.indexOf(acwLayout));
                    addingSpace.removeView(acwLayout);
                    fieldsList.remove(acwLayout);
                    autoCompleteTextViewList.remove(acw);
                });

                addingSpace.addView(acwLayout);
                fieldsList.add(acwLayout);
                autoCompleteTextViewList.add(acw);

                if (e != null)
                    acw.setText(e);

                attachListeners(acw);
            } else
                LogManager.getInstance().showVisualMessage(context, "Non è possible aggiungere altri esami.");
        }else
            LogManager.getInstance().showVisualMessage(context, "Compila il campo vuoto.");
    }

    /**
     * Attach item click listener.
     * Attach text changed listener.
     */
    private void attachListeners(AutoCompleteTextView acw){
        acw.setOnItemClickListener((adapterView, view, i, l) -> {

            if(selectedCourses.contains(String.valueOf(adapterView.getItemAtPosition(i)))) {
                int indexInSelectedCourses = selectedCourses.indexOf(String.valueOf(adapterView.getItemAtPosition(i)));
                int indexInAutoCompleteTextView = autoCompleteTextViewList.indexOf(acw);
                if(indexInAutoCompleteTextView != indexInSelectedCourses) {
                    acw.setError(ALREADY_SELECTED_ERROR);
                    acw.setText("");
                    changeViewState(buttonOk, false);
                } else {
                    changeViewState(buttonOk, true);
                }
            } else {
                addExamsToList(acw, String.valueOf(adapterView.getItemAtPosition(i)));
                changeViewState(buttonOk, true);
            }
        });

        acw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(buttonOk.isEnabled())
                    changeViewState(buttonOk, false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Load all the exams taking into account the university
     * and the course of the user.
     */
    private void loadExams() {
        FirebaseFirestore.getInstance().collection(Constants.DB_COLL_EXAMS)
                .whereEqualTo(Constants.FIELD_UNIVERSITY, DataManager.getInstance().getUser().getUniversity())
                .whereEqualTo(Constants.FIELD_COURSES, DataManager.getInstance().getUser().getCourse())
                .get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot result = task.getResult();
                    if (task.isSuccessful() && result != null) {
                        importedCourses = new ArrayList<>();
                        for (QueryDocumentSnapshot document : result) {
                            LogManager.getInstance().printConsoleMessage(String.valueOf(document.get("name")));
                            importedCourses.add(String.valueOf(document.get("name")));
                        }

                        standardAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, importedCourses);
                        updateList();
                    }
                    else {
                        LogManager.getInstance().showVisualError(context,null,"Impossibile caricare i corsi, riprovare.");
                        endProcess(false);
                    }
                });
    }

    /**
     * Add fields as the exams already set in the database.
     */
    private void updateList(){
        for(String e : DataManager.getInstance().getUser().getExams()) {
            selectedCourses.add(e);
            addAutoCompleteTextView(e);
        }
        view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
    }

    /**
     * Save data into database.
     */
    private void saveData() {
        User user = DataManager.getInstance().getUser();
        user.setExams(selectedCourses);

        view.findViewById(R.id.wait_layout).setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance().collection(Constants.DB_COLL_USERS)
                .document(user.getUid()).set(user)
                .addOnSuccessListener(aVoid -> {
                    LogManager.getInstance().printConsoleMessage("saveData:written");
                    endProcess(true);
                })
                .addOnFailureListener(e -> {
                    LogManager.getInstance().showVisualError(getContext(), e, "saveData:fail");
                });
    }

    /**
     * This method is called whenever the process is terminated.
     * Return to the previous fragment.
     */
    private void endProcess(boolean isSuccess){
        if (isSuccess && context != null) {
            LogManager.getInstance().printConsoleMessage("endProcess:success");
            getFragmentManager().popBackStackImmediate();
        }
        else if(!isSuccess && context != null) {
            LogManager.getInstance().showVisualError(context, null, GENERIC_ERROR);
            (new Handler()).postDelayed(() -> getFragmentManager().popBackStackImmediate(), 3000);
        }
    }

    /**
     * Enable or disable a view.
     */
    private void changeViewState(View v, boolean isEnabled){
        v.setAlpha((isEnabled) ? 1f : 0.5f);
        v.setEnabled(isEnabled);
    }

    /**
     * Add an element to a list if the list is empty.
     * If the list is that position is already set overwrite the
     * list cell.
     */
    private void addExamsToList(AutoCompleteTextView acw, String s){
        if(autoCompleteTextViewList.indexOf(acw) < selectedCourses.size())
            selectedCourses.add(autoCompleteTextViewList.indexOf(acw), s);
        else
            selectedCourses.add(s);
    }

}
