import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0" // Add serialization plugin
}

// Function to load properties from local.properties
fun getApiKey(propertyKey: String): String {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(FileInputStream(localPropertiesFile))
        return properties.getProperty(propertyKey, "") // Return empty string if key not found
    } else {
        // Optionally handle the case where local.properties doesn't exist
        // You might want to throw an error or return a default/empty value
        println("Warning: local.properties file not found. API key will be empty.")
        return ""
    }
}

android {
    namespace = "com.example.quoter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.quoter"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Make the API key available in BuildConfig
        buildConfigField("String", "OPENAI_API_KEY", "\"${getApiKey("openai.api.key")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true // Enable BuildConfig generation
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation(libs.androidx.navigation.compose)
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Ktor for Network Calls
    implementation("io.ktor:ktor-client-core:2.3.11")
    implementation("io.ktor:ktor-client-cio:2.3.11") // CIO engine
    implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
    implementation("io.ktor:ktor-client-logging:2.3.11") // Add logging dependency

    // Kotlinx Serialization for JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Coil for Image Loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Jetpack DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1") // Add this line

    // Pager for fullscreen image viewer
    implementation("androidx.compose.foundation:foundation:1.6.7") // Ensure foundation is up-to-date
    implementation("androidx.compose.foundation:foundation-layout:1.6.7") // Ensure layout is up-to-date

    // OpenAI Java SDK - Update version
    implementation("com.openai:openai-java:1.6.0") // Updated version to 1.6.0

    // Ensure Material Components library is included
    implementation("com.google.android.material:material:1.12.0") // Or a newer stable version
}
