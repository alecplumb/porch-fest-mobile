import Shared
import SwiftUI

/// The festival map as the rest of the app sees it: pins in, taps out. Nothing outside
/// `Map/MapLibre` may import MapLibre, so the map engine can be replaced (for example with
/// MapKit) by changing only the body below.
///
/// Nearby pins are clustered. Tapping a pin calls `onPinTap` with its porch id; tapping
/// empty map calls `onMapTap`.
struct FestivalMapView: View {
    let pins: [MapPin]
    let selectedPorchId: String?
    let styleURL: String
    let onPinTap: (String) -> Void
    let onMapTap: () -> Void

    var body: some View {
        MapLibreFestivalMap(
            pins: pins.map(FestivalPin.init),
            selectedPorchId: selectedPorchId,
            styleURL: URL(string: styleURL),
            onPinTap: onPinTap,
            onMapTap: onMapTap
        )
    }
}

/// A pin in plain Swift types, so map implementations and tests don't depend on Kotlin classes.
struct FestivalPin: Equatable {
    let porchId: String
    let label: String
    /// Porch number as text, or empty when the porch has none.
    let number: String
    let latitude: Double
    let longitude: Double
    let sponsored: Bool
}

extension FestivalPin {
    init(_ pin: MapPin) {
        self.init(
            porchId: pin.porchId,
            label: pin.label,
            number: pin.number.map { String($0.intValue) } ?? "",
            latitude: pin.latitude,
            longitude: pin.longitude,
            sponsored: pin.sponsored
        )
    }
}
