package org.towerporchfest.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.towerporchfest.shared.data.ScheduleFilter

class EventRepositoryTest {
    @Test
    fun scheduleIsOrderedByStartTimeWithJoinedDetails() = runTest {
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler)).also { it.syncEngine.sync() }

        val schedule = graph.repository.schedule(ScheduleFilter()).first()

        assertEquals(listOf("perf-1", "perf-2", "perf-3"), schedule.map { it.performanceId })
        val first = schedule.first()
        assertEquals("The Olives", first.performerName)
        assertEquals(setOf("Rock", "Folk"), first.genres.split(", ").toSet())
        assertEquals("#7 Blue Porch", first.porchLabel)
        assertEquals("1:00 PM – 1:45 PM", first.timeRange)
        // A porch with no name or number is labelled by its address.
        assertEquals("9 Wishon Ave", schedule[1].porchLabel)
        assertEquals("", schedule[1].genres)
        assertEquals("11:30 PM – 12:30 AM", schedule[2].timeRange)
    }

    @Test
    fun searchMatchesPerformerPorchOrAddressIgnoringCase() = runTest {
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler)).also { it.syncEngine.sync() }
        suspend fun ids(search: String) =
            graph.repository.schedule(ScheduleFilter(search = search)).first().map { it.performanceId }

        assertEquals(listOf("perf-1"), ids("olives"))
        assertEquals(listOf("perf-1"), ids(" blue "))
        assertEquals(listOf("perf-2", "perf-3"), ids("WISHON"))
        assertEquals(emptyList(), ids("zydeco"))
    }

    @Test
    fun genreFilterAndGenreListOnlyCoverGenresInUse() = runTest {
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler)).also { it.syncEngine.sync() }

        val rock = graph.repository.schedule(ScheduleFilter(genreId = 1)).first()
        assertEquals(listOf("perf-1", "perf-3"), rock.map { it.performanceId })

        val folk = graph.repository.schedule(ScheduleFilter(genreId = 2)).first()
        assertEquals(listOf("perf-1"), folk.map { it.performanceId })

        // Polka exists on the server but nobody plays it.
        assertEquals(listOf("Folk", "Rock"), graph.repository.genres().first().map { it.name })
    }

    @Test
    fun mapPinsAndPorchPerformances() = runTest {
        val graph = TestGraph(UnconfinedTestDispatcher(testScheduler)).also { it.syncEngine.sync() }

        val pins = graph.repository.mapPins().first().associateBy { it.porchId }
        assertEquals(2, pins.size)
        assertEquals(1, pins.getValue("porch-blue").performanceCount)
        assertEquals(36.765, pins.getValue("porch-blue").latitude)
        assertEquals(2, pins.getValue("porch-plain").performanceCount)

        val sets = graph.repository.performancesAt("porch-plain").first()
        assertEquals(listOf("Solo Sam", "Night Owls"), sets.map { it.performerName })
    }
}
