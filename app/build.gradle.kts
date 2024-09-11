import org.jetbrains.kotlin.gradle.plugin.kotlinToolingVersion

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")

    // id("androidx.room")
}

android {

    namespace = "com.vaultmessenger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vaultmessenger"
        minSdk = 24
        targetSdk = 34
        versionCode = 8
        versionName = "1.9.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true            // Enable code shrinking and obfuscation
            isShrinkResources = true          // Remove unused resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false           // Enable code shrinking and obfuscation
            isShrinkResources = false          // Remove unused resources
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
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.compose.material:material:1.7.0")
    implementation("com.google.accompanist:accompanist-pager:0.24.13-rc")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.35.1-alpha")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.0")
    implementation("com.google.firebase:firebase-crashlytics:19.0.3")
    implementation("com.google.firebase:firebase-perf:21.0.1")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-analytics:22.1.0")
    implementation("com.google.firebase:firebase-firestore:25.1.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.0")
    implementation("androidx.paging:paging-common-android:3.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0-RC")
    implementation("androidx.paging:paging-compose:3.3.2")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.0")
    implementation("androidx.compose.ui:ui-geometry:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.test:core-ktx:1.6.1")
    implementation("androidx.navigation:navigation-testing:2.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.firebase:firebase-messaging:24.0.1")
    implementation("com.google.firebase:firebase-functions:21.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("androidx.annotation:annotation:1.8.2")
    implementation("com.github.dhaval2404:imagepicker:2.1")

    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")

// Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-appcheck-debug")

// Room components
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

// Lifecycle components (keeping only the ktx variant)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$rootProject.lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$rootProject.lifecycleVersion")

// Kotlin components
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinToolingVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$rootProject.coroutines")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$rootProject.coroutines")
}