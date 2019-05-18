package com.kaori.kaori;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class contains all the constants of the app.
 */
public class Constants {

    public static final int WAIT_VIEW_ACTIVE = -1;
    public static final int EMPTY_VIEW_ACTIVE = 0;
    public static final int NO_VIEW_ACTIVE = 1;

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
    public static final String INTERNAL_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator;
    public static final String STORAGE_PATH_PROFILE_IMAGES = "profile_images/";
    public static final String STORAGE_PATH_UPLOADS = "uploads/";
    public static final String STORAGE_DEFAULT_PROFILE_IMAGE = "https://firebasestorage.googleapis.com/v0/b/kaori-c5a43.appspot.com/o/profile_images%2Fdefault.png?alt=media&token=643f78ef-1681-43ae-bf52-e98d173849b4";

    /**
     * Numerical constants.
     */
    public static int SPLASH_SCREEN_WAITING_TIME = 0;

    /**
     * Sign in and login constants.
     */
    public static final int GOOGLE_LOGIN_REQUEST = 0;
    public static final int PICK_IMAGE = 2;
    public static final int FACEBOOK = 10;
    public static final int GOOGLE = 11;
    public static final int NATIVE = 12;
    public static final int USER_NOT_EXISTS = 0;
    public static final int USER_EXISTS= 1;
    public static final String WRONG_PROVIDER = "Login fallito. L'ultima volta hai usato ";
    public static final String WRONG_AUTH_METHOD = "Metodo di autenticazione diverso da quello utilizzato per registrarsi.";
    public static final String GENERIC_ERROR = "Impossibile contattare il server";
    public static final String WRONG_CREDENTIALS = "Le tue credenziali sono errate";
    public static final String USER_NOT_EXISTS_ERROR = "Non sei registrato";
    public static final String LOGIN_SUCCESS = "Login effettuato";
    public static final String FACEBOOK_ERROR = "Impossibile contattare Facebook";
    public static final String NEW_USER_CREATION_ERROR = "Errore durante la creazione del nuovo utente";
    public static final String USER_ALREADY_EXISTS = "L'utente è già esistente";

    /**
     * Finder fragments constants.
     */
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.ITALY);
    public static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.ITALY);
    public static final long constantDate = 1000L;

    public static String getDate(long timestamp){
        return dateFormat.format(new Date(timestamp * constantDate));
    }

    public static String getFormattedDate(long timestamp){
        Calendar today = Calendar.getInstance(Locale.ITALIAN);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar yesterday = Calendar.getInstance();
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        Date myDate = new Date(timestamp * constantDate);
        if(today.getTime().before(myDate))
            return "Oggi, " + hourFormat.format(myDate);
        else if(yesterday.getTime().before(myDate))
            return "Ieri, " + hourFormat.format(myDate);
        return dateFormat.format(myDate);
    }

    public static String getHour(long timestamp){
        return hourFormat.format(new Date(timestamp * constantDate));
    }

    /**
     * Functions.
     */
    public static String translateResponseCode(int code){
        switch (code) {
            case FACEBOOK:
                return "Facebook";
            case GOOGLE:
                return "Google";
            default:
                return "email e password";
        }
    }

    public static String translateTypeCode(int code){
        switch (code) {
            case BOOK:
                return "libro";
            case FILE:
                return "documento";
            default:
                return "link";
        }
    }

}
