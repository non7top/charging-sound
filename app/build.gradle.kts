plugins {
    id("com.android.application")
}

// Bumped by release-please; the version code is derived from it.
val appVersion = "0.0.0" // x-release-please-version
val (major, minor, patch) = appVersion.split(".").map { it.toInt() }

android {
    namespace = "io.github.non7top.chargingsound"
    compileSdk { version = release(37) }
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "io.github.non7top.chargingsound"
        minSdk = 31
        targetSdk = 37
        versionCode = maxOf(1, major * 10000 + minor * 100 + patch)
        versionName = appVersion
    }

    val keystorePath = System.getenv("CHARGING_SOUND_KEYSTORE")
    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("CHARGING_SOUND_STORE_PASSWORD")
                keyAlias = System.getenv("CHARGING_SOUND_KEY_ALIAS")
                keyPassword = System.getenv("CHARGING_SOUND_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
