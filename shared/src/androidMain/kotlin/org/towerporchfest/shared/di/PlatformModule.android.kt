package org.towerporchfest.shared.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module
import org.towerporchfest.shared.data.local.PorchFestDatabase

// Needs an Android Context in the graph: the app passes androidContext(...) to initKoin.
internal actual val platformModule: Module = module {
    single<SqlDriver> { AndroidSqliteDriver(PorchFestDatabase.Schema, get<Context>(), DATABASE_NAME) }
    single<HttpClientEngine> { OkHttp.create() }
}

private const val DATABASE_NAME = "porchfest.db"
