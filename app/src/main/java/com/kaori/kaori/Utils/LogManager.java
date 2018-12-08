package com.kaori.kaori.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * This class is responsible of throwing errors to the user.
 */
public class LogManager {

    private static LogManager logManager;

    private LogManager(){
        super();
    }

    public static LogManager getInstance(){
        if(logManager == null)
            logManager = new LogManager();
        return logManager;
    }

    @SuppressLint("LogNotTimber")
    public void printConsoleError(String message){
        Log.e(Constants.TAG, message);
    }

    @SuppressLint("LogNotTimber")
    public void printConsoleMessage(String message){
        Log.d(Constants.TAG, message);
    }

    @SuppressLint("LogNotTimber")
    public void showVisualError(Context context, Exception e, String message){
        if(e == null)
            Log.e(Constants.TAG, message);
        else
            Log.e(Constants.TAG, message + " -> " + e.getMessage());
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("LogNotTimber")
    public void showVisualMessage(Context context, String message){
        Log.d(Constants.TAG, message);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}


