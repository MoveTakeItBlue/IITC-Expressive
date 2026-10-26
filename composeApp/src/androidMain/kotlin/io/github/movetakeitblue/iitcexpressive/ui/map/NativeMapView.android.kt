package io.github.movetakeitblue.iitcexpressive.ui.map

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.movetakeitblue.iitcexpressive.model.NativeBaseMap
import io.github.movetakeitblue.iitcexpressive.model.NativeMapEntities
import io.github.movetakeitblue.iitcexpressive.viewmodel.MainViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Polygon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val SOURCE_FIELDS = "iitc-fields-source"
private const val SOURCE_LINKS = "iitc-links-source"
private const val SOURCE_PORTALS = "iitc-portals-source"
private const val SOURCE_PORTAL_SELECTION = "iitc-portal-selection-source"
private const val SOURCE_PLAYER_TRAILS = "iitc-player-trails-source"
private const val SOURCE_PLAYER_MARKERS = "iitc-player-markers-source"

private const val LAYER_FIELDS_FILL = "iitc-fields-fill"
private const val LAYER_FIELDS_STROKE = "iitc-fields-stroke"
private const val LAYER_LINKS = "iitc-links-layer"
private const val LAYER_PORTAL_SELECTION_HALO = "iitc-portal-selection-halo"
private const val LAYER_PORTAL_SELECTION_RING = "iitc-portal-selection-ring"
private const val LAYER_PORTALS_HALO = "iitc-portals-halo"
private const val LAYER_PORTALS = "iitc-portals-layer"
private const val LAYER_PORTALS_TITLE = "iitc-portals-title"
private const val LAYER_PLAYER_TRAILS_CASING = "iitc-player-trails-casing"
private const val LAYER_PLAYER_TRAILS = "iitc-player-trails"
private const val LAYER_PLAYER_MARKERS_HALO = "iitc-player-markers-halo"
private const val LAYER_PLAYER_MARKERS = "iitc-player-markers"

private data class SelectedPortalInfo(
    val lat: Double,
    val lng: Double,
    val level: Int,
    val team: Int
)

/**
 * Generates MapLibre raster base map style JSON without requiring any API keys.
 */
private fun getBaseMapStyleJson(baseMap: NativeBaseMap): String = when (baseMap) {
    NativeBaseMap.DARK -> """
    {
      "version": 8,
      "name": "DarkGrayCanvas",
      "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
      "sources": {
        "esri-dark-base": {
          "type": "raster",
          "tiles": [
            "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/{z}/{y}/{x}"
          ],
          "tileSize": 256,
          "maxzoom": 16,
          "attribution": "&copy; Esri, HERE, Garmin, &copy; OpenStreetMap contributors"
        },
        "esri-dark-ref": {
          "type": "raster",
          "tiles": [
            "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Reference/MapServer/tile/{z}/{y}/{x}"
          ],
          "tileSize": 256,
          "maxzoom": 16
        }
      },
      "layers": [
        {
          "id": "esri-dark-base-layer",
          "type": "raster",
          "source": "esri-dark-base",
          "minzoom": 0,
          "maxzoom": 22
        },
        {
          "id": "esri-dark-ref-layer",
          "type": "raster",
          "source": "esri-dark-ref",
          "minzoom": 0,
          "maxzoom": 22
        }
      ]
    }
    """.trimIndent()

    NativeBaseMap.LIGHT -> """
    {
      "version": 8,
      "name": "LightGrayCanvas",
      "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
      "sources": {
        "esri-light-base": {
          "type": "raster",
          "tiles": [
            "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}"
          ],
          "tileSize": 256,
          "maxzoom": 16,
          "attribution": "&copy; Esri, HERE, Garmin, &copy; OpenStreetMap contributors"
        },
        "esri-light-ref": {
          "type": "raster",
          "tiles": [
            "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Reference/MapServer/tile/{z}/{y}/{x}"
          ],
          "tileSize": 256,
          "maxzoom": 16
        }
      },
      "layers": [
        {
          "id": "esri-light-base-layer",
          "type": "raster",
          "source": "esri-light-base",
          "minzoom": 0,
          "maxzoom": 22
        },
        {
          "id": "esri-light-ref-layer",
          "type": "raster",
          "source": "esri-light-ref",
          "minzoom": 0,
          "maxzoom": 22
        }
      ]
    }
    """.trimIndent()

    NativeBaseMap.SATELLITE -> """
    {
      "version": 8,
      "name": "WorldImagery",
      "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
      "sources": {
        "esri-satellite": {
          "type": "raster",
          "tiles": [
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
          ],
          "tileSize": 256,
          "maxzoom": 18,
          "attribution": "&copy; Esri, Maxar, Earthstar Geographics"
        }
      },
      "layers": [
        {
          "id": "esri-satellite-layer",
          "type": "raster",
          "source": "esri-satellite",
          "minzoom": 0,
          "maxzoom": 22
        }
      ]
    }
    """.trimIndent()

    NativeBaseMap.COLOR -> """
    {
      "version": 8,
      "name": "OpenStreetMap",
      "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
      "sources": {
        "osm-color": {
          "type": "raster",
          "tiles": [
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
          ],
          "tileSize": 256,
          "maxzoom": 19,
          "attribution": "&copy; OpenStreetMap contributors"
        }
      },
      "layers": [
        {
          "id": "osm-color-layer",
          "type": "raster",
          "source": "osm-color",
          "minzoom": 0,
          "maxzoom": 22
        }
      ]
    }
    """.trimIndent()
}

