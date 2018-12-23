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
     * Database constants.
     */
    public static final String DB_COLL_UNIVERSITIES = "universities";
    public static final String DB_COLL_COURSE_TYPES = "courses";
    public static final String DB_COLL_POSITIONS = "positions";
    public static final String DB_COLL_MATERIALS = "materials";
    public static final String DB_COLL_EXAMS = "exams";
    public static final String DB_COLL_COURSES = "courses";
    public static final String DB_COLL_USERS = "users";
    public static final String DB_COLL_PROFESSORS = "professors";
    public static final String DB_COLL_MESSAGES = "messages";
    public static final String DB_SUBCOLL_MESSAGES = "chat";
    public static final String FIELD_UNIVERSITY = "university";
    public static final String FIELD_COURSES = "course";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_TOKEN = "tokenID";
    public static final String FIELD_EXAM = "exam";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_LAST_MESSAGE = "lastMessageSent";
    public static final String FIELD_MINI_USER = "miniUser";
    public static final String FIELD_UID = "uid";
    public static final String LIBRO = "Libro";
    public static final String FILE = "File";
    public static final String URL = "Url";

    /**
     * Storage constants.
     */
    public static final String STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator;
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

    /**
     * Registration messages.
     */
    public static final String DIALOG_TITLE_CONFIRM = "Sei sicuro di procedere?";
    public static final String DIALOG_MESSAGE_CONFIRM = "Premendo Ok completerai la tua registazione e riceverai una mail di conferma. Premendo su ANNULLA potrai modificare i tuoi dati.";

    /**
     * Finder fragments constants.
     */
    public static final String STATEMENT = "Hi, there! I'm studying here:";

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY);

}
