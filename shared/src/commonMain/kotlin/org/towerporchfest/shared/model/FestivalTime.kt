package org.towerporchfest.shared.model

private const val MINUTES_PER_DAY = 24 * 60

/** Parses the backend's `HH:MM[:SS]` into minutes since midnight. */
internal fun parseMinuteOfDay(time: String): Int {
    val parts = time.split(':')
    require(parts.size >= 2) { "Unrecognised time: $time" }
    return parts[0].toInt() * 60 + parts[1].toInt()
}

/**
 * The backend stores times without a date, so a set that ends after midnight arrives with
 * an end earlier than its start. Push that end into the next day so durations stay positive.
 */
internal fun normalizeEndMinute(startMinute: Int, endMinute: Int): Int =
    if (endMinute < startMinute) endMinute + MINUTES_PER_DAY else endMinute

/** `13:05` as `1:05 PM`. Minutes past midnight wrap, so 1470 is `12:30 AM`. */
fun formatMinuteOfDay(minute: Int): String {
    val wrapped = ((minute % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY
    val hour24 = wrapped / 60
    val hour12 = when (val h = hour24 % 12) { 0 -> 12; else -> h }
    val suffix = if (hour24 < 12) "AM" else "PM"
    return "$hour12:${(wrapped % 60).toString().padStart(2, '0')} $suffix"
}

fun formatTimeRange(startMinute: Int, endMinute: Int): String =
    "${formatMinuteOfDay(startMinute)} – ${formatMinuteOfDay(endMinute)}"
