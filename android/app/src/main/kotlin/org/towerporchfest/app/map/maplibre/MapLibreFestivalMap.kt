package org.towerporchfest.app.map.maplibre

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.towerporchfest.app.map.PinProperties
import org.towerporchfest.app.map.toGeoJson
import org.towerporchfest.shared.FestivalMapDefaults
import org.towerporchfest.shared.model.MapPin

private const val SOURCE = "porches"
private const val LAYER_CLUSTERS = "porch-clusters"
private const val LAYER_CLUSTER_COUNT = "porch-cluster-count"
private const val LAYER_PINS = "porch-pins"
private const val LAYER_PIN_NUMBERS = "porch-pin-numbers"
private const val LAYER_SELECTED = "porch-selected"

// The website's colours: gold porch pins, teal sponsored pins, indigo for clusters.
private val PIN_COLOR = Color.parseColor("#F2B814")
private val SPONSORED_COLOR = Color.parseColor("#208F95")
private val CLUSTER_COLOR = Color.parseColor("#212133")
private val INDIGO = Color.parseColor("#212133")

/** Fonts served by the OpenFreeMap styles. A style from another provider may need a different name. */
private val LABEL_FONT = arrayOf("Noto Sans Bold")

/** MapLibre implementation of [org.towerporchfest.app.map.FestivalMap]. */
@Composable
internal fun MapLibreFestivalMap(
    pins: List<MapPin>,
    selectedPorchId: String?,
    onPinClick: (String) -> Unit,
    onMapClick: () -> Unit,
    styleUrl: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentOnPinClick by rememberUpdatedState(onPinClick)
    val currentOnMapClick by rememberUpdatedState(onMapClick)

    // Set once the style has loaded and the pin layers exist.
    var loadedStyle by remember { mutableStateOf<Style?>(null) }

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(FestivalMapDefaults.LATITUDE, FestivalMapDefaults.LONGITUDE))
                    .zoom(FestivalMapDefaults.ZOOM)
                    .build()
                map.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
                    style.addPinLayers()
                    loadedStyle = style
                }
                map.addOnMapClickListener { point ->
                    handleTap(map, point, onPin = { currentOnPinClick(it) }, onEmpty = { currentOnMapClick() })
                    true
                }
            }
        }
    }

    // MapView needs the host lifecycle forwarded to it by hand.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            loadedStyle = null
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(loadedStyle, pins) {
        loadedStyle?.getSourceAs<GeoJsonSource>(SOURCE)?.setGeoJson(pins.toGeoJson())
    }
    LaunchedEffect(loadedStyle, selectedPorchId) {
        loadedStyle?.getLayerAs<CircleLayer>(LAYER_SELECTED)?.setFilter(selectedFilter(selectedPorchId))
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

private fun selectedFilter(porchId: String?): Expression =
    Expression.eq(Expression.get(PinProperties.ID), Expression.literal(porchId.orEmpty()))

private fun Style.addPinLayers() {
    addSource(
        GeoJsonSource(
            SOURCE,
            emptyList<MapPin>().toGeoJson(),
            GeoJsonOptions().withCluster(true).withClusterRadius(45).withClusterMaxZoom(15),
        )
    )

    val isCluster = Expression.has("point_count")
    val isPin = Expression.not(isCluster)

    addLayer(
        CircleLayer(LAYER_CLUSTERS, SOURCE).withFilter(isCluster).withProperties(
            PropertyFactory.circleColor(CLUSTER_COLOR),
            PropertyFactory.circleRadius(18f),
            PropertyFactory.circleStrokeColor(Color.WHITE),
            PropertyFactory.circleStrokeWidth(2f),
        )
    )
    addLayer(
        SymbolLayer(LAYER_CLUSTER_COUNT, SOURCE).withFilter(isCluster).withProperties(
            PropertyFactory.textField(Expression.toString(Expression.get("point_count"))),
            PropertyFactory.textFont(LABEL_FONT),
            PropertyFactory.textSize(13f),
            PropertyFactory.textColor(Color.WHITE),
            PropertyFactory.textAllowOverlap(true),
            PropertyFactory.textIgnorePlacement(true),
        )
    )
    addLayer(
        CircleLayer(LAYER_SELECTED, SOURCE).withFilter(selectedFilter(null)).withProperties(
            PropertyFactory.circleColor(Color.WHITE),
            PropertyFactory.circleRadius(17f),
            PropertyFactory.circleStrokeColor(Color.BLACK),
            PropertyFactory.circleStrokeWidth(3f),
        )
    )
    addLayer(
        CircleLayer(LAYER_PINS, SOURCE).withFilter(isPin).withProperties(
            PropertyFactory.circleColor(
                Expression.switchCase(
                    Expression.get(PinProperties.SPONSORED), Expression.color(SPONSORED_COLOR),
                    Expression.color(PIN_COLOR),
                )
            ),
            PropertyFactory.circleRadius(12f),
            PropertyFactory.circleStrokeColor(Color.WHITE),
            PropertyFactory.circleStrokeWidth(2f),
        )
    )
    addLayer(
        SymbolLayer(LAYER_PIN_NUMBERS, SOURCE).withFilter(isPin).withProperties(
            PropertyFactory.textField(Expression.get(PinProperties.NUMBER)),
            PropertyFactory.textFont(LABEL_FONT),
            PropertyFactory.textSize(11f),
            // Indigo reads on gold; white reads on teal.
            PropertyFactory.textColor(
                Expression.switchCase(
                    Expression.get(PinProperties.SPONSORED), Expression.color(Color.WHITE),
                    Expression.color(INDIGO),
                )
            ),
            PropertyFactory.textAllowOverlap(true),
            PropertyFactory.textIgnorePlacement(true),
        )
    )
}

private fun handleTap(map: MapLibreMap, point: LatLng, onPin: (String) -> Unit, onEmpty: () -> Unit) {
    val screenPoint = map.projection.toScreenLocation(point)

    val pin = map.queryRenderedFeatures(screenPoint, LAYER_PINS).firstOrNull()
    if (pin != null) {
        onPin(pin.getStringProperty(PinProperties.ID))
        return
    }
    if (map.queryRenderedFeatures(screenPoint, LAYER_CLUSTERS).isNotEmpty()) {
        // Zoom into a tapped cluster so it breaks apart.
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, map.cameraPosition.zoom + 2.0))
        return
    }
    onEmpty()
}
