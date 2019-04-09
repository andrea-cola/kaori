package com.kaori.kaori.Configuration;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.kaori.kaori.Kaori.KaoriApp;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;

import java.util.ArrayList;

/**
 * Fragment responsible for the edit of profile information.
 */
public class EditUniveristyInfo extends Fragment {

    private Spinner mUniversity, mCourse;
    private String university, course;
    private ArrayList<String> universities, courses;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.edit_profile_info_layout_limited, container, false);
        setHasOptionsMenu(true);

        TextView mName = view1.findViewById(R.id.name);
        mUniversity = view1.findViewById(R.id.university);
        mCourse = view1.findViewById(R.id.course);
        Button mButtonOk = view1.findViewById(R.id.button);

        DataManager.getInstance()
                .loadImageIntoView(Uri.parse(DataManager.getInstance().getUser().getPhotosUrl()),
                        view1.findViewById(R.id.profileImage), getContext());
        mName.setText(DataManager.getInstance().getUser().getName());

        university = DataManager.getInstance().getUser().getUniversity();
        course = DataManager.getInstance().getUser().getCourse();

        universities = DataManager.getInstance().getAllUniversities();
        courses = DataManager.getInstance().getAllCourses(university);

        mButtonOk.setOnClickListener(v -> saveData());

        setupSpinners();

        return view1;
    }

    private void setupSpinners() {
        ArrayAdapter adapterUniversity = new ArrayAdapter<>(getContext(), R.layout.spinner_item,
                universities);
        adapterUniversity.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mUniversity.setAdapter(adapterUniversity);
        mUniversity.setSelection(universities.indexOf(university));

        ArrayAdapter adapterCourses = new ArrayAdapter<>(getContext(), R.layout.spinner_item,
                courses);
        adapterCourses.setNotifyOnChange(true);
        adapterCourses.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mCourse.setAdapter(adapterCourses);
        mCourse.setSelection(courses.indexOf(course));

        mUniversity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                university = adapterView.getItemAtPosition(i).toString();
                courses = DataManager.getInstance().getAllCourses(university);
                mUniversity.getSelectedItem();
                adapterCourses.clear();
                adapterCourses.addAll(courses);
                mCourse.setSelection(courses.indexOf(course), true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                course = mCourse.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void saveData() {
        DataManager.getInstance().getUser().setUniversity(university);
        DataManager.getInstance().getUser().setCourse(course);

        DataManager.getInstance().updateUser();
        Intent intent = new Intent(getActivity(), KaoriApp.class);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

}