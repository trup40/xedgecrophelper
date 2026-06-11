plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.trup40.xedgecrop"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.trup40.xedgecrop"
        minSdk = 34
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = true
            isShrinkResources = true
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
    // Sadece ekran çizimi ve temel aktivite için gereken en saf sınıfları tuttuk
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)
}