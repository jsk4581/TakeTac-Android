package com.team5.taketac;

import android.app.Application;
import com.kakao.vectormap.KakaoMapSdk;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        var appkey = BuildConfig.KAKAO_NATIVE_APP_KEY;

        // ⭐ 네이티브 앱 키 사용해야 합니다!
        KakaoMapSdk.init(this, appkey);
    }
}
