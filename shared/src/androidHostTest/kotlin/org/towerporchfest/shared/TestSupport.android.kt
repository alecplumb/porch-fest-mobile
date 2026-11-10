package org.towerporchfest.shared

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.towerporchfest.shared.data.local.PorchFestDatabase

internal actual fun createTestDriver(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { PorchFestDatabase.Schema.create(it) }
