package com.kaori.kaori.Utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.View;

import com.kaori.kaori.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.kaori.kaori.Utils.Constants.CAMERA_REQUEST;
import static com.kaori.kaori.Utils.Constants.MY_CAMERA_PERMISSION_CODE;

/**
 * Picks an image from the library or from the camera.
 */
public class ImagePicker {

    /**
     * Variables.
     */
    private Activity activity;
    private Fragment fragment;
    private Uri filePath;

    public ImagePicker(Activity activity, Fragment fragment) {
        this.activity = activity;
        this.fragment = fragment;
    }

    /**
     * This method creates a popup letting the user either choose from
     * the gallery or take a photo with the camera.
     */
    public void showChoicePopup() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.camera_popup_menu, null);

        builderSingle.setView(view);
        final AlertDialog alert = builderSingle.create();

        // dismiss the dialog
        view.findViewById(R.id.popup_cancel_button).setOnClickListener(v -> alert.dismiss());

        view.findViewById(R.id.popup_voice1).setOnClickListener(v -> {
            if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                activity.requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            else {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                    try {
                        File photoFile = createImageFile();
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(activity, activity.getString(R.string.file_provider_authority), photoFile);
                            LogManager.getInstance().printConsoleMessage(photoURI.toString());
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            takePictureIntent.putExtra("path", photoURI);
                            fragment.startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                        }
                    } catch (IOException ex) {
                        LogManager.getInstance().showVisualError(activity, ex, "");
                    }
                }
            }
            alert.dismiss();
        });

        view.findViewById(R.id.popup_voice2).setOnClickListener(v -> {
            getImageFromLibrary();
            alert.dismiss();
        });

        alert.show();
    }

    /**
     * Edit the image, rotating it if necessary.
     */
    public Bitmap editImage(String path){
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(path, opts);

        try {
            ExifInterface exif = new ExifInterface(path);
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

            Matrix matrix = new Matrix();
            matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
            bm = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        } catch (IOException e){
            LogManager.getInstance().printConsoleError(e.getMessage());
        }

        return bm;
    }

    /**
     * Calls the activity that allows to select an image
     * from the library.
     */
    private void getImageFromLibrary() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_IMAGE);
    }

    /**
     * Create a new image file and set the absolute path.
     */
    @SuppressLint("SimpleDateFormat")
    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageFileName, ".jpg", storageDir);
        filePath = Uri.parse(file.getAbsolutePath());
        return file;
    }

    /**
     * Return the URI of the generated file.
     */
    public Uri getFilePath() {
        return filePath;
    }
}
