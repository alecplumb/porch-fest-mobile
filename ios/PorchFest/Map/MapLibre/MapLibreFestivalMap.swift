import MapLibre
import Shared
import SwiftUI

private enum LayerID {
    static let source = "porches"
    static let clusters = "porch-clusters"
    static let clusterCount = "porch-cluster-count"
    static let selected = "porch-selected"
    static let pins = "porch-pins"
    static let pinNumbers = "porch-pin-numbers"
}

private enum PinAttribute {
    static let porchId = "porchId"
    static let number = "number"
    static let sponsored = "sponsored"
}

// The website's colours: gold porch pins, teal sponsored pins, indigo for clusters.
private let pinColor = UIColor(red: 0xF2 / 255, green: 0xB8 / 255, blue: 0x14 / 255, alpha: 1)
private let sponsoredColor = UIColor(red: 0x20 / 255, green: 0x8F / 255, blue: 0x95 / 255, alpha: 1)
private let indigo = UIColor(red: 0x21 / 255, green: 0x21 / 255, blue: 0x33 / 255, alpha: 1)
private let clusterColor = indigo

/// Fonts served by the OpenFreeMap styles. A style from another provider may need a different name.
private let labelFont = ["Noto Sans Bold"]

/// MapLibre implementation of `FestivalMapView`.
struct MapLibreFestivalMap: UIViewRepresentable {
    let pins: [FestivalPin]
    let selectedPorchId: String?
    let styleURL: URL?
    let onPinTap: (String) -> Void
    let onMapTap: () -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    func makeUIView(context: Context) -> MLNMapView {
        let mapView = MLNMapView(frame: .zero, styleURL: styleURL)
        mapView.delegate = context.coordinator
        mapView.setCenter(
            CLLocationCoordinate2D(
                latitude: FestivalMapDefaults.shared.LATITUDE,
                longitude: FestivalMapDefaults.shared.LONGITUDE
            ),
            zoomLevel: FestivalMapDefaults.shared.ZOOM,
            animated: false
        )

        let tap = UITapGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.handleTap(_:)))
        // Let the map's own double-tap-to-zoom win over our single tap.
        for recognizer in mapView.gestureRecognizers ?? [] where recognizer is UITapGestureRecognizer {
            tap.require(toFail: recognizer)
        }
        mapView.addGestureRecognizer(tap)
        return mapView
    }

    func updateUIView(_ mapView: MLNMapView, context: Context) {
        context.coordinator.parent = self
        context.coordinator.render(on: mapView)
    }

    final class Coordinator: NSObject, MLNMapViewDelegate {
        var parent: MapLibreFestivalMap
        private var renderedPins: [FestivalPin]?
        private var renderedSelection: String??

        init(_ parent: MapLibreFestivalMap) {
            self.parent = parent
        }

        func mapView(_ mapView: MLNMapView, didFinishLoading style: MLNStyle) {
            addPinLayers(to: style)
            renderedPins = nil
            renderedSelection = nil
            render(on: mapView)
        }

        /// Pushes the current pins and selection into the map, skipping work when nothing changed.
        func render(on mapView: MLNMapView) {
            guard let style = mapView.style,
                  let source = style.source(withIdentifier: LayerID.source) as? MLNShapeSource
            else { return }

            if renderedPins != parent.pins {
                source.shape = MLNShapeCollectionFeature(shapes: parent.pins.map(Self.feature))
                renderedPins = parent.pins
            }
            if renderedSelection != .some(parent.selectedPorchId) {
                let layer = style.layer(withIdentifier: LayerID.selected) as? MLNCircleStyleLayer
                layer?.predicate = Self.selectedPredicate(parent.selectedPorchId)
                renderedSelection = .some(parent.selectedPorchId)
            }
        }

        @objc func handleTap(_ recognizer: UITapGestureRecognizer) {
            guard recognizer.state == .ended, let mapView = recognizer.view as? MLNMapView else { return }
            let point = recognizer.location(in: mapView)
            let hitArea = CGRect(x: point.x - 16, y: point.y - 16, width: 32, height: 32)

            let pins = mapView.visibleFeatures(in: hitArea, styleLayerIdentifiers: [LayerID.pins])
            if let porchId = pins.first?.attribute(forKey: PinAttribute.porchId) as? String {
                parent.onPinTap(porchId)
                return
            }
            let clusters = mapView.visibleFeatures(in: hitArea, styleLayerIdentifiers: [LayerID.clusters])
            if let cluster = clusters.first {
                // Zoom into a tapped cluster so it breaks apart.
                mapView.setCenter(cluster.coordinate, zoomLevel: mapView.zoomLevel + 2, animated: true)
                return
            }
            parent.onMapTap()
        }

        private static func feature(_ pin: FestivalPin) -> MLNPointFeature {
            let feature = MLNPointFeature()
            feature.coordinate = CLLocationCoordinate2D(latitude: pin.latitude, longitude: pin.longitude)
            feature.attributes = [
                PinAttribute.porchId: pin.porchId,
                PinAttribute.number: pin.number,
                PinAttribute.sponsored: pin.sponsored,
            ]
            return feature
        }

        private static func selectedPredicate(_ porchId: String?) -> NSPredicate {
            NSPredicate(format: "%K == %@", PinAttribute.porchId, porchId ?? "")
        }

        private func addPinLayers(to style: MLNStyle) {
            guard style.source(withIdentifier: LayerID.source) == nil else { return }

            let source = MLNShapeSource(
                identifier: LayerID.source,
                features: [],
                options: [.clustered: true, .clusterRadius: 45, .maximumZoomLevelForClustering: 15]
            )
            style.addSource(source)

            let isCluster = NSPredicate(format: "cluster == YES")
            let isPin = NSPredicate(format: "cluster != YES")
            let white = NSExpression(forConstantValue: UIColor.white)

            let clusters = MLNCircleStyleLayer(identifier: LayerID.clusters, source: source)
            clusters.predicate = isCluster
            clusters.circleColor = NSExpression(forConstantValue: clusterColor)
            clusters.circleRadius = NSExpression(forConstantValue: 18)
            clusters.circleStrokeColor = white
            clusters.circleStrokeWidth = NSExpression(forConstantValue: 2)
            style.addLayer(clusters)

            let clusterCount = MLNSymbolStyleLayer(identifier: LayerID.clusterCount, source: source)
            clusterCount.predicate = isCluster
            clusterCount.text = NSExpression(format: "CAST(point_count, 'NSString')")
            clusterCount.textFontNames = NSExpression(forConstantValue: labelFont)
            clusterCount.textFontSize = NSExpression(forConstantValue: 13)
            clusterCount.textColor = white
            clusterCount.textAllowsOverlap = NSExpression(forConstantValue: true)
            clusterCount.textIgnoresPlacement = NSExpression(forConstantValue: true)
            style.addLayer(clusterCount)

            let selected = MLNCircleStyleLayer(identifier: LayerID.selected, source: source)
            selected.predicate = Self.selectedPredicate(nil)
            selected.circleColor = white
            selected.circleRadius = NSExpression(forConstantValue: 17)
            selected.circleStrokeColor = NSExpression(forConstantValue: UIColor.black)
            selected.circleStrokeWidth = NSExpression(forConstantValue: 3)
            style.addLayer(selected)

            let pins = MLNCircleStyleLayer(identifier: LayerID.pins, source: source)
            pins.predicate = isPin
            pins.circleColor = NSExpression(
                forConditional: NSPredicate(format: "%K == YES", PinAttribute.sponsored),
                trueExpression: NSExpression(forConstantValue: sponsoredColor),
                falseExpression: NSExpression(forConstantValue: pinColor)
            )
            pins.circleRadius = NSExpression(forConstantValue: 12)
            pins.circleStrokeColor = white
            pins.circleStrokeWidth = NSExpression(forConstantValue: 2)
            style.addLayer(pins)

            let numbers = MLNSymbolStyleLayer(identifier: LayerID.pinNumbers, source: source)
            numbers.predicate = isPin
            numbers.text = NSExpression(forKeyPath: PinAttribute.number)
            numbers.textFontNames = NSExpression(forConstantValue: labelFont)
            numbers.textFontSize = NSExpression(forConstantValue: 11)
            // Indigo reads on gold; white reads on teal.
            numbers.textColor = NSExpression(
                forConditional: NSPredicate(format: "%K == YES", PinAttribute.sponsored),
                trueExpression: white,
                falseExpression: NSExpression(forConstantValue: indigo)
            )
            numbers.textAllowsOverlap = NSExpression(forConstantValue: true)
            numbers.textIgnoresPlacement = NSExpression(forConstantValue: true)
            style.addLayer(numbers)
        }
    }
}
