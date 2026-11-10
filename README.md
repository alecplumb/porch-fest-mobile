# Porch Fest Mobile

Native iOS and Android apps for [Tower Porch Fest](https://towerporchfest.org) (Fresno, CA).

The first feature is an event browser: the day's schedule with search and genre filters, a page
for each porch, and a map of every porch. The schedule is saved on the phone, so it keeps
working when festival-day cell coverage doesn't.

## Layout

| Directory  | What it is |
|------------|------------|
| `shared/`  | Kotlin Multiplatform module: networking, offline database, sync, and view models used by both apps |
| `android/` | Android app (Jetpack Compose) |
| `ios/`     | iOS app (SwiftUI) |
| `docs/`    | [Architecture](docs/architecture.md) and [CI runner setup](docs/ci-runner.md) |

Each directory builds on its own, so any of them can move to a separate repository later.
See [docs/architecture.md](docs/architecture.md#splitting-into-separate-repos).

## Requirements

- macOS with Xcode 26.4 or later (needed for iOS and for the shared module's iOS tests)
- JDK 17 or 21. Android Studio's bundled JDK works.
- Android SDK with platform 37
- [xcodegen](https://github.com/yonaskolb/XcodeGen): `brew install xcodegen`

## Backend

The apps read one endpoint of the festival's Django site
([porch-fest-django](https://github.com/PorchFest/porch-fest-django)):

    GET /api/mobile/v1/snapshot

It is not in the upstream repository yet. Until it is, run a checkout of the backend that has
the `mobile-snapshot-api` branch, listening on port 8000 of your machine. The server must list
`10.0.2.2` in `ALLOWED_HOSTS` for the Android emulator to reach it.

## Run

**Android**

    cd android
    ./gradlew :app:installLocalDebug

Or open `android/` in Android Studio and pick the `localDebug` variant.

**iOS**

    cd ios
    xcodegen generate
    open PorchFest.xcodeproj

Run the **PorchFest Local** scheme on a simulator. The Xcode project file is generated from
`ios/project.yml` and is not committed; rerun `xcodegen generate` after pulling changes to it.

**Tests**

    (cd shared && ./gradlew check)     # shared code, on Android host and iOS simulator
    (cd android && ./gradlew :app:testLocalDebugUnitTest :app:lintLocalDebug)
    (cd ios && xcodebuild test -project PorchFest.xcodeproj -scheme "PorchFest Local" \
        -destination "platform=iOS Simulator,name=iPhone 17")

## Environments

Each app chooses its backend at build time and passes it to the shared module.

| Environment | Backend | Android | iOS |
|-------------|---------|---------|-----|
| local | `http://10.0.2.2:8000` (emulator), `http://localhost:8000` (simulator) | `local` flavor | **PorchFest Local** scheme |
| prod  | `https://towerporchfest.org` | `prod` flavor | **PorchFest Prod** scheme |

Plain HTTP is allowed only in the local environment.

To use a phone, or a backend on another machine, override the local URL without committing it:

- Android: add `porchfest.baseUrl=http://<host>:8000` to `android/local.properties`
- iOS: create `ios/Config/Local.override.xcconfig` containing `PF_BASE_URL = http:/$()/<host>:8000`

To add an environment, add a product flavor in `android/app/build.gradle.kts`, and an xcconfig
file plus a configuration and scheme in `ios/project.yml`.
