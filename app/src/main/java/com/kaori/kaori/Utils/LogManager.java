package com.kaori.kaori.Utils;

import android.annotation.SuppressLint;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.kaori.kaori.App;
import com.kaori.kaori.R;

/**
 * This class is responsible of throwing errors to the user.
 */
public class LogManager {

    /**
     * Variables.
     */
    private static LogManager logManager;
    private View view;
    private static final String EMPTY_MESSAGE = "No message specified";

    /**
     * Private constructor.
     */
    private LogManager(View view){
        super();
        this.view = view;
    }

    public static void initialize(View view) {
        logManager = new LogManager(view);
    }

    /**
     * Return the instance of the log manager
     * and set the Coordinator view.
     */
    public static LogManager getInstance(){
        return logManager;
    }

    /**
     * Print the error in the console.
     */
    @SuppressLint("LogNotTimber")
    public void printConsoleError(String message){
        Log.e(Constants.TAG, message != null ? message : EMPTY_MESSAGE);
    }

    /**
     * Print a message in the console.
     */
    @SuppressLint("LogNotTimber")
    public void printConsoleMessage(String message){
        Log.d(Constants.TAG, message != null ? message : EMPTY_MESSAGE);
    }

    /**
     * Show the error message and the exception visually
     * and in the console.
     */
    @SuppressLint("LogNotTimber")
    public void showVisualError(Exception e, String message){
        Log.e(Constants.TAG, e == null ? message : message + " -> " + e.getMessage());
        Snackbar.make(view.findViewById(R.id.coordinator), message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Show a message and the exception visually
     * and in the console.
     */
    @SuppressLint("LogNotTimber")
    public void showVisualMessage(String message){
        Log.d(Constants.TAG, message);
        Snackbar.make(view.findViewById(R.id.coordinator), message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Print the error in the console.
     */
    @SuppressLint("LogNotTimber")
    public void printConsoleError(int resId){
        printConsoleError(App.getStringFromRes(resId));
    }

    /**
     * Print a message in the console.
     */
    @SuppressLint("LogNotTimber")
    public void printConsoleMessage(int resId){
        printConsoleMessage(App.getStringFromRes(resId));
    }

    /**
     * Show the error message and the exception visually
     * and in the console.
     */
    @SuppressLint("LogNotTimber")
    public void showVisualError(Exception e, int resId){
        showVisualError(e, App.getStringFromRes(resId));
    }

    /**
     * Show a message and the exception visually
     * and in the console.
     */
    @SuppressLint("LogNotTimber")
    public void showVisualMessage(int resId){
        showVisualMessage(App.getStringFromRes(resId));
    }

}