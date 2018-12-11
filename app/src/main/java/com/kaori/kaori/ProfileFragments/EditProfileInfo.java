package com.kaori.kaori.ProfileFragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kaori.kaori.DBObjects.Course;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;

public class EditProfileInfo extends Fragment {

    private View view;
    private EditText mName;
    private AutoCompleteTextView mUniversity, mCourse;
    private Button mButtonOk;
    private FirebaseFirestore db;
    private ArrayList<Course> allCourses;
    private ArrayList<String> universities, courses;
    private Context context;
    private String selectedUniversity, selectedCourse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_profile_info_layout, container, false);
        context = getContext();
        mName = view.findViewById(R.id.profile_name);
        mUniversity = view.findViewById(R.id.profile_university);
        mCourse = view.findViewById(R.id.profile_course);
        mButtonOk = view.findViewById(R.id.button_ok);

        db = FirebaseFirestore.getInstance();
        loadData();

        // pre-set
        mName.setText(DataManager.getInstance().getUser().getName());
        mUniversity.setText(DataManager.getInstance().getUser().getUniversity());
        mCourse.setText(DataManager.getInstance().getUser().getCourse());

        //set listeners
        mUniversity.setOnItemClickListener((adapterView, view14, i, l) -> {
            setViewState(mCourse, true);

            selectedUniversity = String.valueOf(adapterView.getItemAtPosition(i));

            courses = new ArrayList<>();
            for(Course c : allCourses)
                if(c.getUniversity().equalsIgnoreCase(selectedUniversity))
                    courses.add(c.getName());

            mCourse.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, courses));
            LogManager.getInstance().printConsoleMessage("" + mCourse.getAdapter().getCount());
        });

        mUniversity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!universities.contains(String.valueOf(charSequence))) {
                    setViewState(mCourse, false);
                    mCourse.setText("");
                    setViewState(mButtonOk, false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 0){
                    setViewState(mCourse, false);
                    mCourse.setText("");
                    setViewState(mButtonOk, false);
                }
            }
        });

        mCourse.setOnItemClickListener((adapterView, view14, i, l) -> {
            selectedCourse = String.valueOf(adapterView.getItemAtPosition(i));
            setViewState(mButtonOk, true);
            closeKeyboard();
        });

        mCourse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(courses != null && !courses.contains(String.valueOf(charSequence)))
                    setViewState(mButtonOk, false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 0)
                    setViewState(mButtonOk, false);
            }
        });

        mButtonOk.setOnClickListener(v -> {
            view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
            saveData();
        });

        return view;
    }

    /**
     * Save data into database.
     */
    private void saveData() {
        User user = DataManager.getInstance().getUser();
        user.setUniversity(selectedUniversity);
        user.setCourse(selectedCourse);
        user.setExams(new ArrayList<>());

        db.collection(Constants.DB_COLL_USERS)
                .document(user.getUid()).set(user)
                .addOnSuccessListener(aVoid -> {
                    LogManager.getInstance().printConsoleMessage("saveData:written");
                    getFragmentManager().popBackStackImmediate();
                })
                .addOnFailureListener(e -> {
                    LogManager.getInstance().showVisualError(context, e, "saveData:fail");
                    getFragmentManager().popBackStackImmediate();
                });
    }

    /**
     * Load data from database.
     */
    private void loadData(){
        loadUniversities();
    }

    /**
     * Load universities list.
     */
    private void loadUniversities() {
        universities = new ArrayList<>();
        db.collection(Constants.DB_COLL_UNIVERSITIES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult())
                            universities.add(String.valueOf(document.get("name")));
                        mUniversity.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, universities));
                    } else {
                        // TODO
                    }
                    loadCourses();
                });
    }

    /**
     * Load course.
     */
    private void loadCourses() {
        allCourses = new ArrayList<>();
        db.collection(Constants.DB_COLL_COURSE_TYPES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        for (QueryDocumentSnapshot document : task.getResult())
                            allCourses.add(document.toObject(Course.class));
                    else {
                        // TODO
                    }
                    view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
                });
    }

    /**
     * Enable the view passed as argument.
     */
    private void setViewState(View view, boolean activeFlag){
        view.setEnabled(activeFlag);
        view.setAlpha((activeFlag) ? 1f : 0.5f);
    }

    /**
     * Close keyboard.
     */
    private void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

}
