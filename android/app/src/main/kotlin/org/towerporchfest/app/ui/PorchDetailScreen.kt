package org.towerporchfest.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.towerporchfest.app.R
import org.towerporchfest.shared.model.PorchPerformance
import org.towerporchfest.shared.presentation.PorchDetailUiState
import org.towerporchfest.shared.presentation.PorchDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PorchDetailScreen(porchId: String, onBack: () -> Unit) {
    val viewModel: PorchDetailViewModel = koinViewModel(key = porchId) { parametersOf(porchId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.porch?.label.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        PorchDetailContent(
            state = state,
            showTitle = false,
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        )
    }
}

/** Porch details without any screen chrome, so the map's bottom sheet can show the same content. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PorchDetailContent(state: PorchDetailUiState, showTitle: Boolean, modifier: Modifier = Modifier) {
    val porch = state.porch ?: return
    Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showTitle) Text(porch.label, style = MaterialTheme.typography.headlineSmall)
        Text(
            listOf(porch.streetAddress, porch.city).filter { it.isNotBlank() }.joinToString(", "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (porch.amenities.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                porch.amenities.forEach { amenity ->
                    SuggestionChip(onClick = {}, label = { Text(amenity.label) })
                }
            }
        }
        if (porch.details.isNotBlank()) Text(porch.details)
        porch.sponsorName?.let {
            Text("Sponsored by $it", style = MaterialTheme.typography.labelLarge)
        }

        Text("Performances", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        if (state.performances.isEmpty()) {
            Text("No performances scheduled here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        state.performances.forEach { performance ->
            HorizontalDivider()
            PerformanceBlock(performance)
        }
    }
}

@Composable
private fun PerformanceBlock(performance: PorchPerformance) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(performance.timeRange, style = MaterialTheme.typography.labelMedium)
        Text(performance.performerName, style = MaterialTheme.typography.titleSmall)
        if (performance.genres.isNotEmpty()) {
            Text(performance.genres, style = MaterialTheme.typography.bodySmall)
        }
        if (performance.performerBio.isNotBlank()) {
            Text(performance.performerBio, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
