import Shared
import SwiftUI

struct PorchDetailView: View {
    let porchId: String

    @State private var scope = ScreenScope()
    @State private var observed: PorchDetailUiState?

    var body: some View {
        let viewModel = scope.viewModels.porchDetailViewModel(porchId: porchId)
        let state = observed ?? viewModel.state.value

        ScrollView {
            PorchDetailContent(state: state, showTitle: false)
                .padding()
        }
        .navigationTitle(state.porch?.label ?? "")
        .navigationBarTitleDisplayMode(.inline)
        .task(id: porchId) {
            for await next in viewModel.state {
                observed = next
            }
        }
    }
}

/// Porch details without any screen chrome, so the map's sheet can show the same content.
struct PorchDetailContent: View {
    let state: PorchDetailUiState
    let showTitle: Bool

    var body: some View {
        if let porch = state.porch {
            VStack(alignment: .leading, spacing: 12) {
                if showTitle {
                    Text(porch.label).font(.title2.bold())
                }
                Text([porch.streetAddress, porch.city].filter { !$0.isEmpty }.joined(separator: ", "))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                if !porch.amenities.isEmpty {
                    Text(porch.amenities.map(\.label).joined(separator: " · "))
                        .font(.footnote)
                }
                if !porch.details.isEmpty {
                    Text(porch.details)
                }
                if let sponsor = porch.sponsorName {
                    Text("Sponsored by \(sponsor)").font(.footnote.weight(.semibold))
                }

                Text("Performances").font(.headline).padding(.top, 4)
                if state.performances.isEmpty {
                    Text("No performances scheduled here.").foregroundStyle(.secondary)
                }
                ForEach(state.performances, id: \.performanceId) { performance in
                    Divider()
                    VStack(alignment: .leading, spacing: 2) {
                        Text(performance.timeRange).font(.caption).foregroundStyle(.secondary)
                        Text(performance.performerName).font(.subheadline.weight(.semibold))
                        if !performance.genres.isEmpty {
                            Text(performance.genres).font(.caption).foregroundStyle(.secondary)
                        }
                        if !performance.performerBio.isEmpty {
                            Text(performance.performerBio).font(.callout)
                        }
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}
