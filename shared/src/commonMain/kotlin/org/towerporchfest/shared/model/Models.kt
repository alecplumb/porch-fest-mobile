package org.towerporchfest.shared.model

data class Genre(val id: Int, val name: String, val slug: String)

/** One performance as shown in the schedule list. */
data class ScheduleItem(
    val performanceId: String,
    val startMinute: Int,
    val endMinute: Int,
    val performerName: String,
    val performerImageUrl: String?,
    /** Comma-separated genre names; empty when the performer has none. */
    val genres: String,
    val porchId: String,
    val porchName: String,
    val porchNumber: Int?,
    val streetAddress: String,
) {
    val timeRange: String get() = formatTimeRange(startMinute, endMinute)
    val porchLabel: String get() = porchLabel(porchName, porchNumber, streetAddress)
}

/** Performances that start at the same time, in schedule order. */
data class ScheduleSection(
    val startMinute: Int,
    val title: String,
    val items: List<ScheduleItem>,
)

data class MapPin(
    val porchId: String,
    val label: String,
    val number: Int?,
    val latitude: Double,
    val longitude: Double,
    val performanceCount: Int,
    val sponsored: Boolean,
)

enum class Amenity(val label: String) {
    VENDOR("Vendors"),
    CHILDRENS_ACTIVITIES("Kids' activities"),
    AFTER_PARTY("After party"),
    PARKING("Parking"),
    INFO_BOOTH("Info booth"),
    RESTROOM("Restroom"),
    DRINKING_WATER("Drinking water"),
    BICYCLE_REPAIR("Bike repair"),
}

data class Porch(
    val id: String,
    val slug: String,
    val name: String,
    val number: Int?,
    /** The host's description of the porch. Not named `description`: that collides with NSObject in Swift. */
    val details: String,
    val streetAddress: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?,
    val sponsorName: String?,
    val sponsorLogoUrl: String?,
    val amenities: List<Amenity>,
) {
    val label: String get() = porchLabel(name, number, streetAddress)
}

/** One performance as shown on a porch's detail screen. */
data class PorchPerformance(
    val performanceId: String,
    val startMinute: Int,
    val endMinute: Int,
    val performerName: String,
    val performerBio: String,
    val performerLink: String,
    val performerImageUrl: String?,
    val genres: String,
) {
    val timeRange: String get() = formatTimeRange(startMinute, endMinute)
}

/** Porch names are optional on the backend; fall back to the address. */
internal fun porchLabel(name: String, number: Int?, streetAddress: String): String {
    val base = name.ifBlank { streetAddress }
    return if (number != null) "#$number $base" else base
}
