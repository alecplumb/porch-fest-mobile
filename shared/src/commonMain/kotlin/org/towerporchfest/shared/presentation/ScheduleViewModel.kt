package org.towerporchfest.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.towerporchfest.shared.data.EventRepository
import org.towerporchfest.shared.data.ScheduleFilter
import org.towerporchfest.shared.data.SyncEngine
import org.towerporchfest.shared.data.SyncStatus
import org.towerporchfest.shared.model.Genre
import org.towerporchfest.shared.model.ScheduleItem
import org.towerporchfest.shared.model.ScheduleSection
import org.towerporchfest.shared.model.formatMinuteOfDay

data class ScheduleUiState(
    val sections: List<ScheduleSection> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val search: String = "",
    val selectedGenre: Genre? = null,
    val sync: SyncStatus = SyncStatus.Idle,
    /** Epoch milliseconds of the last successful sync; null before the first one. */
    val lastSyncedAtEpochMillis: Long? = null,
) {
    val isRefreshing: Boolean get() = sync is SyncStatus.Syncing
    val isFiltered: Boolean get() = search.isNotBlank() || selectedGenre != null
    /** Nothing has ever been downloaded, as opposed to a filter matching nothing. */
    val hasNoData: Boolean get() = sections.isEmpty() && !isFiltered && lastSyncedAtEpochMillis == null
}

class ScheduleViewModel(
    private val repository: EventRepository,
    private val syncEngine: SyncEngine,
) : ViewModel() {
    private val search = MutableStateFlow("")
    private val selectedGenre = MutableStateFlow<Genre?>(null)

    private val filter = combine(search, selectedGenre) { text, genre -> ScheduleFilter(text, genre?.id) }

    private val sections = filter
        .flatMapLatest { repository.schedule(it) }
        .map { it.toSections() }

    val state: StateFlow<ScheduleUiState> =
        combine(
            combine(sections, repository.genres(), ::Pair),
            combine(search, selectedGenre, ::Pair),
            syncEngine.status,
            repository.lastSyncedAt(),
        ) { (sections, genres), (search, genre), sync, lastSynced ->
            ScheduleUiState(sections, genres, search, genre, sync, lastSynced)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleUiState())

    fun setSearch(text: String) = search.update { text }

    /** Pass null to clear the genre filter. */
    fun selectGenre(genre: Genre?) = selectedGenre.update { genre }

    fun refresh() {
        viewModelScope.launch { syncEngine.sync() }
    }
}

internal fun List<ScheduleItem>.toSections(): List<ScheduleSection> =
    groupBy { it.startMinute }
        .map { (minute, items) -> ScheduleSection(minute, formatMinuteOfDay(minute), items) }
        .sortedBy { it.startMinute }
