package com.team5.taketac;

import android.app.Application;
import com.kakao.vectormap.KakaoMapSdk;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // ⭐ 네이티브 앱 키 사용해야 합니다!
        KakaoMapSdk.init(this, "45ce2cc4d6dd8ea14d9b1e9c0b6dff8a");
    }
}
