pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "porch-fest-android"
include(":app")

// The shared Kotlin Multiplatform module is its own Gradle build. Including it here makes
// Gradle build "org.towerporchfest.mobile:shared" from source instead of fetching it.
// If the builds are split into separate repos, delete this line and publish the artifact.
includeBuild("../shared")
