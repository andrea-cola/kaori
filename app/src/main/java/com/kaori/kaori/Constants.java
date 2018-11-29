package com.kaori.kaori;

/**
 * This class contains all the constants of the app.
 */
public class Constants {

    /**
     * Screen titles.
     */
    public static final String titleSignIn = "Sign In";
    public static final String titleCreate = "Create new account";
    public static final String titleRegistrationForm = "Registration Form";
    public static final String titleProfileInformation = "Profile Information";
    public static final String titleSplashScreen = "Loading";

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
    public static final String DB_COLL_COURSE_TYPES = "course_types";
    public static final String DB_COLL_COURSES = "exams";
    public static final String DB_COLL_USERS = "users";

    /**
     * Storage constants.
     */
    public static final String STORAGE_PATH_PROFILE_IMAGES = "profile_images/";
    public static final String STORAGE_PATH_UPLOADS = "uploads/";
    public static final String DATABASE_PATH_UPLOADS = "uploads";
    public static final String KAORI_SHARED_PREFERENCES = "kaori_shared";
    public static final String STORAGE_DEFAULT_PROFILE_IMAGE = "https://firebasestorage.googleapis.com/v0/b/kaori-c5a43.appspot.com/o/profile_images%2Fdefault.png?alt=media&token=643f78ef-1681-43ae-bf52-e98d173849b4";

    /**
     * Numerical constants.
     */
    public static int SPLASH_SCREEN_WAITING_TIME = 1000;

    /**
     * Sign in method.
     */
    public static int NATIVE_SIGNIN = 0;
    public static int SOCIAL_SIGNIN = 1;

}
