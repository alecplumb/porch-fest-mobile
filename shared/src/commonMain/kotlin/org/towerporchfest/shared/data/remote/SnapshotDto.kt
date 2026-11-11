package org.towerporchfest.shared.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wire format of `GET /api/mobile/v1/snapshot` (porch-fest-django, `mobile/api`). */
@Serializable
internal data class SnapshotDto(
    @SerialName("schema_version") val schemaVersion: Int,
    val genres: List<GenreDto> = emptyList(),
    val performers: List<PerformerDto> = emptyList(),
    val porches: List<PorchDto> = emptyList(),
    val performances: List<PerformanceDto> = emptyList(),
    /** Null when the server predates the sponsors list; see SyncEngine for the fallback. */
    val sponsors: List<SponsorListItemDto>? = null,
)

@Serializable
internal data class GenreDto(val id: Int, val name: String, val slug: String)

@Serializable
internal data class PerformerDto(
    val id: String,
    val slug: String,
    val name: String,
    val bio: String = "",
    val link: String = "",
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("image_url") val imageUrl: String? = null,
)

@Serializable
internal data class SponsorDto(
    val name: String,
    val level: Int? = null,
    val website: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
) {
    companion object {
        /** Community, the lowest tier: where a sponsor with no stated level is listed. */
        const val DEFAULT_LEVEL = 4
    }
}

/** An entry in the snapshot's top-level `sponsors` list. */
@Serializable
internal data class SponsorListItemDto(
    val id: Int,
    val name: String,
    val level: Int = SponsorDto.DEFAULT_LEVEL,
    val website: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("porch_id") val porchId: String? = null,
)

@Serializable
internal data class PorchDto(
    val id: String,
    val slug: String,
    val name: String = "",
    val number: Int? = null,
    val description: String = "",
    @SerialName("street_address") val streetAddress: String = "",
    val city: String = "",
    val lat: Double,
    val lng: Double,
    @SerialName("image_url") val imageUrl: String? = null,
    val sponsor: SponsorDto? = null,
    val sponsored: Boolean = false,
    val vendor: Boolean = false,
    @SerialName("childrens_activities") val childrensActivities: Boolean = false,
    @SerialName("after_party") val afterParty: Boolean = false,
    val parking: Boolean = false,
    @SerialName("info_booth") val infoBooth: Boolean = false,
    @SerialName("porta_potty") val portaPotty: Boolean = false,
    @SerialName("drinking_water") val drinkingWater: Boolean = false,
    @SerialName("bicycle_repair") val bicycleRepair: Boolean = false,
)

@Serializable
internal data class PerformanceDto(
    val id: String,
    @SerialName("porch_id") val porchId: String,
    @SerialName("performer_id") val performerId: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
)
