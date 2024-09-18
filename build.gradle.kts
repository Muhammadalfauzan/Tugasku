// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        // other repositories...
        mavenCentral()
    }

    dependencies {
        val nav_version = "2.8.0"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
        /*  classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0"
        classpath 'com.google.dagger:hilt-android-gradle-plugin:2.48.1'*/
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
}
