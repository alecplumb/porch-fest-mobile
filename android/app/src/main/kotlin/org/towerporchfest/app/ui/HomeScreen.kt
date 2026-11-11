package org.towerporchfest.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import org.towerporchfest.app.R

private enum class HomeTab(val label: String, val icon: Int) {
    PORCH("Porch", R.drawable.ic_porch),
    SCHEDULE("Schedule", R.drawable.ic_schedule),
    MAP("Map", R.drawable.ic_map),
    MORE("More", R.drawable.ic_more),
}

@Composable
fun HomeScreen(onPorchClick: (String) -> Unit, onOpenDebugSettings: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(HomeTab.PORCH) }
    // Keeps each tab's scroll position and text input when switching between them.
    val tabState = rememberSaveableStateHolder()

    Scaffold(
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(painterResource(item.icon), contentDescription = null) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            tabState.SaveableStateProvider(tab) {
                when (tab) {
                    HomeTab.PORCH -> PorchHomeScreen()
                    HomeTab.SCHEDULE -> ScheduleScreen(onPorchClick = onPorchClick)
                    HomeTab.MAP -> MapScreen(onOpenPorch = onPorchClick)
                    HomeTab.MORE -> MoreScreen(onOpenDebugSettings = onOpenDebugSettings)
                }
            }
        }
    }
}
