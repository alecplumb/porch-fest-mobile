# Porch Fest Mobile

Native iOS and Android apps for [Tower Porch Fest](https://towerporchfest.org) (Fresno, CA).

The apps have a home tab with the festival's sponsors, the day's schedule with search and genre
filters, a page for each porch, and a map of every porch. The schedule is saved on the phone, so
it keeps working when festival-day cell coverage doesn't.

## Layout

| Directory  | What it is |
|------------|------------|
| `shared/`  | Kotlin Multiplatform module: networking, offline database, sync, and view models used by both apps |
| `android/` | Android app (Jetpack Compose) |
| `ios/`     | iOS app (SwiftUI) |
| `docs/`    | [Architecture](docs/architecture.md) and [CI runner setup](docs/ci-runner.md) |
| `tools/`   | `make-brand-assets.sh`, which rebuilds the icon, splash and logo from the website's art |

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
    ./gradlew :app:installDebug

Or open `android/` in Android Studio and run the `debug` variant.

**iOS**

    cd ios
    xcodegen generate
    open PorchFest.xcodeproj

Run the **PorchFest** scheme on a simulator. The Xcode project file is generated from
`ios/project.yml` and is not committed; rerun `xcodegen generate` after pulling changes to it.

**Tests**

    (cd shared && ./gradlew check)     # shared code, on Android host and iOS simulator
    (cd android && ./gradlew :app:testDebugUnitTest :app:lintDebug)
    (cd ios && xcodebuild test -project PorchFest.xcodeproj -scheme PorchFest \
        -destination "platform=iOS Simulator,name=iPhone 17")

## Environments

Debug builds talk to a backend on your machine; release builds talk to production. Each app
reads the choice from its build settings and passes it to the shared module.

| Build | Environment | Backend |
|-------|-------------|---------|
| Debug | local | `http://10.0.2.2:8000` (Android emulator), `http://localhost:8000` (iOS simulator) |
| Release | prod | `https://towerporchfest.org` |

Plain HTTP is allowed only in debug builds. Debug builds install alongside release builds
(their app ID ends in `.debug`) and show a **Debug Settings** screen under the More tab, which
displays the environment in use. Choosing an environment from that screen is planned.

To use a phone, or a backend on another machine, override the debug URL without committing it:

- Android: add `porchfest.baseUrl=http://<host>:8000` to `android/local.properties`
- iOS: create `ios/Config/Local.override.xcconfig` containing `PF_BASE_URL = http:/$()/<host>:8000`

The URLs are set in `android/app/build.gradle.kts` (`buildTypes`) and in
`ios/Config/Local.xcconfig` and `ios/Config/Prod.xcconfig`.

## Brand

Colours, the app icon, the splash screen and the home tab's logo come from the festival website.
The images are generated from the website's art:

    tools/make-brand-assets.sh /path/to/porch-fest-django

Run it again when the organizers publish new art, and commit the results. It needs ImageMagick.

## License

[GPL-3.0](LICENSE), the same license as the festival's website.
