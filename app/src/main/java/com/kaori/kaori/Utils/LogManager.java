package com.kaori.kaori.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kaori.kaori.R;

/**
 * This class is responsible of throwing errors to the user.
 */
public class LogManager {

    /**
     * Variables.
     */
    private static LogManager logManager;
    private static Snackbar snackbar;
    private static View view;

    /**
     * Private constructor.
     */
    private LogManager(){
        super();
    }

    /**
     * Return the instance of the log manager.
     */
    public static LogManager getInstance(){
        if(logManager == null)
            logManager = new LogManager();
        return logManager;
    }

    public static void setView(View v){
        view = v;
    }

    /**
     * Print the error in the console.
     */
    @SuppressLint("LogNotTimber")
    public void printConsoleError(String message){
        Log.e(Constants.TAG, message);
    }

    /**
     * Print a message in the console.
     */
    @SuppressLint("LogNotTimber")
    public void printConsoleMessage(String message){
        Log.d(Constants.TAG, message);
    }

    /**
     * Show the error message and the exception visually
     * and in the console.
     */
    @SuppressLint("LogNotTimber")
    public void showVisualError(Context context, Exception e, String message){
        if(e == null)
            Log.e(Constants.TAG, message);
        else
            Log.e(Constants.TAG, message + " -> " + e.getMessage());

        if(view != null) {
            Snackbar snackbar = Snackbar.make(view.findViewById(R.id.coordinator), message, Snackbar.LENGTH_LONG);
            snackbar.show();
        } else
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show a message and the exception visually
     * and in the console.
     */
    @SuppressLint("LogNotTimber")
    public void showVisualMessage(Context context, String message){
        Log.d(Constants.TAG, message);
        if(view != null) {
            Snackbar snackbar = Snackbar.make(view.findViewById(R.id.coordinator), message, Snackbar.LENGTH_LONG);
            snackbar.show();
        } else
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}


