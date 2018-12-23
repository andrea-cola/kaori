package com.kaori.kaori.Utils;

import android.annotation.SuppressLint;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.kaori.kaori.R;

/**
 * This class is responsible of throwing errors to the user.
 */
public class LogManager {

    /**
     * Variables.
     */
    private static LogManager logManager;
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
    public void showVisualError(Exception e, String message){
        Log.e(Constants.TAG, e == null ? message : message + " -> " + e.getMessage());

        if(view != null) Snackbar.make(view.findViewById(R.id.coordinator), message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Show a message and the exception visually
     * and in the console.
     */
    @SuppressLint("LogNotTimber")
    public void showVisualMessage(String message){
        Log.d(Constants.TAG, message);

        if(view != null) Snackbar.make(view.findViewById(R.id.coordinator), message, Snackbar.LENGTH_LONG).show();
    }

}