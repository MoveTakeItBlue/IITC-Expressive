package io.github.movetakeitblue.iitcexpressive.platform

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import java.io.File
import java.lang.ref.WeakReference

object PlatformContext {
    private var contextRef: WeakReference<Context>? = null
    private var activityRef: WeakReference<Activity>? = null

    fun init(context: Context) {
        contextRef = WeakReference(context.applicationContext)
    }

    fun setActivity(activity: Activity?) {
        activityRef = if (activity != null) WeakReference(activity) else null
    }

    val context: Context
        get() = contextRef?.get() ?: throw IllegalStateException("PlatformContext not initialized")

    val activity: Activity?
        get() = activityRef?.get()
}

actual object PlatformUtils {
    private var locationManager: LocationManager? = null
    private var sensorManager: SensorManager? = null
    private var locationListener: LocationListener? = null
    private var sensorListener: SensorEventListener? = null

    actual fun shareText(text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (title.isNotBlank()) putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        PlatformContext.context.startActivity(chooser)
    }

    actual fun shareGeo(lat: Double, lng: Double, title: String) {
        val geoUri = "geo:$lat,$lng?q=$lat,$lng" + if (title.isNotBlank()) "(${Uri.encode(title)})" else ""
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Open in maps").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            PlatformContext.context.startActivity(chooser)
        } catch (e: Exception) {
            shareText("Location: $lat,$lng", title)
        }
    }

    actual fun shareUrl(url: String, title: String) {
        shareText(url, title)
    }

    actual fun shareFile(filename: String, bytes: ByteArray, mimeType: String) {
        try {
            val ctx = PlatformContext.context
            val cacheDir = File(ctx.cacheDir, "shared_files").apply { mkdirs() }
            val file = File(cacheDir, filename).apply { writeBytes(bytes) }
            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share $filename").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun copyToClipboard(text: String, toastMessage: String?) {
        val cm = PlatformContext.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("IITC", text)
        cm.setPrimaryClip(clip)
        if (!toastMessage.isNullOrBlank()) {
            Toast.makeText(PlatformContext.context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    actual fun showToast(message: String) {
        if (message.isNotBlank()) {
            Toast.makeText(PlatformContext.context, message, Toast.LENGTH_SHORT).show()
        }
    }

    actual fun readClipboardText(): String? {
        val cm = PlatformContext.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip ?: return null
        if (clip.itemCount > 0) {
            return clip.getItemAt(0)?.text?.toString()
        }
        return null
    }

    actual fun hasClipboardUrl(): Boolean {
        val text = readClipboardText() ?: return false
        return text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true)
    }

    actual fun openInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            PlatformContext.context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun openUrl(url: String) {
        openInBrowser(url)
    }

    actual fun pickFile(): ByteArray? {
        return null
    }

    actual fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    actual fun clearCookies(onComplete: (Boolean) -> Unit) {
        try {
            CookieManager.getInstance().removeAllCookies { success ->
                onComplete(success)
            }
        } catch (e: Exception) {
            onComplete(false)
        }
    }

    actual fun getAppVersionName(): String {
        return try {
            val ctx = PlatformContext.context
            val pInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            pInfo.versionName ?: "1.2.0"
        } catch (e: Exception) {
            "1.2.0"
        }
    }

    actual fun getGitCommitHash(): String {
        return ""
    }

    actual fun setStatusBarContrast(isDarkContent: Boolean) {
        val activity = PlatformContext.activity ?: return
        activity.runOnUiThread {
            val window = activity.window ?: return@runOnUiThread
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            // isDarkContent == true: status bar background is light -> render dark/black text
            // isDarkContent == false: status bar background is dark -> render light/white text
            insetsController.isAppearanceLightStatusBars = isDarkContent
            insetsController.isAppearanceLightNavigationBars = isDarkContent
        }
    }

    actual fun startLocationTracking(listener: PlatformLocationListener) {
        stopLocationTracking()
        val ctx = PlatformContext.context
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = lm

        locationListener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                listener.onLocation(loc.latitude, loc.longitude, loc.accuracy.toDouble())
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        try {
            val lastGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = listOfNotNull(lastGps, lastNet).maxByOrNull { it.time }
            if (best != null) {
                listener.onLocation(best.latitude, best.longitude, best.accuracy.toDouble())
            }

            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, locationListener!!)
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 1f, locationListener!!)
            }
        } catch (e: SecurityException) {
            listener.onError("Location permission not granted")
        }

        // Compass tracking
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager = sm
        val rotationSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)
            sensorListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val azimuth = Math.toDegrees(orientationAngles[0].toDouble())
                        val normalized = (azimuth + 360.0) % 360.0
                        listener.onOrientation(normalized)
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sm.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    actual fun stopLocationTracking() {
        locationListener?.let { locationManager?.removeUpdates(it) }
        locationListener = null
        sensorListener?.let { sensorManager?.unregisterListener(it) }
        sensorListener = null
    }

    actual fun requestSingleLocationFix(listener: PlatformLocationListener) {
        val ctx = PlatformContext.context
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val lastGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = listOfNotNull(lastGps, lastNet).maxByOrNull { it.time }
            if (best != null) {
                listener.onLocation(best.latitude, best.longitude, best.accuracy.toDouble())
                if ((System.currentTimeMillis() - best.time) < 15000) {
                    return
                }
            }

            val singleListener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    lm.removeUpdates(this)
                    listener.onLocation(loc.latitude, loc.longitude, loc.accuracy.toDouble())
                }
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            var requested = false
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, singleListener, null)
                requested = true
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, singleListener, null)
                requested = true
            }
            if (!requested && best == null) {
                listener.onError("Location provider disabled")
            }
        } catch (e: SecurityException) {
            listener.onError("Location permission required")
        }
    }

    actual fun configureNativeWebView(webView: Any) {
        val wv = webView as? android.webkit.WebView ?: return
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportMultipleWindows(false)
            useWideViewPort = false
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
        try {
            val cookieManager = android.webkit.CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(wv, true)
            cookieManager.setCookie(
                "https://signin.nianticspatial.com",
                "_ncc=0; Path=/; Domain=.nianticspatial.com"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun decodeImageBitmap(bytes: ByteArray): androidx.compose.ui.graphics.ImageBitmap? {
        return try {
            val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bmp?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}
