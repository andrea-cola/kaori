package com.kaori.kaori.Services;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.kaori.kaori.Constants;

public class ImagePicker {

    private Fragment fragment;

    public ImagePicker(Fragment fragment) {
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
