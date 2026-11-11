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

// The database is a cache of the server. When its schema changes, change this name: the app
// starts with an empty database and the next sync refills it. (v2: sponsors table.)
private const val DATABASE_NAME = "porchfest-v2.db"
