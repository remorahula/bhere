
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "io.getstream.kmp.android"
    compileSdk = 36
    defaultConfig {
        applicationId = "io.getstream.kmp.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Add this:
        manifestPlaceholders["appAuthRedirectScheme"] = "io.getstream.kmp.android"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
kotlin {
    jvmToolchain(21)
}


dependencies {
    implementation(project(":shared"))

    implementation("com.google.android.material:material:1.12.0")


    // Compose (from org.jetbrains.compose plugin)
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    implementation(compose.preview)
    debugImplementation(compose.uiTooling)

    // AndroidX host + integration
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // AppAuth (OIDC)
    implementation(libs.appauth)

    // Location
    implementation(libs.gms.play.services.location)

    // CameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)


}
