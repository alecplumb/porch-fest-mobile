package org.towerporchfest.shared

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.towerporchfest.shared.data.SyncStatus
import org.towerporchfest.shared.presentation.MapViewModel
import org.towerporchfest.shared.presentation.ScheduleUiState
import org.towerporchfest.shared.presentation.ScheduleViewModel

/** Skips intermediate states until one satisfies [predicate]; fails on Turbine's timeout. */
private suspend fun <T> ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}

class ScheduleViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun startsEmptyThenShowsSectionsAfterRefresh() = runTest(dispatcher) {
        val graph = TestGraph(dispatcher)
        val viewModel = ScheduleViewModel(graph.repository, graph.syncEngine)

        viewModel.state.test {
            assertEquals(ScheduleUiState(), awaitItem())

            viewModel.refresh()
            val loaded = awaitUntil { it.sections.isNotEmpty() && it.genres.isNotEmpty() && !it.isRefreshing }

            assertEquals(listOf("1:00 PM", "11:30 PM"), loaded.sections.map { it.title })
            assertEquals(listOf(2, 1), loaded.sections.map { it.items.size })
            assertEquals(listOf("Folk", "Rock"), loaded.genres.map { it.name })
            assertEquals(1_000L, loaded.lastSyncedAtEpochMillis)
            assertFalse(loaded.hasNoData)
            assertFalse(loaded.isRefreshing)
        }
    }

    @Test
    fun genreAndSearchFiltersNarrowTheSchedule() = runTest(dispatcher) {
        val graph = TestGraph(dispatcher).also { it.syncEngine.sync() }
        val viewModel = ScheduleViewModel(graph.repository, graph.syncEngine)

        viewModel.state.test {
            val folk = awaitUntil { it.genres.isNotEmpty() }.genres.first { it.name == "Folk" }

            viewModel.selectGenre(folk)
            var state = awaitUntil { it.selectedGenre == folk && it.sections.sumOf { s -> s.items.size } == 1 }
            assertEquals(listOf("perf-1"), state.sections.flatMap { it.items }.map { it.performanceId })
            assertEquals(folk, state.selectedGenre)
            assertTrue(state.isFiltered)

            viewModel.selectGenre(null)
            viewModel.setSearch("wishon")
            state = awaitUntil { it.search == "wishon" && it.selectedGenre == null && it.sections.sumOf { s -> s.items.size } == 2 }
            assertEquals(listOf("perf-2", "perf-3"), state.sections.flatMap { it.items }.map { it.performanceId })
            assertEquals("wishon", state.search)
        }
    }

    @Test
    fun offlineWithNothingSyncedReportsNoDataAndTheFailure() = runTest(dispatcher) {
        val graph = TestGraph(dispatcher).also { it.server.failing = true }
        val viewModel = ScheduleViewModel(graph.repository, graph.syncEngine)

        viewModel.state.test {
            viewModel.refresh()
            val state = awaitUntil { it.sync is SyncStatus.Failed }
            assertTrue(state.hasNoData)
        }
    }

    @Test
    fun mapSelectionIsDroppedWhenASyncRemovesThePorch() = runTest(dispatcher) {
        val graph = TestGraph(dispatcher).also { it.syncEngine.sync() }
        val viewModel = MapViewModel(graph.repository, graph.syncEngine)

        viewModel.state.test {
            viewModel.selectPorch("porch-plain")
            awaitUntil { it.selectedPorchId == "porch-plain" }

            graph.server.body = Fixtures.SNAPSHOT_V2
            graph.server.etag = "\"v2\""
            graph.syncEngine.sync()

            val state = awaitUntil { it.pins.size == 1 }
            assertEquals(listOf("porch-blue"), state.pins.map { it.porchId })
            assertEquals(null, state.selectedPorchId)
        }
    }
}
