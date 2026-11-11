package org.towerporchfest.shared.model

data class Sponsor(
    val id: String,
    val name: String,
    /** Sponsorship tier as the backend numbers it: 0 is the highest. See [sponsorLevelTitle]. */
    val level: Int,
    /** Empty when the sponsor has no website. */
    val website: String,
    val logoUrl: String?,
    /** The sponsor's porch on the map, if it has one. */
    val porchId: String?,
)

/** Sponsors of one tier, in display order. */
data class SponsorGroup(
    val level: Int,
    val title: String,
    val sponsors: List<Sponsor>,
)

/** Tier headings, matching the website's home page. Levels the app doesn't know get a plain heading. */
fun sponsorLevelTitle(level: Int): String = when (level) {
    0 -> "Event Sponsor"
    1 -> "Diamond Sponsors"
    2 -> "Platinum Sponsors"
    3 -> "Gold Sponsors"
    4 -> "Community Sponsors"
    else -> "Sponsors"
}

internal fun List<Sponsor>.toGroups(): List<SponsorGroup> =
    groupBy { it.level }
        .map { (level, sponsors) -> SponsorGroup(level, sponsorLevelTitle(level), sponsors.sortedBy { it.name.lowercase() }) }
        .sortedBy { it.level }
