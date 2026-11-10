package org.towerporchfest.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.DateFormat
import java.util.Date
import org.koin.androidx.compose.koinViewModel
import org.towerporchfest.shared.data.SyncStatus
import org.towerporchfest.shared.model.ScheduleItem
import org.towerporchfest.shared.presentation.ScheduleUiState
import org.towerporchfest.shared.presentation.ScheduleViewModel

@Composable
fun ScheduleScreen(
    onPorchClick: (String) -> Unit,
    viewModel: ScheduleViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Sync whenever the app comes to the foreground. SyncEngine ignores overlapping calls,
    // and an unchanged schedule costs one small 304 response.
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.refresh() }

    ScheduleContent(
        state = state,
        onSearchChange = viewModel::setSearch,
        onGenreClick = { genre -> viewModel.selectGenre(if (state.selectedGenre == genre) null else genre) },
        onRefresh = viewModel::refresh,
        onPorchClick = onPorchClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ScheduleContent(
    state: ScheduleUiState,
    onSearchChange: (String) -> Unit,
    onGenreClick: (org.towerporchfest.shared.model.Genre) -> Unit,
    onRefresh: () -> Unit,
    onPorchClick: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.search,
            onValueChange = onSearchChange,
            placeholder = { Text("Search bands, porches, streets") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (state.genres.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.genres, key = { it.id }) { genre ->
                    FilterChip(
                        selected = state.selectedGenre == genre,
                        onClick = { onGenreClick(genre) },
                        label = { Text(genre.name) },
                    )
                }
            }
        }
        SyncBanner(state)

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(Modifier.fillMaxSize()) {
                state.sections.forEach { section ->
                    stickyHeader(key = "time-${section.startMinute}") {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                section.title,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }
                    items(section.items, key = { it.performanceId }) { item ->
                        ScheduleRow(item, onClick = { onPorchClick(item.porchId) })
                        HorizontalDivider()
                    }
                }
                if (state.sections.isEmpty()) {
                    item(key = "empty") { EmptyMessage(state, Modifier.fillParentMaxSize()) }
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(item: ScheduleItem, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(item.performerName) },
        supportingContent = {
            Column {
                Text(item.porchLabel)
                if (item.genres.isNotEmpty()) {
                    Text(item.genres, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        overlineContent = { Text(item.timeRange) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun SyncBanner(state: ScheduleUiState) {
    val sync = state.sync
    val lastSynced = state.lastSyncedAtEpochMillis
    val (text, isError) = when {
        sync is SyncStatus.Failed && lastSynced != null ->
            "Couldn't update. Showing the schedule saved ${formatSyncTime(lastSynced)}." to true
        sync is SyncStatus.Failed -> "Couldn't load the schedule: ${sync.message}" to true
        lastSynced != null -> "Updated ${formatSyncTime(lastSynced)}" to false
        else -> return
    }
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    )
}

@Composable
private fun EmptyMessage(state: ScheduleUiState, modifier: Modifier = Modifier) {
    val message = when {
        state.isRefreshing -> "Loading the schedule…"
        state.hasNoData -> "No schedule yet. Pull down to try again once you're online."
        state.isFiltered -> "Nothing matches your search."
        else -> "No performances have been scheduled yet."
    }
    Box(modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatSyncTime(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))
