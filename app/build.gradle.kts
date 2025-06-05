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

    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation(libs.firebase.database)

    // Firebase Storage 추가
    implementation("com.google.firebase:firebase-storage:20.3.0")

    // Glide (이미지 로딩 라이브러리)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.firestore)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
