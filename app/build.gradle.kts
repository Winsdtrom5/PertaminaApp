plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") version "4.4.0" apply false
}

android {
    packagingOptions {
        resources.excludes.add("META-INF/*")
    }
    namespace = "com.example.pertaminaapp"
    compileSdk = 34
    viewBinding.isEnabled = true
    defaultConfig {
        applicationId = "com.example.pertaminaapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-drive:17.0.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0-rc01")
    implementation("com.itextpdf:itext7-core:7.1.13")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.github.librepdf:openpdf:1.3.29")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("io.github.chaosleung:pinview:1.4.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("mysql:mysql-connector-java:5.1.49")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("de.jollyday:jollyday:0.5.2")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.firebase:firebase-common-ktx:20.4.2")
//    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.9.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.wdullaer:materialdatetimepicker:4.2.3")
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2")
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.squareup.okio:okio:1.17.5")
//    implementation("com.google.api-client:google-api-client-android:1.31.5")
//    implementation("com.google.http-client:google-http-client-gson:1.41.1")
//    implementation("com.google.oauth-client:google-oauth-client-jetty:1.31.1")
//    implementation("com.google.gms:google-services:4.4.0")
//    implementation("com.google.auth:google-auth-library-oauth2-http:0.30.0")
//    implementation("com.google.http-client:google-http-client-jackson2:1.41.1")
//    implementation("com.google.api-client:google-api-client:1.31.5")
//    implementation("com.google.apis:google-api-services-drive:v3-rev305-1.31.0")
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-storage")
    implementation("com.firebaseui:firebase-ui-storage:8.0.2")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.24")
//    implementation ("com.microsoft.identity.client:msal:2.1.0")
//    implementation ("com.microsoft.azure:adal4j:1.6.5")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.android.gms:play-services-drive:17.0.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.api-client:google-api-client-gson:1.32.1")
    implementation ("com.google.http-client:google-http-client-gson:1.41.1")
//    implementation ("com.nimbusds:nimbus-jose-jwt:10.8")
//    implementation ("com.nimbusds:nimbus-jwk:10.8")
}