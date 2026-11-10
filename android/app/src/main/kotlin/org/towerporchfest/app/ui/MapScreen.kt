package org.towerporchfest.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.towerporchfest.app.map.FestivalMap
import org.towerporchfest.shared.AppConfig
import org.towerporchfest.shared.presentation.MapViewModel
import org.towerporchfest.shared.presentation.PorchDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onOpenPorch: (String) -> Unit,
    viewModel: MapViewModel = koinViewModel(),
    config: AppConfig = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.refresh() }

    FestivalMap(
        pins = state.pins,
        selectedPorchId = state.selectedPorchId,
        onPinClick = viewModel::selectPorch,
        onMapClick = { viewModel.selectPorch(null) },
        styleUrl = config.mapStyleUrl,
        modifier = Modifier.fillMaxSize(),
    )

    state.selectedPorchId?.let { porchId ->
        ModalBottomSheet(onDismissRequest = { viewModel.selectPorch(null) }) {
            val detailViewModel: PorchDetailViewModel = koinViewModel(key = "sheet:$porchId") { parametersOf(porchId) }
            val detail by detailViewModel.state.collectAsStateWithLifecycle()
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PorchDetailContent(state = detail, showTitle = true)
                TextButton(
                    onClick = {
                        viewModel.selectPorch(null)
                        onOpenPorch(porchId)
                    },
                    modifier = Modifier.align(Alignment.End).padding(horizontal = 8.dp),
                ) { Text("Open full page") }
            }
        }
    }
}
