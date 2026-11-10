import Shared
import SwiftUI

@main
struct PorchFestApp: App {
    init() {
        // Starts dependency injection in the shared Kotlin module. Must run once, before any view.
        KoinHelper.shared.start(config: AppEnvironment.current.sharedConfig)
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
