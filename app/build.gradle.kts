import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Firebase Plugin 추가
}

var properties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
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
        buildConfigField("String", "KAKAO_REST_API_KEY", "\"${properties["KAKAO_REST_API_KEY"]}\"")
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"${properties["KAKAO_NATIVE_APP_KEY"]}\"")
        buildConfigField("String", "OPENROUTER_API_KEY", "\"${properties["OPENROUTER_API_KEY"]}\"")
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {

        debug {
            isMinifyEnabled = false
            manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = properties["KAKAO_NATIVE_APP_KEY"] as String
        }
        release {
            isMinifyEnabled = false
            manifestPlaceholders["MANIFEST_KAKAO_NATIVE_APP_KEY"] = properties["MANIFEST_KAKAO_NATIVE_APP_KEY"] as String
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
    implementation ("com.kakao.sdk:v2-user:2.18.0")
    implementation ("com.kakao.sdk:v2-common:2.18.0")
    // 2. 카카오 길찾기 API 통신을 위한 Retrofit (HTTP 클라이언트)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")

    // 3. Retrofit에서 JSON 응답을 Java 객체로 변환하기 위한 Gson 컨버터
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // 4. 사용자의 현재 위치를 가져오기 위한 Google Play Services Location 라이브러리
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    // ✅ Unit Test 용 JUnit
    testImplementation("junit:junit:4.13.2")

    // ✅ Instrumentation Test 용 (AndroidJUnit4 포함)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.10.0")


    // ✅ Firebase BoM으로 의존성 통일
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database")


    // Glide (이미지 로딩 라이브러리)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}