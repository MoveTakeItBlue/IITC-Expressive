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
import io.github.movetakeitblue.iitcexpressive.model.PlayerActivityPoint
import io.github.movetakeitblue.iitcexpressive.model.PlayerTrail
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import io.github.movetakeitblue.iitcexpressive.util.CoordinateTransform
import io.github.movetakeitblue.iitcexpressive.viewmodel.AppDialog
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
import io.github.movetakeitblue.iitcexpressive.plugin.api.DrawnShape
import io.github.movetakeitblue.iitcexpressive.plugin.api.ShapeLatLng

private const val SOURCE_FIELDS = "iitc-fields-source"
private const val SOURCE_LINKS = "iitc-links-source"
private const val SOURCE_PORTALS = "iitc-portals-source"
private const val SOURCE_PORTAL_SELECTION = "iitc-portal-selection-source"
private const val SOURCE_PLAYER_TRAILS = "iitc-player-trails-source"
private const val SOURCE_PLAYER_MARKERS = "iitc-player-markers-source"
private const val SOURCE_DRAW_POLYGONS = "iitc-draw-polygons-source"
private const val SOURCE_DRAW_LINES = "iitc-draw-lines-source"
private const val SOURCE_DRAW_MARKERS = "iitc-draw-markers-source"

private const val LAYER_FIELDS_FILL = "iitc-fields-fill"
private const val LAYER_FIELDS_STROKE = "iitc-fields-stroke"
private const val LAYER_LINKS = "iitc-links-layer"
private const val LAYER_DRAW_POLYGONS_FILL = "iitc-draw-polygons-fill"
private const val LAYER_DRAW_POLYGONS_STROKE = "iitc-draw-polygons-stroke"
private const val LAYER_DRAW_LINES = "iitc-draw-lines"
private const val LAYER_DRAW_MARKERS_HALO = "iitc-draw-markers-halo"
private const val LAYER_DRAW_MARKERS = "iitc-draw-markers"
private const val LAYER_PORTAL_SELECTION_HALO = "iitc-portal-selection-halo"
private const val LAYER_PORTAL_SELECTION_RING = "iitc-portal-selection-ring"
private const val LAYER_PORTALS_HALO = "iitc-portals-halo"
private const val LAYER_PORTALS = "iitc-portals-layer"
private const val LAYER_PORTALS_TITLE = "iitc-portals-title"
private const val LAYER_PLAYER_TRAILS_CASING = "iitc-player-trails-casing"
private const val LAYER_PLAYER_TRAILS = "iitc-player-trails"
private const val LAYER_PLAYER_MARKERS_HALO = "iitc-player-markers-halo"
private const val LAYER_PLAYER_MARKERS = "iitc-player-markers"

private fun resolveEffectiveMapTextColor(textColorSetting: String, isDarkMap: Boolean): String {
    return when (textColorSetting.lowercase()) {
        "auto" -> if (isDarkMap) "#ffffff" else "#000000"
        "white" -> "#ffffff"
        "black" -> "#000000"
        "amber" -> "#ffd600"
        "cyan" -> "#00e5ff"
        "green" -> "#00ff66"
        else -> {
            val trimmed = textColorSetting.trim()
            if (trimmed.startsWith("#") && (trimmed.length == 7 || trimmed.length == 9)) {
                trimmed
            } else if (trimmed.length == 6 && trimmed.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
                "#$trimmed"
            } else {
                if (isDarkMap) "#ffffff" else "#000000"
            }
        }
    }
}

private fun isHexColorDark(hexColor: String): Boolean {
    return try {
        val clean = hexColor.removePrefix("#")
        val colorInt = clean.toLong(16).toInt()
        val r = (colorInt shr 16) and 0xFF
        val g = (colorInt shr 8) and 0xFF
        val b = colorInt and 0xFF
        (r * 0.299 + g * 0.587 + b * 0.114) < 128
    } catch (e: Exception) {
        false
    }
}

private data class SelectedPortalInfo(
    val lat: Double,
    val lng: Double,
    val level: Int,
    val team: Int
)

/**
 * Creates an M3E tactical speech bubble bitmap popping out to the right of the portal.
 * - Arrow/pointer on the left pointing towards the portal icon
 * - Dark faction container background fill
 * - Crisp pure white agent codename text inside
 * - Subtle faction accent outline and drop shadow
 */
