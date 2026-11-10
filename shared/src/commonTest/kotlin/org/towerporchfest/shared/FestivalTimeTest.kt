package org.towerporchfest.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import org.towerporchfest.shared.model.formatMinuteOfDay
import org.towerporchfest.shared.model.formatTimeRange
import org.towerporchfest.shared.model.normalizeEndMinute
import org.towerporchfest.shared.model.parseMinuteOfDay

class FestivalTimeTest {
    @Test
    fun parsesBackendTimes() {
        assertEquals(0, parseMinuteOfDay("00:00:00"))
        assertEquals(13 * 60 + 5, parseMinuteOfDay("13:05:00"))
        assertEquals(10 * 60, parseMinuteOfDay("10:00"))
    }

    @Test
    fun formatsTwelveHourClock() {
        assertEquals("12:00 AM", formatMinuteOfDay(0))
        assertEquals("10:00 AM", formatMinuteOfDay(600))
        assertEquals("12:00 PM", formatMinuteOfDay(720))
        assertEquals("1:05 PM", formatMinuteOfDay(785))
        assertEquals("11:59 PM", formatMinuteOfDay(1439))
    }

    @Test
    fun setsThatCrossMidnightEndTheNextDay() {
        val start = parseMinuteOfDay("23:30:00")
        val end = normalizeEndMinute(start, parseMinuteOfDay("00:30:00"))
        assertEquals(1470, end)
        assertEquals("11:30 PM – 12:30 AM", formatTimeRange(start, end))
    }
}
