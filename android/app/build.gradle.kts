import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

// Backend for the `local` flavor. The default reaches a server on this machine from the
// emulator. To test on a physical device, set porchfest.baseUrl=http://<your-mac-ip>:8000
// in android/local.properties (not committed) or pass -Pporchfest.baseUrl=...
val localBaseUrl: String = run {
    val localProperties = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use(::load)
    }
    localProperties.getProperty("porchfest.baseUrl")
        ?: providers.gradleProperty("porchfest.baseUrl").orNull
        ?: "http://10.0.2.2:8000"
}

android {
    namespace = "org.towerporchfest.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "org.towerporchfest.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("local") {
            dimension = "environment"
            applicationIdSuffix = ".local"
            versionNameSuffix = "-local"
            buildConfigField("String", "BASE_URL", "\"$localBaseUrl\"")
            buildConfigField("String", "ENVIRONMENT", "\"local\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://towerporchfest.org\"")
            buildConfigField("String", "ENVIRONMENT", "\"prod\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.porchfest.shared)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Used only inside map/maplibre; the rest of the app talks to map/FestivalMap.kt.
    implementation(libs.maplibre.android)

    testImplementation(libs.junit)
}
