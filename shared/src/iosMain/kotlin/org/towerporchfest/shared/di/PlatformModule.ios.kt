package org.towerporchfest.shared.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.towerporchfest.shared.data.local.PorchFestDatabase
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData

internal actual val platformModule: Module = module {
    single<SqlDriver> { NativeSqliteDriver(PorchFestDatabase.Schema, DATABASE_NAME) }
    single<HttpClientEngine> {
        Darwin.create {
            configureSession {
                // The database is the cache and SyncEngine sends If-None-Match itself. Turn off
                // URLSession's own cache so it can't answer a revalidation on our behalf.
                requestCachePolicy = NSURLRequestReloadIgnoringLocalCacheData
                URLCache = null
            }
        }
    }
}

private const val DATABASE_NAME = "porchfest.db"
