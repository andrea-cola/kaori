package com.kaori.kaori.Services;

import android.view.View;

public class ViewManager {

    private View waitView;
    private View emptyView;

    public void setWaitView(View waitView) {
        this.waitView = waitView;
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }

    public void setAuxiliarViewsStatus(int status) {
        int waitViewStatus = View.GONE;
        int emptyViewStatus = View.GONE;

        if(status < 0)
            waitViewStatus = View.VISIBLE;
        else if(status == 0)
            emptyViewStatus = View.VISIBLE;

        if(waitView != null)
            waitView.setVisibility(waitViewStatus);
        if(emptyView != null)
            emptyView.setVisibility(emptyViewStatus);
    }

}
