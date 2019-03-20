package com.kaori.kaori.LoginRegistrationFragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.ImagePicker;
import com.kaori.kaori.Utils.LogManager;
import com.kaori.kaori.Utils.SignInManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.kaori.kaori.Utils.Constants.MY_CAMERA_PERMISSION_CODE;

public class CreateAccountWithEmail extends Fragment {

    /**
     * Constants.
     */
    private final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{4,}$";
    private final String NAME_PATTERN = "^[a-zA-Z ]*$";

    /**
     * Variables.
     */
    private boolean[] validFields = new boolean[3]; // validity state of the fields
    private byte[] image;
    private ImagePicker imagePicker;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.registration, container, false);
        EditText mName = view.findViewById(R.id.name);
        EditText mPassword = view.findViewById(R.id.password);
        EditText mMail = view.findViewById(R.id.username);
        Button button = view.findViewById(R.id.button);

        // set to false all the field validity flags.
        for (int i = 0; i < validFields.length; i++)
            validFields[i] = false;

        // instantiate the image picker.
        imagePicker = new ImagePicker(getActivity(), this);

        // add the action to FAB and to button
        view.findViewById(R.id.floatingActionButton).setOnClickListener(view1 -> imagePicker.showChoicePopup());
        button.setOnClickListener(view12 -> validateFields(mName, mMail, mPassword));

        return view;
    }

    private void validateFields(EditText mName, EditText mMail, EditText mPassword) {
        String name, mail, password;
        name = mName.getText().toString();
        mail = mMail.getText().toString();
        password = mPassword.getText().toString();

        if(nameValidation(name) && emailValidation(mail) && passwordValidation(password) && imageValidation(image))
            updateUserAndSignin(name, mail, password);
    }

    private boolean nameValidation(final String name){
        if(!Pattern.compile(NAME_PATTERN).matcher(name).matches()){
            LogManager.getInstance().showVisualMessage("Indirizzo mMail non valido.");
            return false;
        }
        return true;
    }

    private boolean emailValidation(final String email){
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            LogManager.getInstance().showVisualMessage("Indirizzo mMail non valido.");
            return false;
        }
        return true;
    }

    private boolean passwordValidation(final String password){
        if (!Pattern.compile(PASSWORD_PATTERN).matcher(password).matches()) {
            LogManager.getInstance().showVisualMessage("Password non valido.");
            return false;
        }
        return true;
    }

    private boolean imageValidation(final byte[] bitmap){
        if(bitmap == null) {
            LogManager.getInstance().showVisualMessage("Nessuna immagine selezionata.");
            return false;
        }
        return true;
    }

    private void updateUserAndSignin(final String name, final String mail, final String password) {
        view.findViewById(R.id.wait_layout).setVisibility(View.VISIBLE);
        User user = new User();
        user.setName(name);
        user.setEmail(mail);
        user.setAuthMethod(Constants.NATIVE);

        Response.Listener<String> listener = response -> {
            LogManager.getInstance().showVisualMessage(response);
            if(response.equalsIgnoreCase("1000"))
                SignInManager.getInstance().signInWithEmail(user, password, image);
            else
                LogManager.getInstance().showVisualMessage("L'utente esiste già.");
        };

        Response.ErrorListener errorListener = error -> {
            LogManager.getInstance().showVisualMessage("Errore nella creazione dell'utente.");
        };

        DataManager.getInstance().checkIfTheUserAlreadyExists(listener, errorListener, mail);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.PICK_IMAGE && data != null && data.getData() != null)
            try {
                if(getActivity() != null) {
                    CircleImageView profileImage = view.findViewById(R.id.reg_image_profile);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap profileImageBitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()));
                    profileImage.setImageBitmap(profileImageBitmap);
                    profileImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    image = stream.toByteArray();
                }
            } catch (FileNotFoundException e) {
                LogManager.getInstance().showVisualError(e, getString(R.string.file_not_found));
            }
        else
            LogManager.getInstance().showVisualError(null, getString(R.string.no_image_selected));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
            imagePicker.showChoicePopup();
    }

}
