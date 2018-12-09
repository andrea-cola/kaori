package com.kaori.kaori.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.File;

import retrofit2.http.Url;

/**
 * This class is create for managing the download request, with the specific intents
 */
public class FileManager {
    /**
     * Constants
     */
    private String pathname = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator;

    /**
     * Download variables
     */
    private String title;
    private Uri downloadUrl;
    private Activity activity;
    private Context context;

    /**
     * Contructor
     */
    public FileManager(String title, Uri downloadUrl, Activity activity, Context context) {
        this.title = title;
        this.downloadUrl = downloadUrl;
        this.activity = activity;
        this.context = context;
    }

    /**
     * This method creates and manage the download manager
     */
    public boolean download(){
        ProgressDialog progressDialog = ProgressDialog.show(activity, "", "Downloading...", true);

        // create download request
        DownloadManager.Request request = new DownloadManager.Request(downloadUrl);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(title);
        request.allowScanningByMediaScanner();
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".pdf");

        DownloadManager manager = (DownloadManager)activity.getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null && activity != null) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                manager.enqueue(request);
                progressDialog.dismiss();
                return true;
            } else {
                Toast.makeText(context, "Non hai i permessi per scaricare il file", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            }
        }
        progressDialog.dismiss();
        return false;
    }
}
