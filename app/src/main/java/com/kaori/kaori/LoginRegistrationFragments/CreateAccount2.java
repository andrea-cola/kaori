package com.kaori.kaori.LoginRegistrationFragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.Constants;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.SignInManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.kaori.kaori.Constants.MY_CAMERA_PERMISSION_CODE;

/**
 * This class is responsible for the second step of account creation.
 */
public class CreateAccount2 extends Fragment {

    /**
     * Constants.
     */
    private final int PICK_IMAGE = 0;
    private final int CAMERA_REQUEST = 1;

    /**
     * Variables.
     */
    private User user;
    private boolean[] validFields = new boolean[4];
    private CircleImageView profileImage;
    private EditText name, surname, password, mail;
    private Button createNewAccount;
    private FirebaseAuth auth;
    private Bitmap profileImageBitmap;
    private Uri filePath;
    private FirebaseFirestore db;
    private StorageReference mStorage;
    private ProgressDialog pd;

    /**
     * Override of the inherited method.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg2, container, false);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        createNewAccount = view.findViewById(R.id.button_create_new_account);
        name = view.findViewById(R.id.reg_name);
        surname = view.findViewById(R.id.reg_surname);
        password = view.findViewById(R.id.reg_password);
        mail = view.findViewById(R.id.reg_mail);
        profileImage = view.findViewById(R.id.reg_image_profile);

        user = new User();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // set to false all the field validity flags.
        for (int i = 0; i < validFields.length; i++)
            validFields[i] = false;

        // add listener to changes in edittext.
        setEditTextWatchers();

        // add the action to the FAB.
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildChoicePopup();
            }
        });

        createNewAccount.setEnabled(false);
        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUser();
            }
        });

        return view;
    }

    /**
     * Update the user information before passing to the next
     * fragment.
     */
    private void updateUser() {
        user.setName(name.getText().toString());
        user.setSurname(surname.getText().toString());
        user.setEmail(mail.getText().toString());

        SignInManager signInManager = new SignInManager(getContext());
        signInManager.signInWithEmail(user, password.getText().toString(), profileImageBitmap);
    }

    /**
     * Calls the activity that allows to select an image
     * from the library.
     */
    private void getImageFromLibrary() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    /**
     * Checks that all the fields are valid and then
     * set the button clickable.
     */
    private void updateButtonState() {
        boolean flag = true;
        for (boolean validField : validFields) flag = flag && validField;
        createNewAccount.setAlpha(flag ? 1 : 0.5f);
        createNewAccount.setEnabled(flag);
    }

    /**
     * Set the watchers for all the edit text.
     * The watchers are responsible for checking the
     * correctness of what is entered by the user.
     */
    private void setEditTextWatchers() {
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() < 1) {
                    name.setError("Hai lasciato il campo vuoto.");
                    validFields[0] = false;
                } else
                    validFields[0] = true;
                updateButtonState();
            }
        });

        surname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() < 1) {
                    surname.setError("Hai lasciato il campo vuoto");
                    validFields[1] = false;
                } else
                    validFields[1] = true;
                updateButtonState();
            }
        });

        mail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(editable.toString()).matches()) {
                    mail.setError("Indirizzo mail non valido.");
                    validFields[2] = false;
                } else
                    validFields[2] = true;
                updateButtonState();
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() < 6) {
                    password.setError("Password di almeno 6 caratteri.");
                    validFields[3] = false;
                } else
                    validFields[3] = true;
                updateButtonState();
            }
        });
    }

    /**
     * This method creates a popup letting the user either choose from
     * the gallery or take a photo with the camera.
     */
    private void buildChoicePopup() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.camera_popup_menu, null);

        builderSingle.setView(view);
        final AlertDialog alert = builderSingle.create();

        view.findViewById(R.id.popup_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        view.findViewById(R.id.popup_voice1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                else {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        try {
                            File photoFile = createImageFile();
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.kaori.kaori.fileprovider", photoFile);
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                            }
                        } catch (IOException ex) {
                            // set the default image in case of problems.
                            user.setPhotosUrl(Constants.STORAGE_DEFAULT_PROFILE_IMAGE);
                        }
                    }
                }
                alert.dismiss();
            }
        });

        view.findViewById(R.id.popup_voice2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromLibrary();
                alert.dismiss();
            }
        });

        alert.show();
    }

    /**
     * Handles the result coming from the activity that choose the image.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && filePath != null) {
            profileImageBitmap = BitmapFactory.decodeFile(filePath.toString());
            profileImage.setImageBitmap(profileImageBitmap);
        }
        else if(data != null && data.getData() != null && requestCode == PICK_IMAGE)
            try {
                profileImageBitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()));
                profileImage.setImageBitmap(profileImageBitmap);
            } catch (FileNotFoundException e) {
                // TODO
                Toast.makeText(getActivity(), "Something went wrong.", Toast.LENGTH_LONG).show();
            }
        else {
            // TODO
            Toast.makeText(getContext(), "Error in selecting the image.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle the camera permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(getActivity(), "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Create a new image file and set the absolute path.
     */
    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageFileName, ".jpg", storageDir);
        filePath = Uri.parse(file.getAbsolutePath());
        return file;
    }

}
