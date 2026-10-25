package io.github.movetakeitblue.iitcexpressive.platform

interface PlatformLocationListener {
    fun onLocation(lat: Double, lng: Double, accuracy: Double)
    fun onOrientation(direction: Double?)
    fun onError(message: String)
}

expect object PlatformUtils {
    fun shareText(text: String, title: String)
    fun shareGeo(lat: Double, lng: Double, title: String)
    fun shareUrl(url: String, title: String = "")
    fun shareFile(filename: String, bytes: ByteArray, mimeType: String)
    fun copyToClipboard(text: String, toastMessage: String? = null)
    fun readClipboardText(): String?
    fun hasClipboardUrl(): Boolean
    fun openInBrowser(url: String)
    fun openUrl(url: String)
    fun pickFile(): ByteArray?
    fun currentTimeMillis(): Long
    fun clearCookies(onComplete: (Boolean) -> Unit)
    fun getAppVersionName(): String
    fun getGitCommitHash(): String
    fun setStatusBarContrast(isDarkContent: Boolean)
    fun startLocationTracking(listener: PlatformLocationListener)
    fun stopLocationTracking()
    fun requestSingleLocationFix(listener: PlatformLocationListener)
    fun configureNativeWebView(webView: Any)
    fun decodeImageBitmap(bytes: ByteArray): androidx.compose.ui.graphics.ImageBitmap?
}
