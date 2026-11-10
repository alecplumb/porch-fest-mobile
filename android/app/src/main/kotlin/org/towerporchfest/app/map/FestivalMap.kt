package org.towerporchfest.app.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.towerporchfest.app.map.maplibre.MapLibreFestivalMap
import org.towerporchfest.shared.model.MapPin

/**
 * The festival map as the rest of the app sees it: pins in, taps out. Nothing outside the
 * `map.maplibre` package may use MapLibre types, so the map engine can be replaced (for
 * example with Google Maps) by changing only the call below.
 *
 * Nearby pins are clustered. Tapping a pin calls [onPinClick] with its porch id; tapping
 * empty map calls [onMapClick].
 */
@Composable
fun FestivalMap(
    pins: List<MapPin>,
    selectedPorchId: String?,
    onPinClick: (porchId: String) -> Unit,
    onMapClick: () -> Unit,
    styleUrl: String,
    modifier: Modifier = Modifier,
) {
    MapLibreFestivalMap(pins, selectedPorchId, onPinClick, onMapClick, styleUrl, modifier)
}
