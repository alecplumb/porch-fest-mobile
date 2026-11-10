import Shared
import SwiftUI

struct MapScreen: View {
    @State private var scope = ScreenScope()
    @State private var observed: MapUiState?
    @State private var path: PorchRoute?
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        let viewModel = scope.viewModels.mapViewModel()
        let state = observed ?? viewModel.state.value

        FestivalMapView(
            pins: state.pins,
            selectedPorchId: state.selectedPorchId,
            styleURL: AppEnvironment.current.sharedConfig.mapStyleUrl,
            onPinTap: { viewModel.selectPorch(porchId: $0) },
            onMapTap: { viewModel.selectPorch(porchId: nil) }
        )
        .ignoresSafeArea(edges: .top)
        .navigationTitle("Map")
        .toolbar(.hidden, for: .navigationBar)
        .sheet(
            item: Binding(
                get: { state.selectedPorchId.map(PorchRoute.init(porchId:)) },
                set: { if $0 == nil { viewModel.selectPorch(porchId: nil) } }
            )
        ) { route in
            PorchSheet(porchId: route.porchId) {
                viewModel.selectPorch(porchId: nil)
                path = route
            }
        }
        .navigationDestination(item: $path) { PorchDetailView(porchId: $0.porchId) }
        .task {
            for await next in viewModel.state {
                observed = next
            }
        }
        .onChange(of: scenePhase, initial: true) { _, phase in
            if phase == .active { viewModel.refresh() }
        }
    }
}

extension PorchRoute: Identifiable {
    var id: String { porchId }
}

private struct PorchSheet: View {
    let porchId: String
    let onOpenFullPage: () -> Void

    @State private var scope = ScreenScope()
    @State private var observed: PorchDetailUiState?

    var body: some View {
        let viewModel = scope.viewModels.porchDetailViewModel(porchId: porchId)
        let state = observed ?? viewModel.state.value

        ScrollView {
            VStack(alignment: .trailing, spacing: 12) {
                PorchDetailContent(state: state, showTitle: true)
                Button("Open full page", action: onOpenFullPage)
            }
            .padding()
        }
        .presentationDetents([.medium, .large])
        .task(id: porchId) {
            for await next in viewModel.state {
                observed = next
            }
        }
    }
}
