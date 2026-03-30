plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.restantsearch"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.restantsearch"
        minSdk = 24
        targetSdk = 36
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    dependencies {
        // --- Glide (画像読み込み用) ---
        implementation("com.github.bumptech.glide:glide:4.16.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

        // --- 通信関連 (OkHttp) ---
        implementation("com.squareup.okhttp3:okhttp:4.12.0")

        // --- 位置情報 ---
        // --- 位置情報 ---
        implementation("com.google.android.gms:play-services-location:21.0.1")
    }
}