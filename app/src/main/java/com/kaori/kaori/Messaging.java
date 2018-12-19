package com.kaori.kaori;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

public class Messaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        LogManager.getInstance().printConsoleMessage("Refreshed token: " + token);

        FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_USERS)
                .document(DataManager.getInstance().getUser().getUid())
                .update(Constants.FIELD_TOKEN, token);
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notifyId = 1;

        Intent intent = new Intent(this, KaoriChat.class);
        intent.putExtra("notification", 1);
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification mNotification = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "id";
            String name = "a";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            mChannel.setDescription("description");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[] {100, 300, 200, 300});
            mNotificationManager.createNotificationChannel(mChannel);

            mNotification = new Notification.Builder(getApplicationContext(), id)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                    .setContentIntent(mPendingIntent)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setSmallIcon(R.drawable.logo)
                    .build();
        } else {
            mNotification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setContentIntent(mPendingIntent)
                    .setSmallIcon(R.drawable.logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                    .setLights(Color.WHITE, Color.RED, Color.GREEN)
                    .setVibrate(new long[] {100, 300, 200, 300})
                    .build();
        }
        mNotificationManager.notify(notifyId, mNotification);
    }

}
