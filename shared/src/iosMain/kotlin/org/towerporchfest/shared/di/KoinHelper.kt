package org.towerporchfest.shared.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform
import org.towerporchfest.shared.AppConfig
import org.towerporchfest.shared.presentation.MapViewModel
import org.towerporchfest.shared.presentation.PorchDetailViewModel
import org.towerporchfest.shared.presentation.ScheduleViewModel

/** Swift-facing entry point to dependency injection. Koin's own API stays out of Swift. */
object KoinHelper {
    /** Call once from the SwiftUI `App` initialiser. */
    fun start(config: AppConfig) {
        initKoin(config)
    }
}

/**
 * Owns the shared view models for one SwiftUI screen. Create one per screen-level object and
 * call [clear] when that object is deallocated; this cancels each view model's coroutines,
 * the same way Android does when a screen leaves the back stack.
 */
class ViewModelOwner {
    private val store = ViewModelStore()

    fun scheduleViewModel(): ScheduleViewModel = resolve("schedule") { KoinPlatform.getKoin().get() }

    fun mapViewModel(): MapViewModel = resolve("map") { KoinPlatform.getKoin().get() }

    fun porchDetailViewModel(porchId: String): PorchDetailViewModel =
        resolve("porch:$porchId") { KoinPlatform.getKoin().get { parametersOf(porchId) } }

    fun clear() = store.clear()

    @Suppress("UNCHECKED_CAST")
    private fun <T : ViewModel> resolve(key: String, create: () -> T): T =
        store[key] as T? ?: create().also { store.put(key, it) }
}
