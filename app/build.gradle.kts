import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// OWASP MASVS: Externalize signing credentials from local.properties or environment variables
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun getSigningProperty(propKey: String, envKey: String, fallback: String): String {
    return System.getenv(envKey)
        ?: localProperties.getProperty(propKey)
        ?: localProperties.getProperty(envKey)
        ?: fallback
}

android {
    namespace = "com.hughdevelopment.player"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hughdevelopment.player"
        minSdk = 26
        targetSdk = 35
        versionCode = 15
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("hughSigning") {
            val signingStoreFilePath = getSigningProperty("signing.storeFile", "SIGNING_STORE_FILE", "keystore/hugh_vault.keystore")
            storeFile = file(signingStoreFilePath)
            storePassword = getSigningProperty("signing.storePassword", "SIGNING_STORE_PASSWORD", "hughdevelopment")
            keyAlias = getSigningProperty("signing.keyAlias", "SIGNING_KEY_ALIAS", "hughvaultkey")
            keyPassword = getSigningProperty("signing.keyPassword", "SIGNING_KEY_PASSWORD", "hughdevelopment")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("hughSigning")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("hughSigning")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Media3 Audio Engine
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    // Room Database (Local Stats & Offline Wrapped)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coil Image Loading (High-performance caching)
    implementation(libs.coil.compose)

    // Networking & Streaming Engine
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // YouTube Extractor Core
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.24.4")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
