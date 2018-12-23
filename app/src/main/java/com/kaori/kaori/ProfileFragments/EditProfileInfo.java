package com.kaori.kaori.ProfileFragments;

import android.app.Activity;
import android.content.Context;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaori.kaori.KaoriApp;
import com.kaori.kaori.Model.Course;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.ImagePicker;
import com.kaori.kaori.Utils.LogManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import static com.kaori.kaori.Utils.Constants.CAMERA_REQUEST;
import static com.kaori.kaori.Utils.Constants.MY_CAMERA_PERMISSION_CODE;

/**
 * Fragment responsible for the edit of profile information.
 */
public class EditProfileInfo extends Fragment {

    /**
     * Variables.
     */
    private View view;
    private AutoCompleteTextView mUniversity, mCourse;
    private Button mButtonOk;
    private FirebaseFirestore db;
    private ArrayList<Course> allCourses;
    private ArrayList<String> universities, courses;
    private Context context;
    private String selectedUniversity, selectedCourse;
    private ImageView profileImageView;
    private Bitmap profileImageViewBitmap;
    private Uri filePath;
    private ImagePicker imagePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_profile_info_layout, container, false);
        context = getContext();
        setHasOptionsMenu(true);
        ((KaoriApp)getActivity()).hideBottomBar(true);

        EditText mName = view.findViewById(R.id.profile_name);
        mUniversity = view.findViewById(R.id.profile_university);
        mCourse = view.findViewById(R.id.profile_course);
        mButtonOk = view.findViewById(R.id.button_ok);
        profileImageView = view.findViewById(R.id.profile_image);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);

        // load profile image
        loadImageIntoImageView(Uri.parse(DataManager.getInstance().getUser().getPhotosUrl()));

        db = FirebaseFirestore.getInstance();
        imagePicker = new ImagePicker(getActivity(), this);
        loadData();

        // pre-set
        mName.setText(DataManager.getInstance().getUser().getName());
        mUniversity.setText(DataManager.getInstance().getUser().getUniversity());
        mCourse.setText(DataManager.getInstance().getUser().getCourse());
        if(DataManager.getInstance().getUser().getCourse() != null) {
            selectedUniversity = DataManager.getInstance().getUser().getUniversity();
            selectedCourse = DataManager.getInstance().getUser().getCourse();
            setViewState(mCourse, true);
            setViewState(mButtonOk, true);
        }

        setTextWatchers();

        mButtonOk.setOnClickListener(v -> {
            view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
            saveData();
        });

        floatingActionButton.setOnClickListener(view -> imagePicker.showChoicePopup());

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.empty_menu, menu);
    }

    private void setTextWatchers(){
        //set listeners
        mUniversity.setOnItemClickListener((adapterView, view14, i, l) -> {
            setViewState(mCourse, true);

            selectedUniversity = String.valueOf(adapterView.getItemAtPosition(i));
            filterCoursesByUniversity();
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
    }

    private void filterCoursesByUniversity(){
        courses = new ArrayList<>();
        for(Course c : allCourses)
            if(c.getUniversity().equalsIgnoreCase(selectedUniversity))
                courses.add(c.getName());

        mCourse.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, courses));
    }

    /**
     * Save data into database.
     */
    private void saveData() {
        view.findViewById(R.id.wait_layout).setVisibility(View.VISIBLE);

        User user = DataManager.getInstance().getUser();
        user.setUniversity(selectedUniversity);
        user.setCourse(selectedCourse);
        user.setExams(new ArrayList<>());

        if(profileImageViewBitmap != null) {
            uploadProfileImageOnTheServer(user);
        } else {
            uploadNewUserOnTheServer(user);
        }
    }

    /**
     * This method writes the user in the database.
     */
    private void uploadNewUserOnTheServer(User user) {
        LogManager.getInstance().printConsoleMessage("uploadNewUserOnTheServer");
        db.collection(Constants.DB_COLL_USERS)
            .document(user.getUid()).set(user)
            .addOnSuccessListener(aVoid -> {
                LogManager.getInstance().printConsoleMessage("saveData:written");
                if(getActivity() != null)
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
            })
            .addOnFailureListener(e -> {
                LogManager.getInstance().showVisualError(e, "saveData:fail");
                if(getActivity() != null)
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
            });
    }

    /**
     * This method uploads the image in the Firebase storage.
     */
    private void uploadProfileImageOnTheServer(User user){
        LogManager.getInstance().printConsoleMessage("uploadProfileImageOnTheServer");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profileImageViewBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        final StorageReference mStorage = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PATH_PROFILE_IMAGES + user.getUid());
        UploadTask uploadTask = mStorage.putBytes(baos.toByteArray());
        uploadTask.addOnSuccessListener(taskSnapshot -> mStorage.getDownloadUrl().addOnSuccessListener(uri -> {
            LogManager.getInstance().printConsoleMessage("uploadProfileImageOnTheServer:success");
            user.setPhotosUrl(uri.toString());
            uploadNewUserOnTheServer(user);
        }))
        .addOnFailureListener(e -> {
            LogManager.getInstance().showVisualError(e, "uploadProfileImageOnTheServer:error");
            //endSignIn(false);
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
                    if(selectedUniversity != null && selectedCourse != null)
                        filterCoursesByUniversity();
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

    /**
     * Handles the result coming from the activity that choose the image.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && BitmapFactory.decodeFile(String.valueOf(imagePicker.getFilePath()), new BitmapFactory.Options()) != null) {
            filePath = imagePicker.getFilePath();
            profileImageViewBitmap = imagePicker.editImage(filePath.toString());
            loadImageIntoImageView(profileImageViewBitmap);
        }
        else if(data != null && data.getData() != null && requestCode == Constants.PICK_IMAGE) {
            if(getActivity() != null) {
                try {
                    profileImageViewBitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()));
                } catch (FileNotFoundException e) {
                    //TODO
                }
                loadImageIntoImageView(profileImageViewBitmap);
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
                // TODO: eliminare
                LogManager.getInstance().printConsoleError("Permessi non dati.");
    }

    private void loadImageIntoImageView(Uri uri){
        if(getContext() != null)
            Glide.with(getContext())
                    .load(uri)
                    .apply(DataManager.getInstance().getGetGlideRequestOptionsCircle())
                    .into(profileImageView);
    }

    private void loadImageIntoImageView(Bitmap uri){
        if(getContext() != null)
            Glide.with(getContext())
                    .load(uri)
                    .apply(DataManager.getInstance().getGetGlideRequestOptionsCircle())
                    .into(profileImageView);
    }

}
