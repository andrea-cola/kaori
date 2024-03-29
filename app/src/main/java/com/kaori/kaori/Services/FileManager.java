package com.kaori.kaori.Services;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import com.kaori.kaori.App;
import com.kaori.kaori.BuildConfig;
import com.kaori.kaori.Constants;
import com.kaori.kaori.R;

import java.io.File;

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
     * Constructor
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
    public boolean download() {
        boolean flag = false;
        int notifyId = 0;
        createNotificationChannel();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, "mychannel")
                .setContentTitle(App.getStringFromRes(R.string.notification_title))
                .setContentText(App.getStringFromRes(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setProgress(0, 0, true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS);
        notificationManager.notify(notifyId, builder.build());

        DownloadManager.Request request = new DownloadManager.Request(downloadUrl);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(title);
        request.allowScanningByMediaScanner();
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".pdf");

        if (downloadManager != null && activity != null)
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadManager.enqueue(request);
                File pdfFile = new File(Constants.INTERNAL_STORAGE_PATH + title + ".pdf");
                Uri path = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
                Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pdfIntent.setDataAndType(path, "application/pdf");
                PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, pdfIntent, 0);
                builder.setContentText(App.getStringFromRes(R.string.notification_text_ok))
                        .setContentIntent(pendingIntent);
                flag = true;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                builder.setContentText(App.getStringFromRes(R.string.notification_text_no));
            }
        builder.setProgress(0, 0, false)
                .setAutoCancel(true);
        notificationManager.notify(notifyId, builder.build());
        return flag;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = App.getStringFromRes(R.string.channel_name);
            String description = App.getStringFromRes(R.string.channel_description);
            NotificationChannel channel = new NotificationChannel("mychannel", name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            NotificationManager manager = activity.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
