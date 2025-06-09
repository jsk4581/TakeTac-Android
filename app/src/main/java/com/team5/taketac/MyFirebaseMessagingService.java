package com.team5.taketac;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.app.PendingIntent;
import android.app.Notification;
import android.content.Intent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "수업 알림";
        String body = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "알림 메시지가 도착했습니다.";

        Intent intent = new Intent(this, MainActivity.class); // 알림 클릭 시 앱 실행
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "class_alarm_channel")
                .setSmallIcon(R.mipmap.ic_launcher) // 앱 아이콘
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1001, builder.build());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("fcmToken", token);
        }
    }
}
