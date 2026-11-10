package org.towerporchfest.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.towerporchfest.shared.data.EventRepository
import org.towerporchfest.shared.model.Porch
import org.towerporchfest.shared.model.PorchPerformance

data class PorchDetailUiState(
    /** Null while loading, or if a sync removed the porch. */
    val porch: Porch? = null,
    val performances: List<PorchPerformance> = emptyList(),
)

class PorchDetailViewModel(
    porchId: String,
    repository: EventRepository,
) : ViewModel() {
    val state: StateFlow<PorchDetailUiState> =
        combine(repository.porch(porchId), repository.performancesAt(porchId), ::PorchDetailUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PorchDetailUiState())
}
