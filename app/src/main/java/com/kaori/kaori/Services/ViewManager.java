package com.kaori.kaori.Services;

import android.view.View;

public class ViewManager {

    private View waitView;
    private View emptyView;

    public void setViews(View waitView, View emptyView) {
        this.waitView = waitView;
        this.emptyView = emptyView;
    }

    public void setAuxiliarViewsStatus(int status) {
        if(status < 0) {
            waitView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else if(status == 0) {
            waitView.setVisibility(View.GONE);
            waitView.setVisibility(View.VISIBLE);
        } else {
            waitView.setVisibility(View.GONE);
            waitView.setVisibility(View.GONE);
        }
    }

}
