package org.towerporchfest.shared.data

import co.touchlab.kermit.Logger
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.towerporchfest.shared.data.local.Porch_row as PorchRow
import org.towerporchfest.shared.data.local.PorchFestDatabase
import org.towerporchfest.shared.data.remote.PorchFestApi
import org.towerporchfest.shared.data.remote.SnapshotDto
import org.towerporchfest.shared.data.remote.SnapshotResponse
import org.towerporchfest.shared.model.normalizeEndMinute
import org.towerporchfest.shared.model.parseMinuteOfDay

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    /** The last attempt failed; whatever was synced before is still available offline. */
    data class Failed(val message: String) : SyncStatus
}

/**
 * Keeps the local database in step with the backend. Each sync is one conditional GET of the
 * whole snapshot; when the server has new data, every table is replaced in a single transaction,
 * which also removes anything deleted on the server.
 */
class SyncEngine internal constructor(
    private val api: PorchFestApi,
    private val database: PorchFestDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val nowEpochMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    private val log = Logger.withTag("SyncEngine")
    private val mutex = Mutex()
    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    /**
     * Runs one sync and returns true when local data is now current. Safe to call from
     * anywhere at any time: a call made while another sync is running returns false at once.
     */
    suspend fun sync(): Boolean {
        if (!mutex.tryLock()) return false
        try {
            _status.value = SyncStatus.Syncing
            withContext(dispatcher) { syncOnce() }
            _status.value = SyncStatus.Idle
            return true
        } catch (e: CancellationException) {
            _status.value = SyncStatus.Idle
            throw e
        } catch (e: Exception) {
            log.w(e) { "Sync failed" }
            _status.value = SyncStatus.Failed(e.message ?: "Could not reach the server")
            return false
        } finally {
            mutex.unlock()
        }
    }

    private suspend fun syncOnce() {
        val queries = database.porchFestQueries
        val meta = queries.selectSyncMeta().executeAsOneOrNull()
        // Only send the ETag if we still hold the data it describes.
        val etag = meta?.etag?.takeIf { queries.countPerformances().executeAsOne() > 0 }

        when (val response = api.fetchSnapshot(etag)) {
            SnapshotResponse.NotModified -> queries.touchSyncMeta(nowEpochMillis())
            is SnapshotResponse.Fresh -> {
                val snapshot = response.snapshot
                if (snapshot.schemaVersion > SUPPORTED_SCHEMA_VERSION) {
                    throw UnsupportedSnapshotException()
                }
                replaceAll(snapshot, response.etag)
                log.i { "Synced ${snapshot.performances.size} performances at ${snapshot.porches.size} porches" }
            }
        }
    }

    private fun replaceAll(snapshot: SnapshotDto, etag: String?) {
        val queries = database.porchFestQueries
        database.transaction {
            queries.deletePerformances()
            queries.deletePerformerGenres()
            queries.deletePerformers()
            queries.deletePorches()
            queries.deleteGenres()

            snapshot.genres.forEach { queries.insertGenre(it.id.toLong(), it.name, it.slug) }
            snapshot.performers.forEach { performer ->
                queries.insertPerformer(
                    id = performer.id,
                    slug = performer.slug,
                    name = performer.name,
                    bio = performer.bio,
                    link = performer.link,
                    image_url = performer.imageUrl,
                )
                performer.genreIds.forEach { queries.insertPerformerGenre(performer.id, it.toLong()) }
            }
            snapshot.porches.forEach { porch ->
                queries.insertPorch(
                    PorchRow(
                        id = porch.id,
                        slug = porch.slug,
                        name = porch.name,
                        number = porch.number?.toLong(),
                        description = porch.description,
                        street_address = porch.streetAddress,
                        city = porch.city,
                        lat = porch.lat,
                        lng = porch.lng,
                        image_url = porch.imageUrl,
                        sponsor_name = porch.sponsor?.name,
                        sponsor_level = porch.sponsor?.level?.toLong(),
                        sponsor_website = porch.sponsor?.website,
                        sponsor_logo_url = porch.sponsor?.logoUrl,
                        sponsored = porch.sponsored,
                        vendor = porch.vendor,
                        childrens_activities = porch.childrensActivities,
                        after_party = porch.afterParty,
                        parking = porch.parking,
                        info_booth = porch.infoBooth,
                        porta_potty = porch.portaPotty,
                        drinking_water = porch.drinkingWater,
                        bicycle_repair = porch.bicycleRepair,
                    )
                )
            }
            snapshot.performances.forEach { performance ->
                val start = parseMinuteOfDay(performance.startTime)
                queries.insertPerformance(
                    id = performance.id,
                    porch_id = performance.porchId,
                    performer_id = performance.performerId,
                    start_minute = start.toLong(),
                    end_minute = normalizeEndMinute(start, parseMinuteOfDay(performance.endTime)).toLong(),
                )
            }
            queries.saveSyncMeta(etag, nowEpochMillis())
        }
    }

    internal companion object {
        /** Highest snapshot `schema_version` this build understands. */
        const val SUPPORTED_SCHEMA_VERSION = 1
    }
}

internal class UnsupportedSnapshotException :
    Exception("This version of the app is out of date. Please update to see the latest schedule.")
