package io.github.movetakeitblue.iitcexpressive.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.movetakeitblue.iitcexpressive.model.NativeMapEntities
import io.github.movetakeitblue.iitcexpressive.viewmodel.MainViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Polygon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val SOURCE_PORTALS = "iitc-portals-source"
private const val SOURCE_LINKS = "iitc-links-source"
private const val SOURCE_FIELDS = "iitc-fields-source"

private const val LAYER_FIELDS_FILL = "iitc-fields-fill"
private const val LAYER_FIELDS_STROKE = "iitc-fields-stroke"
private const val LAYER_LINKS = "iitc-links-layer"
private const val LAYER_PORTALS = "iitc-portals-layer"

// Completely free, public dark base map without requiring any API key or watermarks (Esri World Dark Gray Canvas + Reference)
private const val NO_KEY_DARK_STYLE_JSON = """
{
  "version": 8,
  "name": "DarkGrayCanvas",
  "sources": {
    "esri-dark-base": {
      "type": "raster",
      "tiles": [
        "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/{z}/{y}/{x}"
      ],
      "tileSize": 256,
      "attribution": "&copy; Esri, HERE, Garmin, &copy; OpenStreetMap contributors"
    },
    "esri-dark-ref": {
      "type": "raster",
      "tiles": [
        "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Reference/MapServer/tile/{z}/{y}/{x}"
      ],
      "tileSize": 256
    }
  },
  "layers": [
    {
      "id": "esri-dark-base-layer",
      "type": "raster",
      "source": "esri-dark-base",
      "minzoom": 0,
      "maxzoom": 19
    },
    {
      "id": "esri-dark-ref-layer",
      "type": "raster",
      "source": "esri-dark-ref",
      "minzoom": 0,
      "maxzoom": 19
    }
  ]
}
"""

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

    if (!isNativeMapEnabled) {
        return
    }

    // Update GeoJSON layers when entities change
    LaunchedEffect(entities, mapStyle) {
        val style = mapStyle ?: return@LaunchedEffect
        withContext(Dispatchers.Default) {
            // Build FeatureCollections off the main thread
            val portalFeatures = ArrayList<Feature>(entities.portals.size)
            for (p in entities.portals) {
                val point = Point.fromLngLat(p.lng, p.lat)
                val feat = Feature.fromGeometry(point)
                feat.addStringProperty("guid", p.guid)
                feat.addNumberProperty("team", p.team)
                feat.addNumberProperty("level", p.level)
                feat.addNumberProperty("health", p.health)

                val (strokeColor, fillColor) = when (p.team) {
                    1 -> Pair("#70d2ff", "#004f7a") // RES
                    2 -> Pair("#66dc96", "#005a2e") // ENL
                    3 -> Pair("#ff7164", "#7a1000") // MACHINA
                    else -> Pair("#c0c7c7", "#3c4043") // Neutral
                }
                feat.addStringProperty("strokeColor", strokeColor)
                feat.addStringProperty("fillColor", fillColor)

                val radius = when (p.level) {
                    0 -> 4f
                    1, 2 -> 5f
                    3, 4 -> 6f
                    5, 6 -> 7f
                    7, 8 -> 8.5f
                    else -> 5f
                }
                feat.addNumberProperty("radius", radius)
                portalFeatures.add(feat)
            }

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

            val fieldFeatures = ArrayList<Feature>(entities.fields.size)
            for (f in entities.fields) {
                if (f.points.size >= 3) {
                    val ring = f.points.map { Point.fromLngLat(it[1], it[0]) }.toMutableList()
                    // GeoJSON polygon ring must be closed
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

            val portalCollection = FeatureCollection.fromFeatures(portalFeatures)
            val linkCollection = FeatureCollection.fromFeatures(linkFeatures)
            val fieldCollection = FeatureCollection.fromFeatures(fieldFeatures)

            withContext(Dispatchers.Main) {
                style.getSourceAs<GeoJsonSource>(SOURCE_FIELDS)?.setGeoJson(fieldCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_LINKS)?.setGeoJson(linkCollection)
                style.getSourceAs<GeoJsonSource>(SOURCE_PORTALS)?.setGeoJson(portalCollection)
            }
        }
    }

    // AndroidView hosting MapView
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
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

                    // Load dark theme style with Ingress layers (no API key required)
                    val styleBuilder = Style.Builder().fromJson(NO_KEY_DARK_STYLE_JSON)

                    // Sources
                    styleBuilder.withSource(GeoJsonSource(SOURCE_FIELDS))
                    styleBuilder.withSource(GeoJsonSource(SOURCE_LINKS))
                    styleBuilder.withSource(GeoJsonSource(SOURCE_PORTALS))

                    // Fields fill layer
                    val fieldsFillLayer = FillLayer(LAYER_FIELDS_FILL, SOURCE_FIELDS).apply {
                        setProperties(
                            fillColor(get("color")),
                            fillOpacity(0.22f),
                            fillAntialias(true)
                        )
                    }
                    styleBuilder.withLayer(fieldsFillLayer)

                    // Fields stroke layer
                    val fieldsStrokeLayer = LineLayer(LAYER_FIELDS_STROKE, SOURCE_FIELDS).apply {
                        setProperties(
                            lineColor(get("color")),
                            lineWidth(1.2f),
                            lineOpacity(0.6f)
                        )
                    }
                    styleBuilder.withLayer(fieldsStrokeLayer)

                    // Links layer
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

                    // Portals circle layer
                    val portalsLayer = CircleLayer(LAYER_PORTALS, SOURCE_PORTALS).apply {
                        setProperties(
                            circleRadius(get("radius")),
                            circleColor(get("fillColor")),
                            circleStrokeColor(get("strokeColor")),
                            circleStrokeWidth(1.8f),
                            circleOpacity(0.95f),
                            circleStrokeOpacity(1f)
                        )
                    }
                    styleBuilder.withLayer(portalsLayer)

                    map.setStyle(styleBuilder) { loadedStyle ->
                        mapStyle = loadedStyle
                    }

                    // Handle portal tap
                    map.addOnMapClickListener { point ->
                        val screenPoint = map.projection.toScreenLocation(point)
                        val features = map.queryRenderedFeatures(screenPoint, LAYER_PORTALS)
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
