import Shared
import SwiftUI

struct ScheduleView: View {
    @State private var scope = ScreenScope()
    @State private var observed: ScheduleUiState?
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        let viewModel = scope.viewModels.scheduleViewModel()
        let state = observed ?? viewModel.state.value

        List {
            // Filters and sync status scroll with the list, under the navigation title and search field.
            Section {
                GenreChips(state: state) { genre in
                    viewModel.selectGenre(genre: state.selectedGenre == genre ? nil : genre)
                }
                .listRowInsets(EdgeInsets())
                SyncBanner(state: state)
            }
            .listRowSeparator(.hidden)

            ForEach(state.sections, id: \.startMinute) { section in
                Section(section.title) {
                    ForEach(section.items, id: \.performanceId) { item in
                        NavigationLink(value: PorchRoute(porchId: item.porchId)) {
                            ScheduleRow(item: item)
                        }
                    }
                }
            }
        }
        .listStyle(.plain)
        .overlay {
            if state.sections.isEmpty {
                EmptyScheduleView(state: state)
            }
        }
        .navigationTitle("Schedule")
        .searchable(
            text: Binding(get: { state.search }, set: { viewModel.setSearch(text: $0) }),
            prompt: "Bands, porches, streets"
        )
        .refreshable { viewModel.refresh() }
        // Collects the shared StateFlow while the screen is visible.
        .task {
            for await next in viewModel.state {
                observed = next
            }
        }
        // Sync at launch and whenever the app returns to the foreground. The shared SyncEngine
        // ignores overlapping calls, and an unchanged schedule costs one small 304 response.
        .onChange(of: scenePhase, initial: true) { _, phase in
            if phase == .active { viewModel.refresh() }
        }
    }
}

private struct ScheduleRow: View {
    let item: ScheduleItem

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(item.timeRange)
                .font(.caption)
                .foregroundStyle(.secondary)
            Text(item.performerName)
                .font(.headline)
            Text(item.porchLabel)
                .font(.subheadline)
            if !item.genres.isEmpty {
                Text(item.genres)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 2)
    }
}

private struct GenreChips: View {
    let state: ScheduleUiState
    let onTap: (Genre) -> Void

    var body: some View {
        if !state.genres.isEmpty {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(state.genres, id: \.id) { genre in
                        let selected = state.selectedGenre == genre
                        Button(genre.name) { onTap(genre) }
                            .buttonStyle(.bordered)
                            .buttonBorderShape(.capsule)
                            .tint(selected ? Color.accentColor : Color.gray)
                            .foregroundStyle(selected ? Color.accentColor : Color.primary)
                            .fontWeight(selected ? .semibold : .regular)
                            .accessibilityAddTraits(selected ? .isSelected : [])
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
            }
        }
    }
}

private struct SyncBanner: View {
    let state: ScheduleUiState

    var body: some View {
        if let message {
            Text(message.text)
                .font(.caption)
                .foregroundStyle(message.isError ? AnyShapeStyle(.red) : AnyShapeStyle(.secondary))
                .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    private var message: (text: String, isError: Bool)? {
        let lastSynced = state.lastSyncedAtEpochMillis.map { formatSyncTime(epochMillis: $0.int64Value) }
        switch onEnum(of: state.sync) {
        case .failed(let failure):
            if let lastSynced {
                return ("Couldn't update. Showing the schedule saved \(lastSynced).", true)
            }
            return ("Couldn't load the schedule: \(failure.message)", true)
        case .idle, .syncing:
            return lastSynced.map { ("Updated \($0)", false) }
        }
    }
}

private struct EmptyScheduleView: View {
    let state: ScheduleUiState

    var body: some View {
        if state.isRefreshing {
            ProgressView("Loading the schedule…")
        } else if state.hasNoData {
            ContentUnavailableView(
                "No schedule yet",
                systemImage: "wifi.slash",
                description: Text("Pull down to try again once you're online.")
            )
        } else if state.isFiltered {
            ContentUnavailableView.search
        } else {
            ContentUnavailableView("No performances have been scheduled yet", systemImage: "calendar")
        }
    }
}

private func formatSyncTime(epochMillis: Int64) -> String {
    Date(timeIntervalSince1970: TimeInterval(epochMillis) / 1000)
        .formatted(date: .abbreviated, time: .shortened)
}
