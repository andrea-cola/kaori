package com.kaori.kaori.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * This class is create for managing the download request, with the specific intents
 */
public class FileManager {

    /**
     * Download variables
     */
    private String title;
    private Uri downloadUrl;
    private Activity activity;
    private DownloadManager downloadManager;

    /**
     * Contructor
     */
    public FileManager(String title, Uri downloadUrl, Activity activity) {
        this.title = title;
        this.downloadUrl = downloadUrl;
        this.activity = activity;
        this.downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    /**
     * This method creates and manage the download manager
     */
    public boolean download(){
        boolean flag = false;
        ProgressDialog progressDialog = ProgressDialog.show(activity, "", "Downloading...", true);

        DownloadManager.Request request = new DownloadManager.Request(downloadUrl);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(title);
        request.allowScanningByMediaScanner();
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".pdf");

        if (downloadManager != null && activity != null)
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadManager.enqueue(request);
                flag = true;
            } else
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);

        progressDialog.dismiss();
        return flag;
    }
}
