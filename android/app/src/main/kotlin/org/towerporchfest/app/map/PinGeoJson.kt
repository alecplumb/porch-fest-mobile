package org.towerporchfest.app.map

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.towerporchfest.shared.model.MapPin

/** Feature property names shared between the GeoJSON built here and the map layers that read it. */
internal object PinProperties {
    const val ID = "id"
    const val LABEL = "label"
    /** Porch number as text, or empty when the porch has none. */
    const val NUMBER = "number"
    const val SPONSORED = "sponsored"
}

/** Pins as a GeoJSON FeatureCollection string. GeoJSON coordinates are [longitude, latitude]. */
internal fun List<MapPin>.toGeoJson(): String = buildJsonObject {
    put("type", "FeatureCollection")
    putJsonArray("features") {
        forEach { pin ->
            addJsonObject {
                put("type", "Feature")
                putJsonObject("geometry") {
                    put("type", "Point")
                    put("coordinates", JsonArray(listOf(JsonPrimitive(pin.longitude), JsonPrimitive(pin.latitude))))
                }
                putJsonObject("properties") {
                    put(PinProperties.ID, pin.porchId)
                    put(PinProperties.LABEL, pin.label)
                    put(PinProperties.NUMBER, pin.number?.toString().orEmpty())
                    put(PinProperties.SPONSORED, pin.sponsored)
                }
            }
        }
    }
}.toString()