private fun createAgentSpeechBubbleBitmap(
    sender: String,
    team: String,
    fontSizeSp: Float,
    density: Float,
    densityDpi: Int,
    isBold: Boolean = true
): android.graphics.Bitmap {
    val teamUpper = team.uppercase()
    val (bgColor, strokeColor) = when {
        teamUpper.contains("ENL") || teamUpper == "2" || teamUpper == "E" -> Pair("#00522B", "#4EDE92") // Dark emerald, mint accent
        teamUpper.contains("RES") || teamUpper == "1" || teamUpper == "R" -> Pair("#004785", "#6BB5FF") // Dark sapphire, blue accent
        teamUpper.contains("MAC") || teamUpper == "3" || teamUpper == "M" -> Pair("#680009", "#FF7171") // Dark crimson, red accent
        else -> Pair("#282B2D", "#C0C7C7") // Dark charcoal, gray accent
    }

    val bgParsed = try {
        android.graphics.Color.parseColor(bgColor)
    } catch (_: Exception) {
        android.graphics.Color.parseColor("#004785")
    }
    val strokeParsed = try {
        android.graphics.Color.parseColor(strokeColor)
    } catch (_: Exception) {
        android.graphics.Color.WHITE
    }

    val displayName = sender.ifBlank { "Agent" }

    val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = fontSizeSp * density
        typeface = if (isBold) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
        textAlign = android.graphics.Paint.Align.LEFT
        isDither = true
    }

    val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
        color = bgParsed
        isDither = true
    }

    val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        color = android.graphics.Color.argb(
            200,
            android.graphics.Color.red(strokeParsed),
            android.graphics.Color.green(strokeParsed),
            android.graphics.Color.blue(strokeParsed)
        )
        strokeWidth = 1.2f * density
        strokeJoin = android.graphics.Paint.Join.ROUND
        strokeCap = android.graphics.Paint.Cap.ROUND
        isDither = true
    }

    val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
        color = android.graphics.Color.argb(85, 0, 0, 0)
        isDither = true
    }

    val textWidth = textPaint.measureText(displayName)
    val fontMetrics = textPaint.fontMetrics
    val textHeight = fontMetrics.descent - fontMetrics.ascent

    val paddingHoriz = 7.5f * density
    val paddingVert = 3.5f * density
    val cornerRadius = 6f * density

    val pointerW = 6f * density // Horizontal length of triangle pointer pointing left
    val pointerH = 8f * density // Base height of triangle pointer

    val shadowPadX = 3f * density
    val shadowPadY = 3f * density
    val shadowOffsetY = 1.5f * density

    val bodyW = textWidth + paddingHoriz * 2f
    val bodyH = textHeight + paddingVert * 2f

    val totalW = (shadowPadX + pointerW + bodyW + shadowPadX).toInt().coerceAtLeast(1)
    val totalH = (shadowPadY + bodyH + shadowPadY + shadowOffsetY).toInt().coerceAtLeast(1)

    val left = shadowPadX + pointerW
    val top = shadowPadY
    val right = left + bodyW
    val bottom = top + bodyH
    val centerY = (top + bottom) / 2f
    val tipX = shadowPadX
    val pointerHalfH = pointerH / 2f

    fun buildBubblePath(offsetY: Float = 0f): android.graphics.Path {
        val p = android.graphics.Path()
        val t = top + offsetY
        val b = bottom + offsetY
        val cy = centerY + offsetY
        val r = cornerRadius

        // Arrow top base on body left edge
        p.moveTo(left, cy - pointerHalfH)
        // Arrow tip pointing left towards portal
        p.lineTo(tipX, cy)
        // Arrow bottom base
        p.lineTo(left, cy + pointerHalfH)
        // Down body left side
        p.lineTo(left, b - r)
        // Bottom-left rounded arc
        p.quadTo(left, b, left + r, b)
        // Bottom edge
        p.lineTo(right - r, b)
        // Bottom-right rounded arc
        p.quadTo(right, b, right, b - r)
        // Right edge
        p.lineTo(right, t + r)
        // Top-right rounded arc
        p.quadTo(right, t, right - r, t)
        // Top edge
        p.lineTo(left + r, t)
        // Top-left rounded arc
        p.quadTo(left, t, left, t + r)
        // Close back to arrow top base
        p.close()
        return p
    }

    val bitmap = android.graphics.Bitmap.createBitmap(totalW, totalH, android.graphics.Bitmap.Config.ARGB_8888)
    bitmap.density = densityDpi
    val canvas = android.graphics.Canvas(bitmap)

    // 1. Soft Drop Shadow
    val shadowPath = buildBubblePath(offsetY = shadowOffsetY)
    canvas.drawPath(shadowPath, shadowPaint)

    // 2. Dark Faction Fill
    val bubblePath = buildBubblePath(offsetY = 0f)
    canvas.drawPath(bubblePath, fillPaint)

    // 3. Subtle Faction Accent Stroke
    canvas.drawPath(bubblePath, strokePaint)

    // 4. Pure White Agent Codename
    val textBaselineY = centerY - (fontMetrics.descent + fontMetrics.ascent) / 2f
    canvas.drawText(displayName, left + paddingHoriz, textBaselineY, textPaint)

    return bitmap
}

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

    NativeBaseMap.GOOGLE_ROAD -> """
    {
      "version": 8,
      "name": "GoogleRoadmap",
      "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
      "sources": {
        "google-road": {
          "type": "raster",
          "tiles": [
            "https://mt0.google.com/vt/lyrs=m&x={x}&y={y}&z={z}",
            "https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}",
            "https://mt2.google.com/vt/lyrs=m&x={x}&y={y}&z={z}",
            "https://mt3.google.com/vt/lyrs=m&x={x}&y={y}&z={z}"
          ],
          "tileSize": 256,
          "maxzoom": 20,
          "attribution": "&copy; Google Maps"
        }
      },
      "layers": [
        {
          "id": "google-road-layer",
          "type": "raster",
          "source": "google-road",
          "minzoom": 0,
          "maxzoom": 22
        }
      ]
    }
    """.trimIndent()

    NativeBaseMap.GOOGLE_HYBRID -> """
    {
      "version": 8,
      "name": "GoogleHybrid",
      "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
      "sources": {
        "google-hybrid": {
          "type": "raster",
          "tiles": [
            "https://mt0.google.com/vt/lyrs=y&x={x}&y={y}&z={z}",
            "https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}",
            "https://mt2.google.com/vt/lyrs=y&x={x}&y={y}&z={z}",
            "https://mt3.google.com/vt/lyrs=y&x={x}&y={y}&z={z}"
          ],
          "tileSize": 256,
          "maxzoom": 20,
          "attribution": "&copy; Google Maps"
        }
      },
      "layers": [
        {
          "id": "google-hybrid-layer",
          "type": "raster",
          "source": "google-hybrid",
          "minzoom": 0,
          "maxzoom": 22
        }
      ]
    }
    """.trimIndent()

    NativeBaseMap.GOOGLE_TERRAIN -> """
    {
      "version": 8,
      "name": "GoogleTerrain",
      "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
      "sources": {
        "google-terrain": {
          "type": "raster",
          "tiles": [
            "https://mt0.google.com/vt/lyrs=p&x={x}&y={y}&z={z}",
            "https://mt1.google.com/vt/lyrs=p&x={x}&y={y}&z={z}",
            "https://mt2.google.com/vt/lyrs=p&x={x}&y={y}&z={z}",
            "https://mt3.google.com/vt/lyrs=p&x={x}&y={y}&z={z}"
          ],
          "tileSize": 256,
          "maxzoom": 20,
          "attribution": "&copy; Google Maps"
        }
      },
      "layers": [
        {
          "id": "google-terrain-layer",
          "type": "raster",
          "source": "google-terrain",
          "minzoom": 0,
          "maxzoom": 22
        }
      ]
    }
    """.trimIndent()

    NativeBaseMap.GAODE_VECTOR -> """
    {
      "version": 8,
      "name": "AutoNaviVector",
      "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
      "sources": {
        "gaode-vector": {
          "type": "raster",
          "tiles": [
            "https://wprd01.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=7",
            "https://wprd02.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=7",
            "https://wprd03.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=7",
            "https://wprd04.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=7"
          ],
          "tileSize": 256,
          "maxzoom": 19,
          "attribution": "&copy; AutoNavi (高德地图)"
        }
      },
      "layers": [
        {
          "id": "gaode-vector-layer",
          "type": "raster",
          "source": "gaode-vector",
          "minzoom": 0,
          "maxzoom": 22
        }
      ]
    }
    """.trimIndent()

    NativeBaseMap.GAODE_SATELLITE -> """
    {
      "version": 8,
      "name": "AutoNaviSatellite",
      "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
      "sources": {
        "gaode-sat-base": {
          "type": "raster",
          "tiles": [
            "https://wprd01.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=6",
            "https://wprd02.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=6",
            "https://wprd03.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=6",
            "https://wprd04.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=6"
          ],
          "tileSize": 256,
          "maxzoom": 19,
          "attribution": "&copy; AutoNavi (高德地图)"
        },
        "gaode-sat-overlay": {
          "type": "raster",
          "tiles": [
            "https://wprd01.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=8",
            "https://wprd02.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=8",
            "https://wprd03.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=8",
            "https://wprd04.is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&lang=zh_cn&size=1&scale=1&style=8"
          ],
          "tileSize": 256,
          "maxzoom": 19
        }
      },
      "layers": [
        {
          "id": "gaode-sat-base-layer",
          "type": "raster",
          "source": "gaode-sat-base",
          "minzoom": 0,
          "maxzoom": 22
        },
        {
          "id": "gaode-sat-overlay-layer",
          "type": "raster",
          "source": "gaode-sat-overlay",
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
    val bubbleBitmapCache = remember { mutableMapOf<String, android.graphics.Bitmap>() }

    val entities by viewModel.nativeMapEntities.collectAsState()
    val isNativeMapEnabled by viewModel.isNativeMapEnabled.collectAsState()
    val nativeBaseMap by viewModel.nativeBaseMap.collectAsState()
    val selectedHighlighter by viewModel.selectedHighlighter.collectAsState()
    val isPlayerTrackerEnabled by viewModel.isPlayerTrackerEnabled.collectAsState()
    val playerTrails by viewModel.playerTrails.collectAsState()
    val portalStatus by viewModel.portalStatus.collectAsState()
    val portalNameFontSize by viewModel.portalNameFontSize.collectAsState()
    val agentNameFontSize by viewModel.agentNameFontSize.collectAsState()
    val mapFontFamily by viewModel.mapFontFamily.collectAsState()
    val mapFontBold by viewModel.mapFontBold.collectAsState()
    val mapTextHalo by viewModel.mapTextHalo.collectAsState()
    val playerIconScale by viewModel.playerIconScale.collectAsState()
    val mapTextColor by viewModel.mapTextColor.collectAsState()
    val drawnShapes by viewModel.drawnShapes.collectAsState()
    val isDrawingModeActive by viewModel.isDrawingModeActive.collectAsState()
    val currentDrawingType by viewModel.currentDrawingType.collectAsState()
    val currentDrawingColor by viewModel.currentDrawingColor.collectAsState()
    val currentDrawingPoints by viewModel.currentDrawingPoints.collectAsState()

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
        styleBuilder.withSource(GeoJsonSource(SOURCE_DRAW_POLYGONS))
        styleBuilder.withSource(GeoJsonSource(SOURCE_DRAW_LINES))
        styleBuilder.withSource(GeoJsonSource(SOURCE_DRAW_MARKERS))

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

        // Draw Tools - Polygon fill
        val drawPolygonFill = FillLayer(LAYER_DRAW_POLYGONS_FILL, SOURCE_DRAW_POLYGONS).apply {
            setProperties(
                fillColor(get("color")),
                fillOpacity(0.28f),
                fillAntialias(true)
            )
        }
        styleBuilder.withLayer(drawPolygonFill)

        // Draw Tools - Polygon stroke
        val drawPolygonStroke = LineLayer(LAYER_DRAW_POLYGONS_STROKE, SOURCE_DRAW_POLYGONS).apply {
            setProperties(
                lineColor(get("color")),
                lineWidth(2.5f),
                lineOpacity(0.9f)
            )
        }
        styleBuilder.withLayer(drawPolygonStroke)

        // Draw Tools - Polyline layer
        val drawLinesLayer = LineLayer(LAYER_DRAW_LINES, SOURCE_DRAW_LINES).apply {
            setProperties(
                lineColor(get("color")),
                lineWidth(3.0f),
                lineOpacity(0.95f),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND)
            )
        }
        styleBuilder.withLayer(drawLinesLayer)

        // Draw Tools - Markers halo & circle
        val drawMarkersHalo = CircleLayer(LAYER_DRAW_MARKERS_HALO, SOURCE_DRAW_MARKERS).apply {
            setProperties(
                circleRadius(8.0f),
                circleColor("#000000"),
                circleOpacity(0.5f)
            )
        }
        styleBuilder.withLayer(drawMarkersHalo)

        val drawMarkersLayer = CircleLayer(LAYER_DRAW_MARKERS, SOURCE_DRAW_MARKERS).apply {
            setProperties(
                circleRadius(6.0f),
                circleColor(get("color")),
                circleStrokeWidth(2.0f),
                circleStrokeColor("#ffffff")
            )
        }
        styleBuilder.withLayer(drawMarkersLayer)

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

        val fontStack = if (mapFontBold) arrayOf("Noto Sans Bold") else arrayOf("Noto Sans Regular")
        val effectiveTextColor = resolveEffectiveMapTextColor(mapTextColor, baseMap.isDark)
        val isTextDark = isHexColorDark(effectiveTextColor)
        val haloWidth = if (mapTextHalo) 1.2f else 0f
        val haloColor = if (mapTextHalo) (if (isTextDark) "#ffffff" else "#111415") else "rgba(0,0,0,0)"

        // Pre-register any active Player Speech Bubble Bitmaps
        val density = context.resources.displayMetrics.density
        val densityDpi = context.resources.displayMetrics.densityDpi
        for (trail in viewModel.playerTrails.value) {
            val bubbleKey = "bubble_${trail.sender}_${trail.team}_${agentNameFontSize.toInt()}_${if (mapFontBold) "1" else "0"}"
            val bmp = bubbleBitmapCache.getOrPut(bubbleKey) {
                createAgentSpeechBubbleBitmap(
                    sender = trail.sender,
                    team = trail.team,
                    fontSizeSp = agentNameFontSize,
                    density = density,
                    densityDpi = densityDpi,
                    isBold = mapFontBold
                )
            }
            styleBuilder.withImage(bubbleKey, bmp)
        }

        // 11. Player Activity Tracker - Agent speech bubble marker (pops out right from portal icon)
        val playerMarkersLayer = SymbolLayer(LAYER_PLAYER_MARKERS, SOURCE_PLAYER_MARKERS).apply {
            setProperties(
                iconImage(get("bubbleIcon")),
                iconSize(playerIconScale / 1.5f),
                iconAnchor(Property.ICON_ANCHOR_LEFT),
                iconOffset(arrayOf(10f, 0f)),
                iconAllowOverlap(true),
                iconIgnorePlacement(false),
                iconOptional(false)
            )
        }
        styleBuilder.withLayer(playerMarkersLayer)

        // 12. Portals title symbol layer (theme contrast or custom color, on the right of portal)
        val portalTitlesLayer = SymbolLayer(LAYER_PORTALS_TITLE, SOURCE_PORTALS).apply {
            setProperties(
                textField(get("title")),
                textSize(portalNameFontSize),
                textColor(effectiveTextColor),
                textHaloColor(haloColor),
                textHaloWidth(haloWidth),
                textHaloBlur(0.2f),
                textAnchor(Property.TEXT_ANCHOR_LEFT),
                textOffset(arrayOf(1.1f, 0f)),
                textAllowOverlap(false),
                textIgnorePlacement(false),
                textFont(fontStack)
            )
            minZoom = 15.5f
            maxZoom = 22f
        }
        styleBuilder.withLayer(portalTitlesLayer)

        map.setStyle(styleBuilder) { loadedStyle ->
            mapStyle = loadedStyle
        }
    }

    // Switch BaseMap when nativeBaseMap changes (with seamless coordinate system recalibration)
    var lastWasGcj02 by remember { mutableStateOf(nativeBaseMap.isGcj02) }
    LaunchedEffect(nativeBaseMap, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val currTarget = map.cameraPosition.target
        if (currTarget != null && lastWasGcj02 != nativeBaseMap.isGcj02) {
            val (newLat, newLng) = if (nativeBaseMap.isGcj02) {
                // Switching from standard WGS-84 to AutoNavi GCJ-02
                CoordinateTransform.wgs84ToGcj02(currTarget.latitude, currTarget.longitude)
            } else {
                // Switching from AutoNavi GCJ-02 back to standard WGS-84
                CoordinateTransform.gcj02ToWgs84(currTarget.latitude, currTarget.longitude)
            }
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(newLat, newLng))
                .zoom(map.cameraPosition.zoom)
                .tilt(map.cameraPosition.tilt)
                .bearing(map.cameraPosition.bearing)
                .build()
            lastWasGcj02 = nativeBaseMap.isGcj02
        }
        loadMapStyle(map, nativeBaseMap)
    }

    // Listen for smooth Camera Move events (e.g. from Linked Portals jump)
    LaunchedEffect(mapLibreMap, nativeBaseMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        viewModel.cameraMoveEvent.collect { (lat, lng, zoom) ->
            val (targetLat, targetLng) = if (nativeBaseMap.isGcj02) {
                CoordinateTransform.wgs84ToGcj02(lat, lng)
            } else {
                Pair(lat, lng)
            }
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(targetLat, targetLng),
                    zoom
                ),
                800
            )
        }
    }

    // Update GeoJSON layers when entities, highlighters, or player activity changes
    LaunchedEffect(entities, selectedHighlighter, isPlayerTrackerEnabled, playerTrails, portalStatus, mapStyle, nativeBaseMap, mapTextColor, drawnShapes, isDrawingModeActive, currentDrawingPoints, currentDrawingType, currentDrawingColor, agentNameFontSize, mapFontBold) {
        val style = mapStyle ?: return@LaunchedEffect
        withContext(Dispatchers.Default) {
            val isGcj02 = nativeBaseMap.isGcj02
            val effectiveTextColor = resolveEffectiveMapTextColor(mapTextColor, nativeBaseMap.isDark)
            val levelColors = listOf(
                "#c0c7c7", "#fece5a", "#ffa630", "#ff7315", "#e40000",
                "#fd2992", "#eb26cd", "#c124e0", "#9627f4"
            )

            // Build Portal features with dynamic M3E Highlighter styles
            val portalFeatures = ArrayList<Feature>(entities.portals.size)
            for (p in entities.portals) {
                val (pLat, pLng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(p.lat, p.lng) else Pair(p.lat, p.lng)
                val point = Point.fromLngLat(pLng, pLat)
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
                    else -> Pair("#c0c7c7", "#3c4043") // Neutral / Unclaimed: Gray
                }
                feat.addStringProperty("titleColor", effectiveTextColor)

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
                var haloColor = "#000000"
                var haloRadius = 0f
                var haloOpacity = 0f

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
                            fillColor = "#e0e3e3"
                            strokeColor = "#ffffff"
                            strokeWidth = 3f
                            radius = radius + 3.5f
                            haloColor = "#ffffff"
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
                val (oLat, oLng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(l.oLat, l.oLng) else Pair(l.oLat, l.oLng)
                val (dLat, dLng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(l.dLat, l.dLng) else Pair(l.dLat, l.dLng)
                val line = LineString.fromLngLats(
                    listOf(
                        Point.fromLngLat(oLng, oLat),
                        Point.fromLngLat(dLng, dLat)
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
                    val ring = f.points.map { pt ->
                        val (pLat, pLng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(pt[0], pt[1]) else Pair(pt[0], pt[1])
                        Point.fromLngLat(pLng, pLat)
                    }.toMutableList()
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
            val bubbleBitmapsToAdd = ArrayList<Pair<String, android.graphics.Bitmap>>()

            if (isPlayerTrackerEnabled && playerTrails.isNotEmpty()) {
                val density = context.resources.displayMetrics.density
                val densityDpi = context.resources.displayMetrics.densityDpi

                for (trail in playerTrails) {
                    val teamUpper = trail.team.uppercase()
                    val color = when {
                        teamUpper.contains("ENL") || teamUpper == "2" || teamUpper == "E" -> "#66dc96"
                        teamUpper.contains("RES") || teamUpper == "1" || teamUpper == "R" -> "#70d2ff"
                        teamUpper.contains("MAC") || teamUpper == "3" || teamUpper == "M" -> "#ff7164"
                        else -> "#c0c7c7"
                    }

                    if (trail.points.size >= 2) {
                        val coords = trail.points.map { pt ->
                            val (tLat, tLng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(pt.lat, pt.lng) else Pair(pt.lat, pt.lng)
                            Point.fromLngLat(tLng, tLat)
                        }
                        val line = LineString.fromLngLats(coords)
                        val lineFeat = Feature.fromGeometry(line)
                        lineFeat.addStringProperty("sender", trail.sender)
                        lineFeat.addStringProperty("team", trail.team)
                        lineFeat.addStringProperty("color", color)
                        trailFeatures.add(lineFeat)
                    }

                    val (mLat, mLng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(trail.latestLat, trail.latestLng) else Pair(trail.latestLat, trail.latestLng)
                    val point = Point.fromLngLat(mLng, mLat)
                    val ptFeat = Feature.fromGeometry(point)
                    ptFeat.addStringProperty("sender", trail.sender)
                    ptFeat.addStringProperty("team", trail.team)
                    ptFeat.addStringProperty("color", color)
                    ptFeat.addStringProperty("portal", trail.latestPortalName)

                    val bubbleKey = "bubble_${trail.sender}_${trail.team}_${agentNameFontSize.toInt()}_${if (mapFontBold) "1" else "0"}"
                    ptFeat.addStringProperty("bubbleIcon", bubbleKey)
                    markerFeatures.add(ptFeat)

                    val bmp = bubbleBitmapCache.getOrPut(bubbleKey) {
                        createAgentSpeechBubbleBitmap(
                            sender = trail.sender,
                            team = trail.team,
                            fontSizeSp = agentNameFontSize,
                            density = density,
                            densityDpi = densityDpi,
                            isBold = mapFontBold
                        )
                    }
                    bubbleBitmapsToAdd.add(Pair(bubbleKey, bmp))
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
                    val (sLat, sLng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(selInfo.lat, selInfo.lng) else Pair(selInfo.lat, selInfo.lng)
                    val selPoint = Point.fromLngLat(sLng, sLat)
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
                        else -> "#c0c7c7"
                    }
                    selFeat.addNumberProperty("ringRadius", baseRadius + 4.5f)
                    selFeat.addStringProperty("strokeColor", "#ffffff")
                    selFeat.addNumberProperty("haloRadius", baseRadius + 11f)
                    selFeat.addStringProperty("haloColor", haloCol)
                    selectionFeatures.add(selFeat)
                }
            }

            // Build Draw Tools features (Lines, Polygons, Markers)
            val drawPolygonFeatures = ArrayList<Feature>()
            val drawLineFeatures = ArrayList<Feature>()
            val drawMarkerFeatures = ArrayList<Feature>()

            // 1. Saved shapes
            for (shape in drawnShapes) {
                when (shape) {
                    is DrawnShape.Polyline -> {
                        if (shape.latLngs.size >= 2) {
                            val pts = shape.latLngs.map { pt ->
                                val (lat, lng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(pt.lat, pt.lng) else Pair(pt.lat, pt.lng)
                                Point.fromLngLat(lng, lat)
                            }
                            val line = LineString.fromLngLats(pts)
                            val feat = Feature.fromGeometry(line)
                            feat.addStringProperty("color", shape.color)
                            drawLineFeatures.add(feat)
                        }
                    }
                    is DrawnShape.Polygon -> {
                        if (shape.latLngs.size >= 3) {
                            val pts = shape.latLngs.map { pt ->
                                val (lat, lng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(pt.lat, pt.lng) else Pair(pt.lat, pt.lng)
                                Point.fromLngLat(lng, lat)
                            }
                            val closedPts = if (pts.first() != pts.last()) pts + pts.first() else pts
                            val poly = Polygon.fromLngLats(listOf(closedPts))
                            val feat = Feature.fromGeometry(poly)
                            feat.addStringProperty("color", shape.color)
                            drawPolygonFeatures.add(feat)
                        }
                    }
                    is DrawnShape.Circle -> {
                        val (lat, lng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(shape.center.lat, shape.center.lng) else Pair(shape.center.lat, shape.center.lng)
                        val feat = Feature.fromGeometry(Point.fromLngLat(lng, lat))
                        feat.addStringProperty("color", shape.color)
                        drawMarkerFeatures.add(feat)
                    }
                    is DrawnShape.Marker -> {
                        val (lat, lng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(shape.latLng.lat, shape.latLng.lng) else Pair(shape.latLng.lat, shape.latLng.lng)
                        val feat = Feature.fromGeometry(Point.fromLngLat(lng, lat))
                        feat.addStringProperty("color", shape.color)
                        drawMarkerFeatures.add(feat)
                    }
                }
            }

            // 2. In-progress drawing preview
            if (isDrawingModeActive && currentDrawingPoints.isNotEmpty()) {
                val previewPts = currentDrawingPoints.map { pt ->
                    val (lat, lng) = if (isGcj02) CoordinateTransform.wgs84ToGcj02(pt.lat, pt.lng) else Pair(pt.lat, pt.lng)
                    Point.fromLngLat(lng, lat)
                }

                // Vertex markers
                for (pt in previewPts) {
                    val feat = Feature.fromGeometry(pt)
                    feat.addStringProperty("color", currentDrawingColor)
                    drawMarkerFeatures.add(feat)
                }

                if (previewPts.size >= 2) {
                    val line = LineString.fromLngLats(previewPts)
                    val feat = Feature.fromGeometry(line)
                    feat.addStringProperty("color", currentDrawingColor)
                    drawLineFeatures.add(feat)
                }

                if (currentDrawingType == "polygon" && previewPts.size >= 3) {
                    val closed = previewPts + previewPts.first()
                    val poly = Polygon.fromLngLats(listOf(closed))
                    val feat = Feature.fromGeometry(poly)
                    feat.addStringProperty("color", currentDrawingColor)
                    drawPolygonFeatures.add(feat)
                }
            }

            val portalCollection = FeatureCollection.fromFeatures(portalFeatures)
            val linkCollection = FeatureCollection.fromFeatures(linkFeatures)
            val fieldCollection = FeatureCollection.fromFeatures(fieldFeatures)
            val selectionCollection = FeatureCollection.fromFeatures(selectionFeatures)
            val trailCollection = FeatureCollection.fromFeatures(trailFeatures)
            val markerCollection = FeatureCollection.fromFeatures(markerFeatures)
            val drawPolygonCollection = FeatureCollection.fromFeatures(drawPolygonFeatures)
            val drawLineCollection = FeatureCollection.fromFeatures(drawLineFeatures)
            val drawMarkerCollection = FeatureCollection.fromFeatures(drawMarkerFeatures)

            withContext(Dispatchers.Main) {
                for ((key, bmp) in bubbleBitmapsToAdd) {
                    if (style.getImage(key) == null) {
                        style.addImage(key, bmp)
                    }
                }
                style.getSourceAs<GeoJsonSource>(SOURCE_FIELDS)?.setGeoJson(fieldCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_LINKS)?.setGeoJson(linkCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_PORTALS)?.setGeoJson(portalCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_PORTAL_SELECTION)?.setGeoJson(selectionCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_PLAYER_TRAILS)?.setGeoJson(trailCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_PLAYER_MARKERS)?.setGeoJson(markerCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_DRAW_POLYGONS)?.setGeoJson(drawPolygonCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_DRAW_LINES)?.setGeoJson(drawLineCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_DRAW_MARKERS)?.setGeoJson(drawMarkerCollection)
            }
        }
    }

    // Dynamic reactive updates for typography & icon settings (font size, bold, halo, player icon scale, text color)
    LaunchedEffect(portalNameFontSize, agentNameFontSize, mapFontBold, mapTextHalo, playerIconScale, mapStyle, mapTextColor, nativeBaseMap) {
        val style = mapStyle ?: return@LaunchedEffect
        val fontStack = if (mapFontBold) arrayOf("Noto Sans Bold") else arrayOf("Noto Sans Regular")
        val effectiveTextColor = resolveEffectiveMapTextColor(mapTextColor, nativeBaseMap.isDark)
        val isTextDark = isHexColorDark(effectiveTextColor)
        val haloW = if (mapTextHalo) 1.2f else 0f
        val haloC = if (mapTextHalo) (if (isTextDark) "#ffffff" else "#111415") else "rgba(0,0,0,0)"

        style.getLayerAs<SymbolLayer>(LAYER_PORTALS_TITLE)?.setProperties(
            textSize(portalNameFontSize),
            textColor(effectiveTextColor),
            textFont(fontStack),
            textHaloWidth(haloW),
            textHaloColor(haloC),
            textAnchor(Property.TEXT_ANCHOR_LEFT),
            textOffset(arrayOf(1.1f, 0f))
        )
        style.getLayerAs<SymbolLayer>(LAYER_PLAYER_MARKERS)?.setProperties(
            iconSize(playerIconScale / 1.5f),
            iconOffset(arrayOf(10f, 0f))
        )
    }

    // AndroidView hosting MapView
    AndroidView(
        factory = { ctx ->
            val options = MapLibreMapOptions.createFromAttributes(ctx).apply {
                localIdeographFontFamily(mapFontFamily)
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
                        val (camLat, camLng) = if (nativeBaseMap.isGcj02) {
                            CoordinateTransform.wgs84ToGcj02(lat, lng)
                        } else {
                            Pair(lat, lng)
                        }
                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(camLat, camLng))
                            .zoom(zoom.coerceIn(2.0, 18.5))
                            .build()
                    }

                    loadMapStyle(map, nativeBaseMap)

                    // Handle map clicks (Portals & Player Markers)
                    map.addOnMapClickListener { point ->
                        val screenPoint = map.projection.toScreenLocation(point)
                        val touchSlop = 32f
                        val touchBox = android.graphics.RectF(
                            screenPoint.x - touchSlop,
                            screenPoint.y - touchSlop,
                            screenPoint.x + touchSlop,
                            screenPoint.y + touchSlop
                        )

                        // If drawing mode is active in Draw Tools, intercept tap to add points!
                        if (viewModel.isDrawingModeActive.value) {
                            val portalFeatures = map.queryRenderedFeatures(touchBox, LAYER_PORTALS)
                            if (portalFeatures.isNotEmpty()) {
                                val pGeom = portalFeatures[0].geometry() as? Point
                                if (pGeom != null) {
                                    val (pWgsLat, pWgsLng) = if (nativeBaseMap.isGcj02) {
                                        CoordinateTransform.gcj02ToWgs84(pGeom.latitude(), pGeom.longitude())
                                    } else {
                                        Pair(pGeom.latitude(), pGeom.longitude())
                                    }
                                    viewModel.addDrawingPoint(pWgsLat, pWgsLng)
                                    return@addOnMapClickListener true
                                }
                            }
                            val (wgsLat, wgsLng) = if (nativeBaseMap.isGcj02) {
                                CoordinateTransform.gcj02ToWgs84(point.latitude, point.longitude)
                            } else {
                                Pair(point.latitude, point.longitude)
                            }
                            viewModel.addDrawingPoint(wgsLat, wgsLng)
                            return@addOnMapClickListener true
                        }

                        // 1. Query Portals near the tap point
                        val portalFeatures = map.queryRenderedFeatures(touchBox, LAYER_PORTALS)

                        // 2. Query Player Speech Bubble Markers near the tap point
                        val playerMarkerFeatures = map.queryRenderedFeatures(touchBox, LAYER_PLAYER_MARKERS)

                        // Case A: A portal is within touch range
                        if (portalFeatures.isNotEmpty()) {
                            // If player marker is also nearby, check if the tap was specifically to the right of the portal
                            // on the speech bubble (which pops out to the right of the portal icon)
                            var clickedPlayerPin = false
                            if (playerMarkerFeatures.isNotEmpty()) {
                                val pGeom = portalFeatures[0].geometry() as? Point
                                if (pGeom != null) {
                                    val portalScreen = map.projection.toScreenLocation(LatLng(pGeom.latitude(), pGeom.longitude()))
                                    if (screenPoint.x > portalScreen.x + 6f) {
                                        clickedPlayerPin = true
                                    }
                                }
                            }

                            if (clickedPlayerPin) {
                                val sender = playerMarkerFeatures[0].getStringProperty("sender") ?: "Agent"
                                val team = playerMarkerFeatures[0].getStringProperty("team") ?: ""
                                val portal = playerMarkerFeatures[0].getStringProperty("portal") ?: ""
                                val (wgsLat, wgsLng) = if (nativeBaseMap.isGcj02) {
                                    CoordinateTransform.gcj02ToWgs84(point.latitude, point.longitude)
                                } else {
                                    Pair(point.latitude, point.longitude)
                                }
                                val trail = viewModel.playerTrails.value.find { it.sender == sender }
                                    ?: playerTrails.find { it.sender == sender }
                                    ?: PlayerTrail(
                                        sender = sender,
                                        team = team,
                                        points = listOf(
                                            PlayerActivityPoint(
                                                lat = wgsLat,
                                                lng = wgsLng,
                                                portalName = portal,
                                                time = PlatformUtils.currentTimeMillis(),
                                                formattedTime = ""
                                            )
                                        ),
                                        lastSeenTime = PlatformUtils.currentTimeMillis(),
                                        latestLat = wgsLat,
                                        latestLng = wgsLng,
                                        latestPortalName = portal
                                    )
                                viewModel.setDialog(AppDialog.PlayerTrailInfo(trail))
                                return@addOnMapClickListener true
                            }

                            // Otherwise, the user tapped on/near the portal -> select the portal!
                            val guid = portalFeatures[0].getStringProperty("guid")
                            if (!guid.isNullOrBlank()) {
                                viewModel.onPortalTapped(guid)
                                return@addOnMapClickListener true
                            }
                        }

                        // Case B: No portal was tapped -> check Player Marker pin
                        if (playerMarkerFeatures.isNotEmpty()) {
                            val sender = playerMarkerFeatures[0].getStringProperty("sender") ?: "Agent"
                            val team = playerMarkerFeatures[0].getStringProperty("team") ?: ""
                            val portal = playerMarkerFeatures[0].getStringProperty("portal") ?: ""
                            val (wgsLat, wgsLng) = if (nativeBaseMap.isGcj02) {
                                CoordinateTransform.gcj02ToWgs84(point.latitude, point.longitude)
                            } else {
                                Pair(point.latitude, point.longitude)
                            }
                            val trail = viewModel.playerTrails.value.find { it.sender == sender }
                                ?: playerTrails.find { it.sender == sender }
                                ?: PlayerTrail(
                                    sender = sender,
                                    team = team,
                                    points = listOf(
                                        PlayerActivityPoint(
                                            lat = wgsLat,
                                            lng = wgsLng,
                                            portalName = portal,
                                            time = PlatformUtils.currentTimeMillis(),
                                            formattedTime = ""
                                        )
                                    ),
                                    lastSeenTime = PlatformUtils.currentTimeMillis(),
                                    latestLat = wgsLat,
                                    latestLng = wgsLng,
                                    latestPortalName = portal
                                )
                            viewModel.setDialog(AppDialog.PlayerTrailInfo(trail))
                            return@addOnMapClickListener true
                        }

                        // Case C: Check Player Trail line (only when not clicking a portal)
                        val trailFeatures = map.queryRenderedFeatures(touchBox, LAYER_PLAYER_TRAILS)
                        if (trailFeatures.isNotEmpty()) {
                            val sender = trailFeatures[0].getStringProperty("sender") ?: "Agent"
                            val team = trailFeatures[0].getStringProperty("team") ?: ""
                            val trail = viewModel.playerTrails.value.find { it.sender == sender }
                                ?: playerTrails.find { it.sender == sender }
                            if (trail != null) {
                                viewModel.setDialog(AppDialog.PlayerTrailInfo(trail))
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
                            val (wgsLat, wgsLng) = if (nativeBaseMap.isGcj02) {
                                CoordinateTransform.gcj02ToWgs84(target.latitude, target.longitude)
                            } else {
                                Pair(target.latitude, target.longitude)
                            }
                            viewModel.onNativeMapCameraChanged(
                                lat = wgsLat,
                                lng = wgsLng,
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
