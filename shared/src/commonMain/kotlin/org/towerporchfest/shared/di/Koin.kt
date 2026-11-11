package org.towerporchfest.shared.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.towerporchfest.shared.AppConfig
import org.towerporchfest.shared.data.EventRepository
import org.towerporchfest.shared.data.SyncEngine
import org.towerporchfest.shared.data.local.PorchFestDatabase
import org.towerporchfest.shared.data.remote.PorchFestApi
import org.towerporchfest.shared.data.remote.createHttpClient
import org.towerporchfest.shared.presentation.HomeViewModel
import org.towerporchfest.shared.presentation.MapViewModel
import org.towerporchfest.shared.presentation.PorchDetailViewModel
import org.towerporchfest.shared.presentation.ScheduleViewModel

/**
 * Supplies what differs per platform: the SQLDelight `SqlDriver` and the Ktor `HttpClientEngine`.
 */
internal expect val platformModule: Module

internal val networkModule = module {
    single { createHttpClient(engine = get()) }
    single { PorchFestApi(client = get(), baseUrl = get<AppConfig>().baseUrl) }
}

internal val databaseModule = module {
    single { PorchFestDatabase(driver = get()) }
}

internal val repositoryModule = module {
    single { EventRepository(database = get()) }
    single { SyncEngine(api = get(), database = get()) }
}

internal val viewModelModule = module {
    viewModel { HomeViewModel(repository = get(), syncEngine = get()) }
    viewModel { ScheduleViewModel(repository = get(), syncEngine = get()) }
    viewModel { MapViewModel(repository = get(), syncEngine = get()) }
    viewModel { (porchId: String) -> PorchDetailViewModel(porchId = porchId, repository = get()) }
}

/** Everything except the platform module, so tests can swap in fakes for the driver and engine. */
internal fun commonModules(config: AppConfig): List<Module> = listOf(
    module { single { config } },
    networkModule,
    databaseModule,
    repositoryModule,
    viewModelModule,
)

/**
 * Starts dependency injection for the app. Call once at launch. Android passes
 * `androidContext(...)` in [appDeclaration]; iOS calls this through `KoinHelper.start`.
 */
fun initKoin(config: AppConfig, appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(commonModules(config) + platformModule)
    }
