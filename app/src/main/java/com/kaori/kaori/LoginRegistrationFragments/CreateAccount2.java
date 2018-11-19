package com.kaori.kaori.LoginRegistrationFragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kaori.kaori.Constants;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.kaori.kaori.Constants.MY_CAMERA_PERMISSION_CODE;

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
    private CircleImageView profileImage;
    private EditText name, surname, password, mail;
    private Button createNewAccount;
    private boolean[] validFields = new boolean[4];
    private String mCurrentPhotoPath;

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

        // show the title bar
        if (getActivity() != null && isAdded())
            ((Kaori) getActivity()).getSupportActionBar().setTitle(Constants.titleRegistrationForm);

        // disable the button
        createNewAccount.setEnabled(false);
        createNewAccount.setAlpha(0.5f);

        // set to false all the field validity flags.
        for (int i = 0; i < validFields.length; i++)
            validFields[i] = false;

        setEditTextWatchers();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getImageFromLibrary();
                buildChoicePopup();
            }
        });

        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUser();
                // go the next fragment
            }
        });

        return view;
    }

    /**
     * Let the previous fragment to pass the user.
     */
    /*package-private*/ void setUser(User user) {
        this.user = user;
    }

    /**
     * Update the user information before passing to the next
     * fragment.
     */
    private void updateUser() {
        user.setName(name.getText().toString());
        user.setSurname(surname.getText().toString());
        user.setEmail(mail.getText().toString());
        // TODO: gestire la mancanza del photoURI
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

    private void buildChoicePopup() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.popup_menu, null);
        ((TextView) view.findViewById(R.id.popup_title)).setText("Choose an option:");
        ((Button) view.findViewById(R.id.popup_voice1)).setText("From camera");
        ((Button) view.findViewById(R.id.popup_voice2)).setText("From gallery");

        builderSingle.setView(view);
        final AlertDialog alert = builderSingle.create();

        Button cancel = view.findViewById(R.id.popup_cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        Button choice1 = view.findViewById(R.id.popup_voice1);
        choice1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.kaori.kaori.fileprovider", photoFile);
                            Log.d(Constants.TAG, photoURI.toString());
                            user.setPhotosUrl(photoURI);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                        }
                    }
                }
                alert.dismiss();
            }
        });

        Button choice2 = view.findViewById(R.id.popup_voice2);
        choice2.setOnClickListener(new View.OnClickListener() {
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
        if (requestCode == PICK_IMAGE) {
            if (data != null) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                    profileImage.setImageBitmap(bitmap);
                    user.setPhotosUrl(selectedImage);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Caricamento fallito", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Nessuna immagine selezionata", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), user.getPhotosUrl());
                Matrix matrix = new Matrix();

                matrix.postRotate(90);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                profileImage.setImageBitmap(rotatedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(getActivity(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
