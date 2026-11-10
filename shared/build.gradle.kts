import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.skie)
}

group = "org.towerporchfest.mobile"
version = "0.1.0"

kotlin {
    android {
        namespace = "org.towerporchfest.shared"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        withHostTest { }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "org.towerporchfest.shared")
            // Swift needs the ViewModel base type to hold and clear shared view models.
            export(libs.androidx.lifecycle.viewmodel)
        }
    }

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kermit)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.turbine)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}

sqldelight {
    databases {
        create("PorchFestDatabase") {
            packageName.set("org.towerporchfest.shared.data.local")
        }
    }
}

skie {
    analytics {
        // Don't send build analytics to Touchlab.
        disableUpload.set(true)
    }
}
