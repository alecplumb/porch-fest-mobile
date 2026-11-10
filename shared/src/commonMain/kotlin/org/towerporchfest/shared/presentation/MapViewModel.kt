package org.towerporchfest.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.towerporchfest.shared.data.EventRepository
import org.towerporchfest.shared.data.SyncEngine
import org.towerporchfest.shared.model.MapPin

data class MapUiState(
    val pins: List<MapPin> = emptyList(),
    /** Porch whose pin is selected, if any. */
    val selectedPorchId: String? = null,
)

class MapViewModel(
    repository: EventRepository,
    private val syncEngine: SyncEngine,
) : ViewModel() {
    private val selectedPorchId = MutableStateFlow<String?>(null)

    val state: StateFlow<MapUiState> =
        combine(repository.mapPins(), selectedPorchId) { pins, selected ->
            // Drop the selection if a sync removed that porch.
            MapUiState(pins, selected?.takeIf { id -> pins.any { it.porchId == id } })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    /** Pass null to clear the selection. */
    fun selectPorch(porchId: String?) = selectedPorchId.update { porchId }

    fun refresh() {
        viewModelScope.launch { syncEngine.sync() }
    }
}
