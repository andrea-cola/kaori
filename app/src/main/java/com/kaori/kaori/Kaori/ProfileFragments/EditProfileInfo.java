package com.kaori.kaori.Kaori.ProfileFragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.kaori.kaori.App;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Kaori.KaoriApp;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.ImagePicker;
import com.kaori.kaori.Services.LogManager;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import static com.kaori.kaori.Constants.MY_CAMERA_PERMISSION_CODE;

/**
 * Fragment responsible for the edit of profile information.
 */
public class EditProfileInfo extends Fragment {

    private ImageView profileImageView;
    private Bitmap profileImageViewBitmap;
    private TextInputEditText mName;
    private Spinner mUniversity, mCourse;
    private ImagePicker imagePicker;
    private String university, course;
    private String oldUniversity, oldCourse;
    private ArrayList<String> universities, courses;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.edit_profile_info_layout, container, false);
        setHasOptionsMenu(true);

        mName = view1.findViewById(R.id.name);
        profileImageView = view1.findViewById(R.id.profileImage);
        mUniversity = view1.findViewById(R.id.university);
        mCourse = view1.findViewById(R.id.course);
        Button mButtonOk = view1.findViewById(R.id.button);
        FloatingActionButton floatingActionButton = view1.findViewById(R.id.imagePicker);

        // load profile image
        DataManager.getInstance().loadImageIntoView(Uri.parse(DataManager.getInstance().getUser().getPhotosUrl()), profileImageView, getContext());
        // set the current data of user
        mName.setText(DataManager.getInstance().getUser().getName());

        oldUniversity = DataManager.getInstance().getUser().getUniversity();
        university = DataManager.getInstance().getUser().getUniversity();

        oldCourse = DataManager.getInstance().getUser().getCourse();
        course = DataManager.getInstance().getUser().getCourse();

        universities = DataManager.getInstance().getAllUniversities();
        courses = DataManager.getInstance().getAllCourses(university);

        mButtonOk.setOnClickListener(v -> saveData());
        floatingActionButton.setOnClickListener(view -> {
            imagePicker = new ImagePicker(this);
            imagePicker.showChoicePopup();
        });

        setupSpinners();

        return view1;
    }

    private void setupSpinners(){
        ArrayAdapter adapterUniversity = new ArrayAdapter<>(getContext(), R.layout.spinner_item, universities);
        adapterUniversity.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mUniversity.setAdapter(adapterUniversity);
        mUniversity.setSelection(universities.indexOf(university));

        ArrayAdapter adapterCourses = new ArrayAdapter<>(getContext(), R.layout.spinner_item, courses);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Save data into database.
     */
    private void saveData() {
        if(mName.getText() != null && mName.getText().toString().length() > 1 && mName.getText().toString().split(" ").length > 1) {
            DataManager.getInstance().getUser().setName(String.valueOf(mName.getText()));
            DataManager.getInstance().getUser().setUniversity(university);
            DataManager.getInstance().getUser().setCourse(course);

            if (!oldCourse.equalsIgnoreCase(course) || !oldUniversity.equalsIgnoreCase(university)){
                DataManager.getInstance().getUser().setExams(new ArrayList<>());
                DataManager.getInstance().downloadAllExams();
                App.setAuxiliarViewsStatus(Constants.NO_VIEW_ACTIVE);
            }

            if(profileImageViewBitmap != null)
                DataManager.getInstance().uploadImageWithImage(profileImageViewBitmap, (KaoriApp) getActivity());
            else
                DataManager.getInstance().updateUser();

            if (getActivity() != null)
                getActivity().getSupportFragmentManager().popBackStackImmediate();
        } else {
            LogManager.getInstance().showVisualMessage("Nome e cogonome non validi.");
        }
    }

    /**
     * Handles the result coming from the activity that choose the image.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.PICK_IMAGE && data != null && data.getData() != null) {
            try {
                profileImageViewBitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()));
                DataManager.getInstance().loadImageIntoView(profileImageViewBitmap, profileImageView, getContext());
            } catch (FileNotFoundException e) {
                LogManager.getInstance().showVisualMessage("Il file non è stato trovato.");
            }
        }
        else
            LogManager.getInstance().showVisualError( null, getString(R.string.no_image_selected));
    }

    /**
     * Handle the camera permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                imagePicker.showChoicePopup();
            else
                LogManager.getInstance().showVisualMessage("Non hai concetto i permessi.");
    }

}