package org.towerporchfest.shared

/**
 * Environment settings, supplied by each app at startup. The shared module holds no
 * environment constants of its own: Android reads these from build flavors, iOS from xcconfig.
 */
data class AppConfig(
    /** Backend root without a trailing slash, e.g. `http://10.0.2.2:8000`. */
    val baseUrl: String,
    /** Short label such as `local` or `prod`, for logs and debug UI. */
    val environment: String,
    /** MapLibre style JSON URL for the basemap. */
    val mapStyleUrl: String,
) {
    companion object {
        /** Free, keyless OpenStreetMap vector tiles from OpenFreeMap. */
        const val DEFAULT_MAP_STYLE_URL: String = "https://tiles.openfreemap.org/styles/liberty"
    }
}

/** Where the map opens before the user moves it: the Tower District, Fresno. */
object FestivalMapDefaults {
    const val LATITUDE: Double = 36.765
    const val LONGITUDE: Double = -119.805
    const val ZOOM: Double = 14.0
}
