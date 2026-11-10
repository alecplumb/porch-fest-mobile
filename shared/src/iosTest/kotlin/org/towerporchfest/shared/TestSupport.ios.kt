package org.towerporchfest.shared

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import org.towerporchfest.shared.data.local.PorchFestDatabase

internal actual fun createTestDriver(): SqlDriver = inMemoryDriver(PorchFestDatabase.Schema)
