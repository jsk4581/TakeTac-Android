plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Firebase Plugin 추가
}

android {
    namespace = "com.team5.taketac"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.team5.taketac"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("com.google.firebase:firebase-auth:22.3.1") // Firebase Auth 직접 명시
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation ("com.kakao.maps.open:android:2.12.8")
    implementation ("com.kakao.sdk:v2-user:2.18.0")        // ✅ 로그인 포함 (Utility 있음)
    implementation ("com.kakao.sdk:v2-common:2.18.0")
    // 2. 카카오 길찾기 API 통신을 위한 Retrofit (HTTP 클라이언트)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")

    // 3. Retrofit에서 JSON 응답을 Java 객체로 변환하기 위한 Gson 컨버터
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // 4. 사용자의 현재 위치를 가져오기 위한 Google Play Services Location 라이브러리
    implementation ("com.google.android.gms:play-services-location:21.0.1")
}
