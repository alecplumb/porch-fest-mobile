import SwiftUI

/// Navigation value for a porch's detail page.
struct PorchRoute: Hashable {
    let porchId: String
}

enum RootTab: String {
    case porch, schedule, map, more
}

struct RootView: View {
    // Pass `-initialTab map` (or schedule, more) as a launch argument to open on that tab, for
    // screenshots and UI tests.
    @State private var tab = RootTab(rawValue: UserDefaults.standard.string(forKey: "initialTab") ?? "") ?? .porch

    var body: some View {
        TabView(selection: $tab) {
            NavigationStack {
                PorchHomeView()
            }
            .tabItem { Label("Porch", systemImage: "house") }
            .tag(RootTab.porch)

            NavigationStack {
                ScheduleView()
                    .navigationDestination(for: PorchRoute.self) { PorchDetailView(porchId: $0.porchId) }
            }
            .tabItem { Label("Schedule", systemImage: "calendar") }
            .tag(RootTab.schedule)

            NavigationStack {
                MapScreen()
                    .navigationDestination(for: PorchRoute.self) { PorchDetailView(porchId: $0.porchId) }
            }
            .tabItem { Label("Map", systemImage: "map") }
            .tag(RootTab.map)

            NavigationStack {
                MoreView()
            }
            .tabItem { Label("More", systemImage: "ellipsis") }
            .tag(RootTab.more)
        }
    }
}
