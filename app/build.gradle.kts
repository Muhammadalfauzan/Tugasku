plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-kapt")
    id ("kotlin-parcelize")
    id ("androidx.navigation.safeargs")
    id ("com.google.dagger.hilt.android")
    id ("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.ecommerce"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.ecommerce"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
        mlModelBinding = true
    }

    buildTypes {
 /*       release {
            isMinifyEnabled = true
            isShrinkResources =true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }*/

        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



    //Navigation Component
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Dagger - Hilt
    implementation(libs.dagger.hilt.android.v252)
    annotationProcessor(libs.dagger.hilt.compiler.v252)

    implementation(libs.dagger.hilt.android.v252)
    kapt(libs.dagger.hilt.compiler.v252)

    //circle
    implementation (libs.circleimageview)

    //Glide
    implementation (libs.glide)
    kapt (libs.compiler)

    // Room database
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    //Live Data
    implementation (libs.androidx.lifecycle.livedata.ktx)

    //slider
    implementation (libs.imageslideshow)

    //okHTTP
    implementation(libs.logging.interceptor)

    // Firebase

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.crashlytics)

    implementation( libs.androidx.swiperefreshlayout)

    // encrypted
    implementation (libs.androidx.security.crypto)

    //ML Kit
    implementation (libs.image.labeling)

    //Lottie
    implementation (libs.lottie)

    //Shimer Efek
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
}
    kapt {
        correctErrorTypes =  true
    }
