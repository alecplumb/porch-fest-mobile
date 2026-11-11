import Foundation
import Shared

/// Environment settings for this build. The values come from the active xcconfig
/// (Config/Local.xcconfig for Debug, Config/Prod.xcconfig for Release) by way of Info.plist.
struct AppEnvironment: Equatable {
    let baseURL: String
    let name: String

    static let current = AppEnvironment(infoDictionary: Bundle.main.infoDictionary ?? [:])

    init(infoDictionary: [String: Any]) {
        let url = (infoDictionary["PFBaseURL"] as? String)?.trimmingCharacters(in: .whitespaces) ?? ""
        // An empty value means the xcconfig was not applied; fail safe to production.
        baseURL = url.isEmpty ? "https://towerporchfest.org" : url
        name = (infoDictionary["PFEnvironment"] as? String).flatMap { $0.isEmpty ? nil : $0 } ?? "prod"
    }

    var sharedConfig: AppConfig {
        AppConfig(baseUrl: baseURL, environment: name, mapStyleUrl: AppConfig.companion.DEFAULT_MAP_STYLE_URL)
    }
}
