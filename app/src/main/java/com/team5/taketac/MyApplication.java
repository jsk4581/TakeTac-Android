package com.team5.taketac;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.kakao.vectormap.KakaoMapSdk;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        var appkey = BuildConfig.KAKAO_NATIVE_APP_KEY;

        // ⭐ 네이티브 앱 키 사용해야 합니다!
        KakaoMapSdk.init(this, appkey);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel classAlarmChannel = new NotificationChannel(
                    "class_alarm_channel",
                    "수업 알림",
                    NotificationManager.IMPORTANCE_HIGH
            );
            classAlarmChannel.setDescription("수업 늦을 거 같으면 take-tac");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(classAlarmChannel);
            }
        }
    }
}

