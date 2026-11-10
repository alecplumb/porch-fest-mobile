package org.towerporchfest.app.map

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import org.towerporchfest.shared.model.MapPin

class PinGeoJsonTest {
    @Test
    fun pinsBecomeAFeatureCollectionWithLongitudeFirst() {
        val pins = listOf(
            MapPin("porch-blue", "#7 Blue Porch", 7, latitude = 36.765, longitude = -119.805, performanceCount = 3, sponsored = true),
            MapPin("porch-plain", "9 Wishon Ave", null, latitude = 36.76, longitude = -119.8, performanceCount = 0, sponsored = false),
        )

        val root = Json.parseToJsonElement(pins.toGeoJson()).jsonObject
        assertEquals("FeatureCollection", root.getValue("type").jsonPrimitive.content)

        val features = root.getValue("features").jsonArray
        assertEquals(2, features.size)

        val first = features[0].jsonObject
        val coordinates = first.getValue("geometry").jsonObject.getValue("coordinates").jsonArray
        assertEquals(-119.805, coordinates[0].jsonPrimitive.double, 0.0)
        assertEquals(36.765, coordinates[1].jsonPrimitive.double, 0.0)
        val properties = first.getValue("properties").jsonObject
        assertEquals("porch-blue", properties.getValue(PinProperties.ID).jsonPrimitive.content)
        assertEquals("7", properties.getValue(PinProperties.NUMBER).jsonPrimitive.content)

        val second = features[1].jsonObject.getValue("properties").jsonObject
        assertEquals("", second.getValue(PinProperties.NUMBER).jsonPrimitive.content)
    }

    @Test
    fun noPinsIsStillValidGeoJson() {
        val root = Json.parseToJsonElement(emptyList<MapPin>().toGeoJson()).jsonObject
        assertEquals(0, root.getValue("features").jsonArray.size)
    }
}
