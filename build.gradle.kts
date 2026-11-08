buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // Overrides the older Kotlin that AGP's built-in Kotlin bundles.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    id("com.android.application") version "9.5.1" apply false
}
