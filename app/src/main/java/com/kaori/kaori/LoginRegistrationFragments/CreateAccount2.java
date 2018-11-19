package com.kaori.kaori.LoginRegistrationFragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kaori.kaori.Constants;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateAccount2 extends Fragment {

    /**
     * Constants.
     */
    private final int PICK_IMAGE = 0;

    /**
     * Variables.
     */
    private User user;
    private CircleImageView profileImage;
    private EditText name, surname, password, mail;
    private Button createNewAccount;
    private boolean[] validFields = new boolean[4];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg2, container, false);

        // show the title bar
        if(getActivity() != null && isAdded())
            ((Kaori) getActivity()).getSupportActionBar().setTitle(Constants.titleRegistrationForm);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        createNewAccount = view.findViewById(R.id.button_create_new_account);
        name = view.findViewById(R.id.reg_name);
        surname = view.findViewById(R.id.reg_surname);
        password = view.findViewById(R.id.reg_password);
        mail = view.findViewById(R.id.reg_mail);
        profileImage = view.findViewById(R.id.reg_image_profile);

        // disable the button
        createNewAccount.setEnabled(false);
        createNewAccount.setAlpha(0.5f);

        // set to false all the field validity flags.
        for(int i = 0; i < validFields.length; i++)
            validFields[i] = false;

        setEditTextWatchers();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageFromLibrary();
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
    /*package-private*/ void setUser(User user){
        this.user = user;
    }

    /**
     * Update the user information before passing to the next
     * fragment.
     */
    private void updateUser(){
        user.setName(name.getText().toString());
        user.setSurname(surname.getText().toString());
        user.setEmail(mail.getText().toString());
        // TODO: gestire la mancanza del photoURI
    }

    /**
     * Calls the activity that allows to select an image
     * from the library.
     */
    private void getImageFromLibrary(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    /**
     * Checks that all the fields are valid and then
     * set the button clickable.
     */
    private void updateButtonState(){
        boolean flag = true;
        for (boolean validField : validFields) flag = flag && validField;
        createNewAccount.setAlpha(flag ? 1 : 0.5f);
    }

    /**
     * Set the watchers for all the edit text.
     * The watchers are responsible for checking the
     * correctness of what is entered by the user.
     */
    private void setEditTextWatchers(){
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
                if(editable.length() < 1) {
                    name.setError("Hai lasciato il campo vuoto.");
                    validFields[0] = false;
                }
                else
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
                if(editable.length() < 1) {
                    surname.setError("Hai lasciato il campo vuoto");
                    validFields[1] = false;
                }
                else
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
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(editable.toString()).matches()) {
                    mail.setError("Indirizzo mail non valido.");
                    validFields[2] = false;
                }
                else
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
                if(editable.length() < 6) {
                    password.setError("Password di almeno 6 caratteri.");
                    validFields[3] = false;
                }
                else
                    validFields[3] = true;
                updateButtonState();
            }
        });
    }

    /**
     * Handles the result coming from the activity that choose the image.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            if(data != null){
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
        }
    }

}
