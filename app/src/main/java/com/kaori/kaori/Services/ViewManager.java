package com.kaori.kaori.Services;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ViewManager {

    private View waitView;
    private List<View> emptyViews;

    public void setWaitView(View waitView) {
        this.waitView = waitView;
    }

    public void setEmptyView(View emptyView) {
        this.emptyViews = new ArrayList<>();
        this.emptyViews.add(emptyView);
    }

    public void addEmptyView(View emptyView) {
        if(!emptyViews.contains(emptyView))
            this.emptyViews.add(emptyView);
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
        if(emptyViews != null && emptyViews.size() > 0)
            for(View emptyView : emptyViews)
                emptyView.setVisibility(emptyViewStatus);
    }

}
