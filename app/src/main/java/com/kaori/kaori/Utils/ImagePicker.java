package com.kaori.kaori.Utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

public class ImagePicker {

    private Activity activity;
    private Fragment fragment;
    private Uri filePath;

    public ImagePicker(Activity activity, Fragment fragment) {
        this.activity = activity;
        this.fragment = fragment;
    }

    public void showChoicePopup() {
        getImageFromLibrary();
    }

    private void getImageFromLibrary() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_IMAGE);
    }

}
