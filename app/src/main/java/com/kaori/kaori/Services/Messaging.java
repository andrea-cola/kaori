package com.kaori.kaori.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kaori.kaori.App;
import com.kaori.kaori.Chat.KaoriChat;
import com.kaori.kaori.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Messaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FirebaseMex", "Message received" + remoteMessage.getData());
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notifyId = 1;

        Intent intent = new Intent(this, KaoriChat.class);
        intent.putExtra("notification", 1);
        intent.putExtra("name", remoteMessage.getData().get("title"));
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification mNotification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "id";
            String name = "a";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 300, 200, 300});
            mNotificationManager.createNotificationChannel(mChannel);

            mNotification = new Notification.Builder(getApplicationContext(), id)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setContentText(remoteMessage.getData().get("body"))
                    .setLargeIcon(getIcon(remoteMessage.getData().get("icon")))
                    .setContentIntent(mPendingIntent)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setAutoCancel(true)
                    .build();
        } else
            mNotification = createOldNotification(remoteMessage, mPendingIntent, getIcon(remoteMessage.getData().get("icon")));

        mNotificationManager.notify(notifyId, mNotification);
    }

    private Bitmap getIcon(String url){
        if(DataManager.getInstance() != null) {
            if(DataManager.getInstance().getNotificationsIcons().containsKey(url))
                return DataManager.getInstance().getNotificationsIcons().get(url);
            else {
                loadImage(url);
            }
        }
        return BitmapFactory.decodeResource(App.getActiveContext().getResources(), R.drawable.logo_completo_black);
    }

    private void loadImage(String url){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    InputStream in = new URL(url).openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    DataManager.getInstance().getNotificationsIcons().put(url, bitmap);
                } catch (IOException e) {

                }
                return null;
            }
        }.execute();
    }

    private Notification createOldNotification(RemoteMessage remoteMessage, PendingIntent pendingIntent, Bitmap icon){
        return new Notification.Builder(getApplicationContext())
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(icon)
                .setLights(Color.WHITE, Color.RED, Color.GREEN)
                .setVibrate(new long[]{100, 300, 200, 300})
                .build();
    }

}