package com.kaori.kaori.LoginRegistrationFragments;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.AuthMethods;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.ImagePicker;
import com.kaori.kaori.Utils.LogManager;

import java.io.FileNotFoundException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.kaori.kaori.Utils.Constants.CAMERA_REQUEST;
import static com.kaori.kaori.Utils.Constants.MY_CAMERA_PERMISSION_CODE;

/**
 * This class is responsible for the second step of account creation.
 */
public class CreateAccountWithEmail extends Fragment {

    /**
     * Constants.
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Variables.
     */
    private boolean[] validFields = new boolean[3];
    private User user;
    private CircleImageView profileImage;
    private EditText name, password, mail;
    private Button createNewAccount;
    private Bitmap profileImageBitmap;
    private ImagePicker imagePicker;

    /**
     * Override of the inherited method.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg2, container, false);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        createNewAccount = view.findViewById(R.id.button_ok);
        name = view.findViewById(R.id.name);
        password = view.findViewById(R.id.password);
        mail = view.findViewById(R.id.username);
        profileImage = view.findViewById(R.id.reg_image_profile);

        user = new User();
        user.setPhotosUrl(Constants.STORAGE_DEFAULT_PROFILE_IMAGE);

        // set to false all the field validity flags.
        for (int i = 0; i < validFields.length; i++)
            validFields[i] = false;

        // add listener to changes in EditText.
        setEditTextWatchers();

        imagePicker = new ImagePicker(getActivity(), this);
        // add the action to the FAB.
        floatingActionButton.setOnClickListener(view1 -> imagePicker.showChoicePopup());

        createNewAccount.setEnabled(false);
        createNewAccount.setOnClickListener(view12 -> updateUserAndLogin());

        return view;
    }

    /**
     * Update the user information before passing to the next
     * fragment.
     */
    private void updateUserAndLogin() {
        user.setName(name.getText().toString());
        user.setEmail(mail.getText().toString());

        Object[] o = new Object[3];
        o[0] = user;
        o[1] = String.valueOf(password.getText());
        o[2] = profileImageBitmap;

        LoginWaitFragment loginWaitFragment = new LoginWaitFragment();
        loginWaitFragment.setParameters(Constants.SIGNIN, AuthMethods.NATIVE, o);

        if(getActivity() != null && isAdded())
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_layout, loginWaitFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
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
                    validFields[1] = false;
                } else
                    validFields[1] = true;
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
                    validFields[2] = false;
                } else
                    validFields[2] = true;
                updateButtonState();
            }
        });
    }

    /**
     * Handles the result coming from the activity that choose the image.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST) {
            profileImageBitmap = imagePicker.editImage(String.valueOf(imagePicker.getFilePath()));
            profileImage.setImageBitmap(profileImageBitmap);
        }
        else if(data != null && data.getData() != null && requestCode == Constants.PICK_IMAGE)
            try {
                if(getActivity() != null) {
                    profileImageBitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()));
                    profileImage.setImageBitmap(profileImageBitmap);
                }
            } catch (FileNotFoundException e) {
                LogManager.getInstance().showVisualError(getContext(), e, getString(R.string.file_not_found));
            }
        else
            LogManager.getInstance().showVisualError(getContext(), null, getString(R.string.no_image_selected));
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

}
