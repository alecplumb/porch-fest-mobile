#if DEBUG
import SwiftUI

/// Developer settings, reachable from the More tab in debug builds only.
///
/// For now this shows which environment the build is using. Environment selection goes in the
/// "Environment" section; iOS-only developer settings get their own section below it.
struct DebugSettingsView: View {
    private let environment = AppEnvironment.current

    var body: some View {
        List {
            Section("Environment") {
                LabeledContent("Environment", value: environment.name)
                LabeledContent("Backend", value: environment.baseURL)
            }
        }
        .navigationTitle("Debug Settings")
        .navigationBarTitleDisplayMode(.inline)
    }
}
#endif
