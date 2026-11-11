package org.towerporchfest.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.towerporchfest.shared.data.ScheduleFilter
import org.towerporchfest.shared.data.SyncStatus
import org.towerporchfest.shared.model.Amenity

class SyncEngineTest {
    @Test
    fun firstSyncStoresTheSnapshot() = runTest {
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler))

        assertTrue(graph.syncEngine.sync())

        assertEquals(listOf<String?>(null), graph.server.sentEtags)
        assertEquals(3, graph.repository.schedule(ScheduleFilter()).first().size)
        assertEquals(1_000L, graph.repository.lastSyncedAt().first())
        assertIs<SyncStatus.Idle>(graph.syncEngine.status.value)

        val porch = graph.repository.porch("porch-blue").first()!!
        assertEquals("#7 Blue Porch", porch.label)
        assertEquals("Tower Yoga", porch.sponsorName)
        assertEquals(listOf(Amenity.PARKING, Amenity.RESTROOM), porch.amenities)
    }

    @Test
    fun sponsorsComeFromTheServerListIncludingThoseWithoutAPorch() = runTest {
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler))
        graph.syncEngine.sync()

        val sponsors = graph.repository.sponsors().first()

        // Ordered by level, then name.
        assertEquals(listOf("Valley Radio", "Tower Yoga", "alpha Print"), sponsors.map { it.name })
        assertEquals(listOf(0, 2, 2), sponsors.map { it.level })
        assertNull(sponsors[0].porchId)
        assertEquals("porch-blue", sponsors[1].porchId)
        assertEquals("", sponsors[1].website)
        assertNull(sponsors[2].logoUrl)
    }

    @Test
    fun withoutAServerListSponsorsAreDerivedFromPorches() = runTest {
        // No "sponsors" key at all, like a backend that predates the list.
        val server = FakeServer(body = Fixtures.SNAPSHOT_WITHOUT_SPONSOR_LIST)
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler), server)
        graph.syncEngine.sync()

        val sponsors = graph.repository.sponsors().first()

        assertEquals(listOf("Tower Yoga"), sponsors.map { it.name })
        assertEquals("porch-blue", sponsors.single().porchId)
        assertEquals(2, sponsors.single().level)
    }

    @Test
    fun secondSyncSendsTheEtagAndKeepsDataOnNotModified() = runTest {
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler))
        graph.syncEngine.sync()

        graph.now = 2_000L
        assertTrue(graph.syncEngine.sync())

        assertEquals(listOf(null, "\"v1\""), graph.server.sentEtags)
        assertEquals(3, graph.repository.schedule(ScheduleFilter()).first().size)
        assertEquals(2_000L, graph.repository.lastSyncedAt().first())
    }

    @Test
    fun newSnapshotReplacesEverythingIncludingDeletedRows() = runTest {
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler))
        graph.syncEngine.sync()

        graph.server.body = Fixtures.SNAPSHOT_V2
        graph.server.etag = "\"v2\""
        assertTrue(graph.syncEngine.sync())

        val schedule = graph.repository.schedule(ScheduleFilter()).first()
        assertEquals(listOf("perf-1"), schedule.map { it.performanceId })
        assertEquals("3:00 PM – 3:45 PM", schedule.single().timeRange)
        assertEquals(listOf("Rock"), graph.repository.genres().first().map { it.name })
        assertNull(graph.repository.porch("porch-plain").first())
        assertEquals(1, graph.repository.mapPins().first().size)
    }

    @Test
    fun failedSyncReportsTheErrorAndKeepsOfflineData() = runTest {
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler))
        graph.syncEngine.sync()

        graph.server.failing = true
        assertFalse(graph.syncEngine.sync())

        assertIs<SyncStatus.Failed>(graph.syncEngine.status.value)
        assertEquals(3, graph.repository.schedule(ScheduleFilter()).first().size)

        graph.server.failing = false
        assertTrue(graph.syncEngine.sync())
        assertIs<SyncStatus.Idle>(graph.syncEngine.status.value)
    }

    @Test
    fun snapshotFromANewerSchemaIsRejectedWithoutTouchingData() = runTest {
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler))
        graph.syncEngine.sync()

        graph.server.body = Fixtures.FUTURE_SCHEMA
        graph.server.etag = "\"v99\""
        assertFalse(graph.syncEngine.sync())

        val status = assertIs<SyncStatus.Failed>(graph.syncEngine.status.value)
        assertTrue("update" in status.message)
        assertEquals(3, graph.repository.schedule(ScheduleFilter()).first().size)
    }
}
