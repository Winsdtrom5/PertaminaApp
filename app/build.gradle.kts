plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
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
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.24")
    implementation ("androidx.databinding:databinding-runtime:8.1.1")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.1")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("androidx.lifecycle:lifecycle-common-java8:2.7.0-alpha01")
    implementation ("com.itextpdf:itext7-core:7.1.13")
    implementation ("com.google.mlkit:barcode-scanning:17.2.0")
    implementation ("com.github.librepdf:openpdf:1.3.29")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.sun.mail:android-mail:1.6.0")
    implementation ("com.sun.mail:android-activation:1.6.0")
    implementation ("io.github.chaosleung:pinview:1.4.4")
    androidTestImplementation ("androidx.test:runner:1.5.2")
    androidTestImplementation ("androidx.test:rules:1.5.0")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("mysql:mysql-connector-java:5.1.49")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation ("com.google.android.gms:play-services-drive:17.0.0")
}