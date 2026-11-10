package org.towerporchfest.shared

import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.engine.HttpClientEngine
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.towerporchfest.shared.data.EventRepository
import org.towerporchfest.shared.data.SyncEngine
import org.towerporchfest.shared.di.commonModules
import org.towerporchfest.shared.presentation.MapViewModel
import org.towerporchfest.shared.presentation.PorchDetailViewModel
import org.towerporchfest.shared.presentation.ScheduleViewModel

/**
 * Koin resolves the graph at runtime, so a missing or mistyped definition would otherwise only
 * show up as a crash when a screen opens. This resolves every entry point the apps ask for.
 */
class KoinGraphTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun everyEntryPointResolves() {
        val config = AppConfig("http://test", "test", AppConfig.DEFAULT_MAP_STYLE_URL)
        val fakePlatform = module {
            single<SqlDriver> { createTestDriver() }
            single<HttpClientEngine> { FakeServer().engine }
        }
        val app = koinApplication { modules(commonModules(config) + fakePlatform) }
        try {
            val koin = app.koin
            assertEquals(config, koin.get<AppConfig>())
            assertNotNull(koin.get<EventRepository>())
            assertNotNull(koin.get<SyncEngine>())
            assertNotNull(koin.get<ScheduleViewModel>())
            assertNotNull(koin.get<MapViewModel>())
            assertNotNull(koin.get<PorchDetailViewModel> { parametersOf("porch-blue") })
        } finally {
            app.close()
        }
    }
}
