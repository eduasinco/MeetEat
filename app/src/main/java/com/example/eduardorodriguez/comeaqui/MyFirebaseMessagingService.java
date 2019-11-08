package com.example.eduardorodriguez.comeaqui;

import android.app.Notification;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.example.eduardorodriguez.comeaqui.App.*;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManagerCompat notificationManager;


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN",s);
        // sendRegistrationToServer(token);
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        notificationManager = NotificationManagerCompat.from(this);
        String channel = remoteMessage.getData().get("channel");

        switch (channel){
            case MESSAGES_CHANNEL_ID:
                Notification messageNotification = new NotificationCompat.Builder(this, MESSAGES_CHANNEL_ID)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .build();
                notificationManager.notify(0, messageNotification);
                break;

            case ORDERS_CHANNEL_ID:
                Notification orderNotification = new NotificationCompat.Builder(this, ORDERS_CHANNEL_ID)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_EVENT)
                        .build();
                notificationManager.notify(1, orderNotification);
                break;

            case NOTIFICATIONS_CHANNEL_ID:
                Notification notificationNotification = new NotificationCompat.Builder(this, NOTIFICATIONS_CHANNEL_ID)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_EVENT)
                        .build();
                notificationManager.notify(2, notificationNotification);
                break;
        }

        System.out.println("HOLAAAAAAAA: " + remoteMessage.getData().get("message"));
        super.onMessageReceived(remoteMessage);
    }
}