import SwiftUI

private let creatorCompany = "Etherwalker Labs"
private let creatorName = "Alec Plumb"
private let creatorEmail = "alec@etherwalker.com"

/// The More tab: about the app and, in debug builds, the way into Debug Settings.
struct MoreView: View {
    var body: some View {
        List {
            Section("About") {
                LabeledContent(AppVersion.displayName, value: "Version \(AppVersion.version) (\(AppVersion.build))")
            }

            Section("Created by") {
                VStack(alignment: .leading, spacing: 2) {
                    Text(creatorCompany)
                    Text(creatorName).font(.subheadline).foregroundStyle(.secondary)
                }
                if let mail = URL(string: "mailto:\(creatorEmail)") {
                    Link(creatorEmail, destination: mail)
                }
            }

            #if DEBUG
            Section("Developer") {
                NavigationLink("Debug Settings") { DebugSettingsView() }
            }
            #endif
        }
        .navigationTitle("More")
    }
}

/// The app's name and version as built, read from Info.plist.
enum AppVersion {
    static let displayName = string("CFBundleDisplayName") ?? string("CFBundleName") ?? "Porch Fest"
    static let version = string("CFBundleShortVersionString") ?? "?"
    static let build = string("CFBundleVersion") ?? "?"

    private static func string(_ key: String) -> String? {
        Bundle.main.object(forInfoDictionaryKey: key) as? String
    }
}
