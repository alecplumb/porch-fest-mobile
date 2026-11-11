package org.towerporchfest.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.towerporchfest.shared.AppConfig
import org.towerporchfest.shared.di.initKoin

class PorchFestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // BASE_URL and ENVIRONMENT come from the build type (see app/build.gradle.kts).
        val config = AppConfig(
            baseUrl = BuildConfig.BASE_URL,
            environment = BuildConfig.ENVIRONMENT,
            mapStyleUrl = AppConfig.DEFAULT_MAP_STYLE_URL,
        )
        initKoin(config) { androidContext(this@PorchFestApp) }
    }
}
