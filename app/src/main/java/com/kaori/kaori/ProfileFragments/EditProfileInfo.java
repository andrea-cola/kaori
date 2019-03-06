package com.kaori.kaori.ProfileFragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.ImagePicker;
import com.kaori.kaori.Utils.LogManager;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import static com.kaori.kaori.Utils.Constants.CAMERA_REQUEST;
import static com.kaori.kaori.Utils.Constants.MY_CAMERA_PERMISSION_CODE;

/**
 * Fragment responsible for the edit of profile information.
 */
public class EditProfileInfo extends Fragment {

    private ImageView profileImageView;
    private Bitmap profileImageViewBitmap;
    private Spinner mUniversity, mCourse;
    private ImagePicker imagePicker;
    private String university, course;
    private ArrayList<String> universities, courses;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.edit_profile_info_layout, container, false);
        setHasOptionsMenu(true);

        EditText mName = view1.findViewById(R.id.profile_name);
        profileImageView = view1.findViewById(R.id.profile_image);
        mUniversity = view1.findViewById(R.id.profile_university);
        mCourse = view1.findViewById(R.id.profile_course);
        Button mButtonOk = view1.findViewById(R.id.button_ok);
        FloatingActionButton floatingActionButton = view1.findViewById(R.id.floatingActionButton);

        // load profile image
        DataManager.getInstance().loadImageIntoView(Uri.parse(DataManager.getInstance().getUser().getPhotosUrl()), profileImageView, getContext());
        // set the current data of user
        mName.setText(DataManager.getInstance().getUser().getName());

        university = DataManager.getInstance().getUser().getUniversity();
        course = DataManager.getInstance().getUser().getCourse();
        universities = DataManager.getInstance().getAllUniversities();
        courses = DataManager.getInstance().getAllCourses(university);

        mButtonOk.setOnClickListener(v -> saveData());
        floatingActionButton.setOnClickListener(view -> {
            imagePicker = new ImagePicker(getActivity(), this);
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

        ArrayAdapter adapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, courses);
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mCourse.setAdapter(adapter2);
        mCourse.setSelection(courses.indexOf(course));

        mUniversity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!university.equalsIgnoreCase(mUniversity.getItemAtPosition(i).toString())) {
                    university = mUniversity.getItemAtPosition(i).toString();
                    courses = DataManager.getInstance().getAllCourses(university);
                    adapter2.notifyDataSetChanged();
                    mCourse.setSelection(0);
                }
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
        DataManager.getInstance().getUser().setUniversity(university);
        DataManager.getInstance().getUser().setCourse(course);
        DataManager.getInstance().getUser().setExams(new ArrayList<>());
        DataManager.getInstance().updateUser(profileImageViewBitmap);
        if(getActivity() != null)
            getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    /**
     * Handles the result coming from the activity that choose the image.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && BitmapFactory.decodeFile(String.valueOf(imagePicker.getFilePath()), new BitmapFactory.Options()) != null) {
            profileImageViewBitmap = imagePicker.editImage(imagePicker.getFilePath().toString());
            DataManager.getInstance().loadImageIntoView(profileImageViewBitmap, profileImageView, getContext());
        }
        else if(requestCode == Constants.PICK_IMAGE && data != null && data.getData() != null) {
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