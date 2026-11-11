# Architecture

## What is shared and what is native

The shared module holds everything that does not draw pixels. Each app holds its UI, its map
engine, and its environment settings.

    shared/   Kotlin Multiplatform (Android, iOS)
      model/          Porch, ScheduleItem, MapPin, time formatting
      data/remote/    PorchFestApi: one Ktor call to the snapshot endpoint
      data/local/     SQLDelight database (src/commonMain/sqldelight/.../PorchFest.sq)
      data/           SyncEngine (writes), EventRepository (reads)
      presentation/   HomeViewModel, ScheduleViewModel, MapViewModel, PorchDetailViewModel
      di/             Koin modules and initKoin()

    android/  Jetpack Compose          ios/  SwiftUI
      ui/     screens                    Screens/
      map/    FestivalMap + MapLibre     Map/   FestivalMapView + MapLibre

View models are shared. Each one exposes a single `StateFlow` of an immutable UI state plus
functions for user actions. Android collects it with `collectAsStateWithLifecycle`; iOS iterates
it as an `AsyncSequence` in a `.task`, which [SKIE](https://skie.touchlab.co) makes possible.

## Offline sync

The backend returns the whole festival in one response, `GET /api/mobile/v1/snapshot`, with an
`ETag` header.

1. `SyncEngine.sync()` sends the saved ETag as `If-None-Match`.
2. On `304 Not Modified` nothing is downloaded and only the "last updated" time changes.
3. On `200` every table is emptied and refilled inside one database transaction. Replacing
   everything is what removes performances that were deleted on the server; the backend keeps
   no record of deletions to sync from.
4. On failure the saved data stays as it was and the UI shows when it was last updated.

The apps sync at launch, when they return to the foreground, and on pull to refresh. Screens
never read the network: they observe `EventRepository` flows over the database, which re-emit
after a sync.

The database is a cache of the server. If its schema changes, add a SQLDelight migration or
bump the database file name and let the next sync refill it.

The backend stores performance times without a date. The shared module keeps them as minutes
since midnight and treats an end time earlier than the start as the following day.

The snapshot carries a `schema_version`. A build refuses a version newer than it understands
and asks the user to update, so the backend can change the format without corrupting old apps.

## Sponsors

The home tab lists sponsors by tier, like the website's home page. The list comes from the
snapshot's top-level `sponsors` array and is stored with everything else. A backend that does
not send that array still attaches a sponsor to each sponsored porch, and `SyncEngine` builds
the list from those instead; that fallback misses sponsors with no porch on the map.
`SyncEngine.insertSponsors` is the only code that knows there are two sources.

Logos are loaded from the backend's URLs when shown (Coil on Android, `AsyncImage` on iOS) and
cached by those libraries. A logo that has never been loaded shows the sponsor's name when the
phone is offline.

## Maps

Both apps use MapLibre with [OpenFreeMap](https://openfreemap.org) tiles: open source, no API
key, no usage billing. Only the basemap needs a connection; pins come from the local database.
Downloading the festival area for offline use is planned and not built yet.

MapLibre is confined to one directory per app (`android/.../map/maplibre/`,
`ios/PorchFest/Map/MapLibre/`). The rest of each app uses a wrapper with its own types
(`FestivalMap`, `FestivalMapView`): pins in, taps out. Switching to Google Maps or MapKit means
writing one new file behind that wrapper.

## Dependency injection

Koin, configured in `shared/.../di/Koin.kt`. Each platform supplies two things in its
`platformModule`: the SQLDelight driver and the Ktor HTTP engine. Android gets view models with
`koinViewModel()`. Swift cannot call Koin's generic API, so `iosMain` exposes `KoinHelper.start`
and `ViewModelOwner`, which hands out view models and clears them when a screen goes away.

Koin resolves dependencies at runtime, so `KoinGraphTest` resolves every entry point in CI.

## Configuration

The shared module contains no URLs. Each app builds an `AppConfig` (base URL, environment name,
map style URL) from its own build settings and passes it to `initKoin`. Debug builds use the
local backend and release builds use production; see the README.

Debug builds have a Debug Settings screen (More tab) on both platforms. It is a separate screen
in each app, not shared UI, so each can add platform-specific developer settings; today it only
displays the `AppConfig` in use.

## Brand

Colours come from the website's stylesheet: indigo `#212133`, slate blue `#6F91A5`, gold
`#F2B814`, beige `#EBE2D8`. Android defines them in `theme/Color.kt` and `res/values/colors.xml`,
iOS in colour sets in `Assets.xcassets`. The website has no dark mode; the apps' dark themes are
built on the indigo. The festival logo is navy line art, so it is always shown on beige.

## Splitting into separate repos

The three directories are joined by two links, each one line:

- `android/settings.gradle.kts` has `includeBuild("../shared")`, which makes Gradle build
  `org.towerporchfest.mobile:shared` from source. Remove it and the same dependency is fetched
  from a Maven repository that `shared` publishes to.
- `ios/scripts/build-shared-framework.sh` runs `shared`'s Gradle build from an Xcode build
  phase. Replace it with a Swift package dependency on an XCFramework that `shared` publishes.

`shared/` and `android/` each have their own Gradle wrapper and version catalog. While they
are joined, keep the Kotlin and Android Gradle Plugin versions in the two catalogs identical.

## Versions

Kotlin's tested range decides the Android Gradle Plugin and Gradle versions. Before upgrading
Kotlin, AGP, Gradle or SKIE, check the
[Kotlin Multiplatform compatibility guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
and the [SKIE releases](https://github.com/touchlab/SKIE/releases): SKIE supports each Kotlin
version explicitly, so a Kotlin upgrade waits for a matching SKIE release.
