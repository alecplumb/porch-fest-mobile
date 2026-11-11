package org.towerporchfest.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.towerporchfest.shared.data.EventRepository
import org.towerporchfest.shared.data.SyncEngine
import org.towerporchfest.shared.data.SyncStatus
import org.towerporchfest.shared.model.SponsorGroup
import org.towerporchfest.shared.model.toGroups

data class HomeUiState(
    /** Sponsors by tier, highest tier first. Empty until the first sync. */
    val sponsorGroups: List<SponsorGroup> = emptyList(),
    val sync: SyncStatus = SyncStatus.Idle,
) {
    val isRefreshing: Boolean get() = sync is SyncStatus.Syncing
}

/** The home ("Porch") tab: festival art and the sponsor list. */
class HomeViewModel(
    repository: EventRepository,
    private val syncEngine: SyncEngine,
) : ViewModel() {
    val state: StateFlow<HomeUiState> =
        combine(repository.sponsors(), syncEngine.status) { sponsors, sync ->
            HomeUiState(sponsors.toGroups(), sync)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun refresh() {
        viewModelScope.launch { syncEngine.sync() }
    }
}
