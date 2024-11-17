plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs")
    id("com.google.devtools.ksp") version "1.9.24-1.0.20"


}

android {
    namespace = "sdm.com.asturexplorers"
    compileSdk = 34

    defaultConfig {
        applicationId = "sdm.com.asturexplorers"
        minSdk = 34
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("com.google.code.gson:gson:2.8.9")
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.coil)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation(libs.firebase.crashlytics)

    // Import the Firebase Auth dependency
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Import the Google Sign-In dependency
    implementation(libs.google.auth)
    implementation(kotlin("script-runtime"))


    //Import OSM dependency
    implementation(libs.osmdroid.android)

}