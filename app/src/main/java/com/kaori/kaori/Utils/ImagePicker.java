package com.kaori.kaori.Utils;

import android.content.Intent;
import android.support.v4.app.Fragment;

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
