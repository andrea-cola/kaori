package com.kaori.kaori;

import android.app.Application;
import android.content.Context;
import android.view.View;

import com.kaori.kaori.Services.ViewManager;

public class App extends Application {

    private static App app;
    private ViewManager viewManager;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        viewManager = new ViewManager();
    }

    public static App getInstance() {
        return app;
    }

    public static Context getActiveContext() {
        return app.getApplicationContext();
    }

    public static String getStringFromRes(int resId) {
        return app.getApplicationContext().getString(resId);
    }

    public static void setWaitView(View wait){
        app.viewManager.setWaitView(wait);
    }

    public static void setEmptyView(View empty){
        app.viewManager.setEmptyView(empty);
    }

    public static void addEmptyView(View empty) {
        app.viewManager.addEmptyView(empty);
    }

    public static void setAuxiliarViewsStatus(int status) {
        app.viewManager.setAuxiliarViewsStatus(status);
    }

}
