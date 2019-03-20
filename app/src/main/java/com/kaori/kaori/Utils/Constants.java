package com.kaori.kaori.Utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * This class contains all the constants of the app.
 */
public class Constants {

    /**
     * Log titles.
     */
    public static final String TAG = "KaoriDebug";

    /**
     * Camera constants.
     */
    public static final int MY_CAMERA_PERMISSION_CODE = 100;

    /**
     * Document types.
     */
    public static final int BOOK = 1;
    public static final int FILE = 2;
    public static final int URL = 3;

    /**
     * Storage constants.
     */
    public static final String REMOTE_STORAGE_PATH = "gs://kaori-c5a43.appspot.com";
    public static final String INTERNAL_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator;
    public static final String STORAGE_PATH_PROFILE_IMAGES = "profile_images/";
    public static final String STORAGE_PATH_UPLOADS = "uploads/";
    public static final String DATABASE_PATH_UPLOADS = "uploads";
    public static final String KAORI_SHARED_PREFERENCES = "kaori_shared";
    public static final String STORAGE_DEFAULT_PROFILE_IMAGE = "https://firebasestorage.googleapis.com/v0/b/kaori-c5a43.appspot.com/o/profile_images%2Fdefault.png?alt=media&token=643f78ef-1681-43ae-bf52-e98d173849b4";

    /**
     * Numerical constants.
     */
    public static int SPLASH_SCREEN_WAITING_TIME = 0;

    /**
     * Sign in and login constants.
     */
    public static final int GOOGLE_LOGIN_REQUEST = 0;
    public static final int GOOGLE_SIGNIN_REQUEST = 1;
    public static final int PICK_IMAGE = 2;
    public static final int CAMERA_REQUEST = 3;
    public static final int LOGIN = 0;
    public static final int SIGNIN = 1;
    public static final int FACEBOOK = 10;
    public static final int GOOGLE = 11;
    public static final int NATIVE = 12;
    public static final int USER_NOT_EXISTS = 1000;
    public static final int USER_EXISTS_AND_CORRECT_METHOD = 1001;

    /**
     * Registration messages.
     */
    public static final String DIALOG_TITLE_CONFIRM = "Sei sicuro di procedere?";
    public static final String DIALOG_MESSAGE_CONFIRM = "Premendo Ok completerai la tua registazione e riceverai una mail di conferma. Premendo su ANNULLA potrai modificare i tuoi dati.";

    /**
     * Finder fragments constants.
     */
    public static final String STATEMENT = "Ciao, sto studiando qui: \n";

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ITALY);
    public static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
    public static final SimpleDateFormat dateFormat3 = new SimpleDateFormat("HH:mm", Locale.ITALY);

}
