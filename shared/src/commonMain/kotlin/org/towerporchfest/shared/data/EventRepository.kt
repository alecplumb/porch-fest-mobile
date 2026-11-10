package org.towerporchfest.shared.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.towerporchfest.shared.data.local.PorchFestDatabase
import org.towerporchfest.shared.model.Amenity
import org.towerporchfest.shared.model.Genre
import org.towerporchfest.shared.model.MapPin
import org.towerporchfest.shared.model.Porch
import org.towerporchfest.shared.model.PorchPerformance
import org.towerporchfest.shared.model.ScheduleItem
import org.towerporchfest.shared.model.porchLabel

data class ScheduleFilter(
    /** Matches performer name, porch name or street address; blank matches everything. */
    val search: String = "",
    val genreId: Int? = null,
)

/** Read side of the offline database. Every flow re-emits when a sync replaces the data. */
class EventRepository internal constructor(
    database: PorchFestDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val queries = database.porchFestQueries

    fun schedule(filter: ScheduleFilter): Flow<List<ScheduleItem>> =
        queries.scheduleRows(search = filter.search.trim(), genreId = filter.genreId?.toLong())
            .asFlow()
            .mapToList(dispatcher)
            .map { rows ->
                rows.map { row ->
                    ScheduleItem(
                        performanceId = row.performance_id,
                        startMinute = row.start_minute.toInt(),
                        endMinute = row.end_minute.toInt(),
                        performerName = row.performer_name,
                        performerImageUrl = row.performer_image_url,
                        genres = row.genre_names,
                        porchId = row.porch_id,
                        porchName = row.porch_name,
                        porchNumber = row.porch_number?.toInt(),
                        streetAddress = row.street_address,
                    )
                }
            }

    fun genres(): Flow<List<Genre>> =
        queries.genresInUse()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { Genre(it.id.toInt(), it.name, it.slug) } }

    fun mapPins(): Flow<List<MapPin>> =
        queries.mapPins()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows ->
                rows.map { row ->
                    MapPin(
                        porchId = row.id,
                        label = porchLabel(row.name, row.number?.toInt(), row.street_address),
                        number = row.number?.toInt(),
                        latitude = row.lat,
                        longitude = row.lng,
                        performanceCount = row.performance_count.toInt(),
                        sponsored = row.sponsored,
                    )
                }
            }

    fun porch(id: String): Flow<Porch?> =
        queries.porchById(id)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { row ->
                row?.let {
                    Porch(
                        id = it.id,
                        slug = it.slug,
                        name = it.name,
                        number = it.number?.toInt(),
                        details = it.description,
                        streetAddress = it.street_address,
                        city = it.city,
                        latitude = it.lat,
                        longitude = it.lng,
                        imageUrl = it.image_url,
                        sponsorName = it.sponsor_name,
                        sponsorLogoUrl = it.sponsor_logo_url,
                        amenities = buildList {
                            if (it.vendor) add(Amenity.VENDOR)
                            if (it.childrens_activities) add(Amenity.CHILDRENS_ACTIVITIES)
                            if (it.after_party) add(Amenity.AFTER_PARTY)
                            if (it.parking) add(Amenity.PARKING)
                            if (it.info_booth) add(Amenity.INFO_BOOTH)
                            if (it.porta_potty) add(Amenity.RESTROOM)
                            if (it.drinking_water) add(Amenity.DRINKING_WATER)
                            if (it.bicycle_repair) add(Amenity.BICYCLE_REPAIR)
                        },
                    )
                }
            }

    fun performancesAt(porchId: String): Flow<List<PorchPerformance>> =
        queries.performancesForPorch(porchId)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows ->
                rows.map { row ->
                    PorchPerformance(
                        performanceId = row.performance_id,
                        startMinute = row.start_minute.toInt(),
                        endMinute = row.end_minute.toInt(),
                        performerName = row.performer_name,
                        performerBio = row.performer_bio,
                        performerLink = row.performer_link,
                        performerImageUrl = row.performer_image_url,
                        genres = row.genre_names,
                    )
                }
            }

    /** Epoch milliseconds of the last successful sync, or null if there has never been one. */
    fun lastSyncedAt(): Flow<Long?> =
        queries.selectSyncMeta()
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.last_synced_at }
}
