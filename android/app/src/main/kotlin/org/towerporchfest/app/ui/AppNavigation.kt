package org.towerporchfest.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

/** The tabbed home screen: Schedule and Map. */
@Serializable
data object Home : NavKey

@Serializable
data class PorchDetail(val porchId: String) : NavKey

/** Developer-only settings, reachable from the More tab in debug builds. */
@Serializable
data object DebugSettings : NavKey

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        // Gives each destination its own saved state and its own ViewModel scope, so a
        // screen's shared view model is cleared when the screen leaves the back stack.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(
                    onPorchClick = { porchId -> backStack.add(PorchDetail(porchId)) },
                    onOpenDebugSettings = { backStack.add(DebugSettings) },
                )
            }
            entry<PorchDetail> { key ->
                PorchDetailScreen(porchId = key.porchId, onBack = { backStack.removeLastOrNull() })
            }
            entry<DebugSettings> {
                DebugSettingsScreen(onBack = { backStack.removeLastOrNull() })
            }
        },
    )
}