@Composable
actual fun NativeMapView(
    viewModel: MainViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize MapLibre engine once
    remember {
        MapLibre.getInstance(context)
    }

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapStyle by remember { mutableStateOf<Style?>(null) }

    val entities by viewModel.nativeMapEntities.collectAsState()
    val isNativeMapEnabled by viewModel.isNativeMapEnabled.collectAsState()
    val nativeBaseMap by viewModel.nativeBaseMap.collectAsState()
    val selectedHighlighter by viewModel.selectedHighlighter.collectAsState()
    val isPlayerTrackerEnabled by viewModel.isPlayerTrackerEnabled.collectAsState()
    val playerTrails by viewModel.playerTrails.collectAsState()
    val portalStatus by viewModel.portalStatus.collectAsState()

    if (!isNativeMapEnabled) {
        return
    }

    // Helper to configure MapLibre style with Ingress & tactical layers
    fun loadMapStyle(map: MapLibreMap, baseMap: NativeBaseMap) {
        val styleJson = getBaseMapStyleJson(baseMap)
        val styleBuilder = Style.Builder().fromJson(styleJson)

        // GeoJSON Sources
        styleBuilder.withSource(GeoJsonSource(SOURCE_FIELDS))
        styleBuilder.withSource(GeoJsonSource(SOURCE_LINKS))
        styleBuilder.withSource(GeoJsonSource(SOURCE_PORTALS))
        styleBuilder.withSource(GeoJsonSource(SOURCE_PORTAL_SELECTION))
        styleBuilder.withSource(GeoJsonSource(SOURCE_PLAYER_TRAILS))
        styleBuilder.withSource(GeoJsonSource(SOURCE_PLAYER_MARKERS))

        // 1. Fields fill layer
        val fieldsFillLayer = FillLayer(LAYER_FIELDS_FILL, SOURCE_FIELDS).apply {
            setProperties(
                fillColor(get("color")),
                fillOpacity(0.22f),
                fillAntialias(true)
            )
        }
        styleBuilder.withLayer(fieldsFillLayer)

        // 2. Fields stroke layer
        val fieldsStrokeLayer = LineLayer(LAYER_FIELDS_STROKE, SOURCE_FIELDS).apply {
            setProperties(
                lineColor(get("color")),
                lineWidth(1.2f),
                lineOpacity(0.6f)
            )
        }
        styleBuilder.withLayer(fieldsStrokeLayer)

        // 3. Links layer
        val linksLayer = LineLayer(LAYER_LINKS, SOURCE_LINKS).apply {
            setProperties(
                lineColor(get("color")),
                lineWidth(2.2f),
                lineOpacity(0.85f),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND)
            )
        }
        styleBuilder.withLayer(linksLayer)

        // 4. Player Activity Tracker - Trails casing (dark outline for readability on all base maps)
        val playerTrailsCasing = LineLayer(LAYER_PLAYER_TRAILS_CASING, SOURCE_PLAYER_TRAILS).apply {
            setProperties(
                lineColor("#000000"),
                lineWidth(4.5f),
                lineOpacity(0.55f),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND)
            )
        }
        styleBuilder.withLayer(playerTrailsCasing)

        // 5. Player Activity Tracker - Trails dashed line
        val playerTrailsLayer = LineLayer(LAYER_PLAYER_TRAILS, SOURCE_PLAYER_TRAILS).apply {
            setProperties(
                lineColor(get("color")),
                lineWidth(2.6f),
                lineOpacity(0.9f),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineDasharray(arrayOf(2f, 2f))
            )
        }
        styleBuilder.withLayer(playerTrailsLayer)

        // 6. Selected Portal glowing halo layer
        val selectionHaloLayer = CircleLayer(LAYER_PORTAL_SELECTION_HALO, SOURCE_PORTAL_SELECTION).apply {
            setProperties(
                circleRadius(get("haloRadius")),
                circleColor(get("haloColor")),
                circleOpacity(0.55f),
                circleBlur(0.6f)
            )
        }
        styleBuilder.withLayer(selectionHaloLayer)

        // 7. Selected Portal prominent ring layer
        val selectionRingLayer = CircleLayer(LAYER_PORTAL_SELECTION_RING, SOURCE_PORTAL_SELECTION).apply {
            setProperties(
                circleRadius(get("ringRadius")),
                circleColor("rgba(0,0,0,0)"),
                circleOpacity(0f),
                circleStrokeColor(get("strokeColor")),
                circleStrokeWidth(3.5f),
                circleStrokeOpacity(1f)
            )
        }
        styleBuilder.withLayer(selectionRingLayer)

        // 8. Portals glowing halo layer (M3E dynamic light bloom)
        val portalsHaloLayer = CircleLayer(LAYER_PORTALS_HALO, SOURCE_PORTALS).apply {
            setProperties(
                circleRadius(get("haloRadius")),
                circleColor(get("haloColor")),
                circleOpacity(get("haloOpacity")),
                circleBlur(0.6f)
            )
        }
        styleBuilder.withLayer(portalsHaloLayer)

        // 9. Portals core circle layer
        val portalsLayer = CircleLayer(LAYER_PORTALS, SOURCE_PORTALS).apply {
            setProperties(
                circleRadius(get("radius")),
                circleColor(get("fillColor")),
                circleStrokeColor(get("strokeColor")),
                circleStrokeWidth(get("strokeWidth")),
                circleOpacity(get("opacity")),
                circleStrokeOpacity(get("strokeOpacity"))
            )
        }
        styleBuilder.withLayer(portalsLayer)

        // 10. Player Activity Tracker - Agent position radiant glow
        val playerMarkersHaloLayer = CircleLayer(LAYER_PLAYER_MARKERS_HALO, SOURCE_PLAYER_MARKERS).apply {
            setProperties(
                circleRadius(16f),
                circleColor(get("color")),
                circleOpacity(0.45f),
                circleBlur(0.7f)
            )
        }
        styleBuilder.withLayer(playerMarkersHaloLayer)

        // 11. Player Activity Tracker - Agent position marker
        val playerMarkersLayer = CircleLayer(LAYER_PLAYER_MARKERS, SOURCE_PLAYER_MARKERS).apply {
            setProperties(
                circleRadius(7f),
                circleColor(get("color")),
                circleStrokeColor("#ffffff"),
                circleStrokeWidth(2f),
                circleOpacity(1f)
            )
        }
        styleBuilder.withLayer(playerMarkersLayer)

        // 12. Portals title symbol layer (shown at high zoom)
        val portalTitlesLayer = SymbolLayer(LAYER_PORTALS_TITLE, SOURCE_PORTALS).apply {
            setProperties(
                textField(get("title")),
                textSize(11.5f),
                textColor("#ffffff"),
                textHaloColor("#000000"),
                textHaloWidth(2f),
                textHaloBlur(0.5f),
                textAnchor(Property.TEXT_ANCHOR_TOP),
                textOffset(arrayOf(0f, 1.35f)),
                textAllowOverlap(false),
                textIgnorePlacement(false),
                textFont(arrayOf("Noto Sans Regular", "Open Sans Regular", "sans-serif"))
            )
            minZoom = 15.5f
            maxZoom = 22f
        }
        styleBuilder.withLayer(portalTitlesLayer)

        map.setStyle(styleBuilder) { loadedStyle ->
            mapStyle = loadedStyle
        }
    }

    // Switch BaseMap when nativeBaseMap changes
    LaunchedEffect(nativeBaseMap, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        loadMapStyle(map, nativeBaseMap)
    }

    // Listen for smooth Camera Move events (e.g. from Linked Portals jump)
    LaunchedEffect(mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        viewModel.cameraMoveEvent.collect { (lat, lng, zoom) ->
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(lat, lng),
                    zoom
                ),
                800
            )
        }
    }

    // Update GeoJSON layers when entities, highlighters, or player activity changes
    LaunchedEffect(entities, selectedHighlighter, isPlayerTrackerEnabled, playerTrails, portalStatus, mapStyle) {
        val style = mapStyle ?: return@LaunchedEffect
        withContext(Dispatchers.Default) {
            val levelColors = listOf(
                "#c0c7c7", "#fece5a", "#ffa630", "#ff7315", "#e40000",
                "#fd2992", "#eb26cd", "#c124e0", "#9627f4"
            )

            // Build Portal features with dynamic M3E Highlighter styles
            val portalFeatures = ArrayList<Feature>(entities.portals.size)
            for (p in entities.portals) {
                val point = Point.fromLngLat(p.lng, p.lat)
                val feat = Feature.fromGeometry(point)
                feat.addStringProperty("guid", p.guid)
                feat.addStringProperty("title", p.title)
                feat.addNumberProperty("team", p.team)
                feat.addNumberProperty("level", p.level)
                feat.addNumberProperty("health", p.health)

                val (baseStroke, baseFill) = when (p.team) {
                    1 -> Pair("#70d2ff", "#004f7a") // RES
                    2 -> Pair("#66dc96", "#005a2e") // ENL
                    3 -> Pair("#ff7164", "#7a1000") // MACHINA
                    else -> Pair("#ffa630", "#e65100") // Unclaimed: Orange!
                }

                var strokeColor = baseStroke
                var fillColor = baseFill
                var strokeWidth = 2.2f
                var radius = when (p.level) {
                    0 -> 8f
                    1, 2 -> 9.5f
                    3, 4 -> 11f
                    5, 6 -> 13f
                    7, 8 -> 15.5f
                    else -> 9f
                }
                var opacity = 0.95f
                var strokeOpacity = 1f
                var haloColor = if (p.team == 0) "#ff7a00" else "#000000"
                var haloRadius = if (p.team == 0) radius + 3.5f else 0f
                var haloOpacity = if (p.team == 0) 0.35f else 0f

                when (selectedHighlighter) {
                    "Portal Level Color" -> {
                        val lvlCol = levelColors.getOrElse(p.level) { "#fece5a" }
                        fillColor = lvlCol
                        strokeColor = "#ffffff"
                        strokeWidth = 1.5f
                        haloColor = lvlCol
                        haloRadius = radius + 5f
                        haloOpacity = 0.65f
                    }
                    "High Level Portals" -> {
                        when {
                            p.level >= 8 -> {
                                fillColor = "#9627f4"
                                strokeColor = "#ffffff"
                                strokeWidth = 2.2f
                                radius = 9.5f
                                haloColor = "#9627f4"
                                haloRadius = 18f
                                haloOpacity = 0.9f
                            }
                            p.level == 7 -> {
                                fillColor = "#c124e0"
                                strokeColor = "#ffffff"
                                strokeWidth = 2.0f
                                radius = 8f
                                haloColor = "#c124e0"
                                haloRadius = 14f
                                haloOpacity = 0.75f
                            }
                            p.level == 6 -> {
                                fillColor = "#eb26cd"
                                radius = 6.5f
                                haloColor = "#eb26cd"
                                haloRadius = 10f
                                haloOpacity = 0.45f
                            }
                            else -> {
                                opacity = 0.25f
                                strokeOpacity = 0.25f
                                haloOpacity = 0f
                            }
                        }
                    }
                    "Needs Recharge" -> {
                        if (p.health < 100) {
                            when {
                                p.health < 25 -> {
                                    fillColor = "#d32f2f"
                                    strokeColor = "#ffffff"
                                    radius = 8.5f
                                    haloColor = "#ff1744"
                                    haloRadius = 16f
                                    haloOpacity = 0.95f
                                }
                                p.health < 50 -> {
                                    fillColor = "#f57c00"
                                    strokeColor = "#ffffff"
                                    radius = 7.5f
                                    haloColor = "#ff9100"
                                    haloRadius = 13f
                                    haloOpacity = 0.8f
                                }
                                p.health < 80 -> {
                                    fillColor = "#fbc02d"
                                    strokeColor = "#ffffff"
                                    radius = 6.5f
                                    haloColor = "#ffd600"
                                    haloRadius = 10f
                                    haloOpacity = 0.65f
                                }
                                else -> {
                                    fillColor = "#689f38"
                                    radius = 6f
                                    haloColor = "#aeea00"
                                    haloRadius = 8f
                                    haloOpacity = 0.5f
                                }
                            }
                        } else {
                            opacity = 0.25f
                            strokeOpacity = 0.25f
                            haloOpacity = 0f
                        }
                    }
                    "Missing Resonators" -> {
                        if (p.resCount in 1..7) {
                            val missing = 8 - p.resCount
                            fillColor = "#c2185b"
                            strokeColor = "#ff4081"
                            strokeWidth = 2.2f
                            radius = 7.5f
                            haloColor = "#ff4081"
                            haloRadius = 11f + (missing * 0.8f)
                            haloOpacity = 0.85f
                        } else if (p.resCount == 8) {
                            opacity = 0.35f
                            strokeOpacity = 0.35f
                            haloOpacity = 0f
                        }
                    }
                    "Portal Weakness" -> {
                        if (p.health < 50 || p.resCount <= 4) {
                            fillColor = "#d50000"
                            strokeColor = "#ff8a80"
                            strokeWidth = 2.2f
                            radius = 8.5f
                            haloColor = "#ff1744"
                            haloRadius = 16f
                            haloOpacity = 0.9f
                        } else if (p.health < 80 || p.resCount < 8) {
                            fillColor = "#ff6d00"
                            strokeColor = "#ffd180"
                            radius = 7.5f
                            haloColor = "#ffab00"
                            haloRadius = 12f
                            haloOpacity = 0.75f
                        } else {
                            opacity = 0.25f
                            strokeOpacity = 0.25f
                            haloOpacity = 0f
                        }
                    }
                    "Unclaimed Portals" -> {
                        if (p.team == 0) {
                            fillColor = "#ff7a00"
                            strokeColor = "#ffffff"
                            strokeWidth = 3f
                            radius = radius + 3.5f
                            haloColor = "#ff9100"
                            haloRadius = radius + 10f
                            haloOpacity = 0.95f
                        } else {
                            opacity = 0.2f
                            strokeOpacity = 0.2f
                            haloOpacity = 0f
                        }
                    }
                    "Hide Team Ownership" -> {
                        fillColor = "#282b2d"
                        strokeColor = "#8e9192"
                        strokeWidth = 1.5f
                        haloOpacity = 0f
                    }
                }

                feat.addStringProperty("strokeColor", strokeColor)
                feat.addStringProperty("fillColor", fillColor)
                feat.addNumberProperty("strokeWidth", strokeWidth)
                feat.addNumberProperty("radius", radius)
                feat.addNumberProperty("opacity", opacity)
                feat.addNumberProperty("strokeOpacity", strokeOpacity)
                feat.addStringProperty("haloColor", haloColor)
                feat.addNumberProperty("haloRadius", haloRadius)
                feat.addNumberProperty("haloOpacity", haloOpacity)
                portalFeatures.add(feat)
            }

            // Build Link features
            val linkFeatures = ArrayList<Feature>(entities.links.size)
            for (l in entities.links) {
                val line = LineString.fromLngLats(
                    listOf(
                        Point.fromLngLat(l.oLng, l.oLat),
                        Point.fromLngLat(l.dLng, l.dLat)
                    )
                )
                val feat = Feature.fromGeometry(line)
                feat.addStringProperty("guid", l.guid)
                feat.addNumberProperty("team", l.team)
                val color = when (l.team) {
                    1 -> "#70d2ff"
                    2 -> "#66dc96"
                    3 -> "#ff7164"
                    else -> "#c0c7c7"
                }
                feat.addStringProperty("color", color)
                linkFeatures.add(feat)
            }

            // Build Field features
            val fieldFeatures = ArrayList<Feature>(entities.fields.size)
            for (f in entities.fields) {
                if (f.points.size >= 3) {
                    val ring = f.points.map { Point.fromLngLat(it[1], it[0]) }.toMutableList()
                    if (ring.first() != ring.last()) {
                        ring.add(ring.first())
                    }
                    val poly = Polygon.fromLngLats(listOf(ring))
                    val feat = Feature.fromGeometry(poly)
                    feat.addStringProperty("guid", f.guid)
                    feat.addNumberProperty("team", f.team)
                    val color = when (f.team) {
                        1 -> "#70d2ff"
                        2 -> "#66dc96"
                        3 -> "#ff7164"
                        else -> "#c0c7c7"
                    }
                    feat.addStringProperty("color", color)
                    fieldFeatures.add(feat)
                }
            }

            // Build Player Activity Tracker features
            val trailFeatures = ArrayList<Feature>()
            val markerFeatures = ArrayList<Feature>()
            if (isPlayerTrackerEnabled && playerTrails.isNotEmpty()) {
                for (trail in playerTrails) {
                    val color = when (trail.team.uppercase()) {
                        "RESISTANCE", "RES", "1" -> "#70d2ff"
                        "ENLIGHTENED", "ENL", "2" -> "#66dc96"
                        "MACHINA", "MAC", "3" -> "#ff7164"
                        else -> "#c0c7c7"
                    }

                    if (trail.points.size >= 2) {
                        val coords = trail.points.map { Point.fromLngLat(it.lng, it.lat) }
                        val line = LineString.fromLngLats(coords)
                        val lineFeat = Feature.fromGeometry(line)
                        lineFeat.addStringProperty("sender", trail.sender)
                        lineFeat.addStringProperty("team", trail.team)
                        lineFeat.addStringProperty("color", color)
                        trailFeatures.add(lineFeat)
                    }

                    val point = Point.fromLngLat(trail.latestLng, trail.latestLat)
                    val ptFeat = Feature.fromGeometry(point)
                    ptFeat.addStringProperty("sender", trail.sender)
                    ptFeat.addStringProperty("team", trail.team)
                    ptFeat.addStringProperty("color", color)
                    ptFeat.addStringProperty("portal", trail.latestPortalName)
                    markerFeatures.add(ptFeat)
                }
            }

            // Build Selected Portal Halo & Ring Feature
            val selectionFeatures = ArrayList<Feature>(1)
            val pStatus = portalStatus
            val currentGuid = pStatus.guid
            val latE6 = pStatus.latE6
            val lngE6 = pStatus.lngE6
            if (!currentGuid.isNullOrBlank()) {
                val selPortal = entities.portals.find { it.guid == currentGuid }
                val selInfo = when {
                    selPortal != null -> SelectedPortalInfo(selPortal.lat, selPortal.lng, selPortal.level, selPortal.team)
                    latE6 != null && lngE6 != null -> {
                        val teamInt = when (pStatus.team.uppercase()) {
                            "R", "RESISTANCE" -> 1
                            "E", "ENLIGHTENED" -> 2
                            "M", "MACHINA" -> 3
                            else -> 0
                        }
                        SelectedPortalInfo(latE6 / 1e6, lngE6 / 1e6, pStatus.level, teamInt)
                    }
                    else -> null
                }

                if (selInfo != null && selInfo.lat != 0.0 && selInfo.lng != 0.0) {
                    val selPoint = Point.fromLngLat(selInfo.lng, selInfo.lat)
                    val selFeat = Feature.fromGeometry(selPoint)
                    val baseRadius = when (selInfo.level) {
                        0 -> 8f
                        1, 2 -> 9.5f
                        3, 4 -> 11f
                        5, 6 -> 13f
                        7, 8 -> 15.5f
                        else -> 9f
                    }
                    val haloCol = when (selInfo.team) {
                        1 -> "#70d2ff"
                        2 -> "#66dc96"
                        3 -> "#ff7164"
                        else -> "#ffa630"
                    }
                    selFeat.addNumberProperty("ringRadius", baseRadius + 4.5f)
                    selFeat.addStringProperty("strokeColor", "#ffffff")
                    selFeat.addNumberProperty("haloRadius", baseRadius + 11f)
                    selFeat.addStringProperty("haloColor", haloCol)
                    selectionFeatures.add(selFeat)
                }
            }

            val portalCollection = FeatureCollection.fromFeatures(portalFeatures)
            val linkCollection = FeatureCollection.fromFeatures(linkFeatures)
            val fieldCollection = FeatureCollection.fromFeatures(fieldFeatures)
            val selectionCollection = FeatureCollection.fromFeatures(selectionFeatures)
            val trailCollection = FeatureCollection.fromFeatures(trailFeatures)
            val markerCollection = FeatureCollection.fromFeatures(markerFeatures)

            withContext(Dispatchers.Main) {
                style.getSourceAs<GeoJsonSource>(SOURCE_FIELDS)?.setGeoJson(fieldCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_LINKS)?.setGeoJson(linkCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_PORTALS)?.setGeoJson(portalCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_PORTAL_SELECTION)?.setGeoJson(selectionCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_PLAYER_TRAILS)?.setGeoJson(trailCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_PLAYER_MARKERS)?.setGeoJson(markerCollection)
            }
        }
    }

    // AndroidView hosting MapView
    AndroidView(
        factory = { ctx ->
            val options = MapLibreMapOptions.createFromAttributes(ctx).apply {
                localIdeographFontFamily("sans-serif")
                localIdeographFontFamilyEnabled(true)
            }
            MapView(ctx, options).apply {
                onCreate(null)
                getMapAsync { map ->
                    mapLibreMap = map
                    map.uiSettings.apply {
                        isAttributionEnabled = false
                        isLogoEnabled = false
                        isCompassEnabled = true
                        isRotateGesturesEnabled = true
                        isTiltGesturesEnabled = true
                        isZoomGesturesEnabled = true
                        isScrollGesturesEnabled = true
                    }

                    // Limit zoom range to eliminate "map data not yet available" and ensure optimal display
                    map.setMinZoomPreference(2.0)
                    map.setMaxZoomPreference(18.5)

                    // Restore saved map camera position if available
                    val savedPos = viewModel.getSavedMapPosition()
                    if (savedPos != null) {
                        val (lat, lng, zoom) = savedPos
                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(lat, lng))
                            .zoom(zoom.coerceIn(2.0, 18.5))
                            .build()
                    }

                    loadMapStyle(map, nativeBaseMap)

                    // Handle map clicks (Portals & Player Markers)
                    map.addOnMapClickListener { point ->
                        val screenPoint = map.projection.toScreenLocation(point)
                        val touchSlop = 36f
                        val touchBox = android.graphics.RectF(
                            screenPoint.x - touchSlop,
                            screenPoint.y - touchSlop,
                            screenPoint.x + touchSlop,
                            screenPoint.y + touchSlop
                        )

                        // 1. Check Agent marker click
                        val agentFeatures = map.queryRenderedFeatures(touchBox, LAYER_PLAYER_MARKERS)
                        if (agentFeatures.isNotEmpty()) {
                            val sender = agentFeatures[0].getStringProperty("sender") ?: "Agent"
                            val team = agentFeatures[0].getStringProperty("team") ?: ""
                            val portal = agentFeatures[0].getStringProperty("portal") ?: ""
                            Toast.makeText(context, "Agent: $sender\nFaction: $team\nNear: $portal", Toast.LENGTH_SHORT).show()
                            return@addOnMapClickListener true
                        }

                        // 2. Check Portal click
                        val features = map.queryRenderedFeatures(touchBox, LAYER_PORTALS)
                        if (features.isNotEmpty()) {
                            val guid = features[0].getStringProperty("guid")
                            if (!guid.isNullOrBlank()) {
                                viewModel.onPortalTapped(guid)
                                return@addOnMapClickListener true
                            }
                        }
                        false
                    }

                    // Handle map camera movement -> Sync to IITC / Leaflet
                    map.addOnCameraIdleListener {
                        val camera = map.cameraPosition
                        val target = camera.target
                        if (target != null) {
                            viewModel.onNativeMapCameraChanged(
                                lat = target.latitude,
                                lng = target.longitude,
                                zoom = camera.zoom
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )

    // Forward Android lifecycle events to MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ -> }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
