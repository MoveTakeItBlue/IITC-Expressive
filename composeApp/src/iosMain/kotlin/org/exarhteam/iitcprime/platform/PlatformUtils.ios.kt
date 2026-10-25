package org.exarhteam.iitcprime.platform

import platform.UIKit.*
import platform.Foundation.*
import platform.WebKit.*
import platform.CoreLocation.*
import platform.darwin.NSObject
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual object PlatformUtils {
    private var locationManager: CLLocationManager? = null

    actual fun shareText(text: String, title: String) {
        val items = listOf(text)
        val activityController = UIActivityViewController(activityItems = items, applicationActivities = null)
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        root?.presentViewController(activityController, animated = true, completion = null)
    }

    actual fun shareGeo(lat: Double, lng: Double, title: String) {
        val urlString = "https://maps.apple.com/?ll=$lat,$lng&q=${title}"
        openInBrowser(urlString)
    }

    actual fun shareUrl(url: String, title: String) {
        shareText(url, title)
    }

    actual fun shareFile(filename: String, bytes: ByteArray, mimeType: String) {
        // iOS share file
        val tempDir = NSTemporaryDirectory()
        val path = "$tempDir/$filename"
        val data = bytes.toNSData()
        data.writeToFile(path, atomically = true)
        val url = NSURL.fileURLWithPath(path)
        val activityController = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        root?.presentViewController(activityController, animated = true, completion = null)
    }

    actual fun copyToClipboard(text: String, toastMessage: String?) {
        UIPasteboard.generalPasteboard.string = text
    }

    actual fun readClipboardText(): String? {
        return UIPasteboard.generalPasteboard.string
    }

    actual fun hasClipboardUrl(): Boolean {
        val text = readClipboardText() ?: return false
        return text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true)
    }

    actual fun openInBrowser(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    }

    actual fun openUrl(url: String) {
        openInBrowser(url)
    }

    actual fun pickFile(): ByteArray? {
        return null
    }

    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    actual fun clearCookies(onComplete: (Boolean) -> Unit) {
        val dataStore = WKWebsiteDataStore.defaultDataStore()
        val dataTypes = NSSet.setWithObject(WKWebsiteDataTypeCookies)
        val dateFrom = NSDate.dateWithTimeIntervalSince1970(0.0)
        dataStore.removeDataOfTypes(dataTypes, modifiedSince = dateFrom) {
            onComplete(true)
        }
    }

    actual fun getAppVersionName(): String {
        val info = NSBundle.mainBundle.infoDictionary
        return info?.get("CFBundleShortVersionString") as? String ?: "1.2.0"
    }

    actual fun getGitCommitHash(): String {
        return ""
    }

    actual fun setStatusBarContrast(isDarkContent: Boolean) {
        // Managed in iOS UIViewController
    }

    actual fun startLocationTracking(listener: PlatformLocationListener) {
        stopLocationTracking()
        val lm = CLLocationManager()
        locationManager = lm
        lm.requestWhenInUseAuthorization()
        lm.startUpdatingLocation()
        lm.startUpdatingHeading()
    }

    actual fun stopLocationTracking() {
        locationManager?.stopUpdatingLocation()
        locationManager?.stopUpdatingHeading()
        locationManager = null
    }

    actual fun requestSingleLocationFix(listener: PlatformLocationListener) {
        startLocationTracking(listener)
    }

    actual fun configureNativeWebView(webView: Any) {
        // Platform specific WKWebView configuration on iOS if needed
    }

    actual fun decodeImageBitmap(bytes: ByteArray): androidx.compose.ui.graphics.ImageBitmap? = null
}

private fun ByteArray.toNSData(): NSData {
    // Helper to convert ByteArray to NSData in Kotlin Native
    val pinned = this.pin()
    return NSData.dataWithBytes(pinned.addressOf(0), this.size.toULong())
}
