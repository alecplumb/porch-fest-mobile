import Shared
import SwiftUI

/// The home tab: festival art, the date, and the sponsors, laid out like the website's home page.
struct PorchHomeView: View {
    @State private var scope = ScreenScope()
    @State private var observed: HomeUiState?
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        let viewModel = scope.viewModels.homeViewModel()
        let state = observed ?? viewModel.state.value

        ScrollView {
            VStack(spacing: 0) {
                FestivalArt()
                DateBand()
                ForEach(state.sponsorGroups, id: \.level) { group in
                    SponsorGroupView(group: group)
                }
            }
            .padding(.bottom, 24)
        }
        .toolbar(.hidden, for: .navigationBar)
        .refreshable { viewModel.refresh() }
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

// The logo is navy line art, so it always sits on the website's beige, in light and dark mode.
private struct FestivalArt: View {
    var body: some View {
        Image("BrandLogo")
            .resizable()
            .scaledToFit()
            .frame(height: 300)
            .padding(24)
            .frame(maxWidth: .infinity)
            .background(Color.brandBeige)
            .accessibilityLabel("\(FestivalInfo.shared.NAME): building community through music and art")
    }
}

private struct DateBand: View {
    var body: some View {
        VStack(spacing: 4) {
            Text(FestivalInfo.shared.LOCATION.uppercased())
                .font(.subheadline.weight(.heavy))
                .kerning(2)
                .foregroundStyle(Color.brandGold)
            Text(FestivalInfo.shared.DATE)
                .font(.title)
                .foregroundStyle(.white)
            Text(FestivalInfo.shared.TAGLINE.uppercased())
                .font(.caption)
                .foregroundStyle(.white)
                .multilineTextAlignment(.center)
        }
        .padding(.vertical, 20)
        .padding(.horizontal)
        .frame(maxWidth: .infinity)
        .background(Color.brandIndigo)
    }
}

private struct SponsorGroupView: View {
    let group: SponsorGroup

    var body: some View {
        // The top tier gets full-width logos; the others sit two to a row.
        let columns = Array(repeating: GridItem(.flexible(), spacing: 12), count: group.level == 0 ? 1 : 2)

        VStack(alignment: .leading, spacing: 12) {
            Text(group.title.uppercased())
                .font(.headline)
            LazyVGrid(columns: columns, spacing: 12) {
                ForEach(group.sponsors, id: \.id) { sponsor in
                    SponsorCard(sponsor: sponsor)
                }
            }
        }
        .padding(.horizontal)
        .padding(.top, 24)
    }
}

/// Sponsor logos are 2:1 artwork on white, so the card is always white, whatever the appearance.
private struct SponsorCard: View {
    let sponsor: Sponsor
    @Environment(\.openURL) private var openURL

    var body: some View {
        let website = URL(string: sponsor.website)

        Button {
            if let website { openURL(website) }
        } label: {
            logo
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .aspectRatio(2, contentMode: .fit)
                .background(.white)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(.separator), lineWidth: 1))
        }
        .buttonStyle(.plain)
        .disabled(website == nil || sponsor.website.isEmpty)
        .accessibilityLabel(sponsor.name)
        .accessibilityHint(sponsor.website.isEmpty ? "" : "Opens the sponsor's website")
    }

    @ViewBuilder private var logo: some View {
        if let url = sponsor.logoUrl.flatMap(URL.init(string:)) {
            AsyncImage(url: url) { phase in
                switch phase {
                case .success(let image):
                    image.resizable().scaledToFit()
                case .empty:
                    Color.white
                default:
                    // Couldn't be loaded (for example offline before it was ever cached): show the name.
                    name
                }
            }
        } else {
            name
        }
    }

    private var name: some View {
        Text(sponsor.name)
            .font(.subheadline.weight(.semibold))
            .foregroundStyle(Color.brandIndigo)
            .multilineTextAlignment(.center)
            .padding(8)
    }
}
