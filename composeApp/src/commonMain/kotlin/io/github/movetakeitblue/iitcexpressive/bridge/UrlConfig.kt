package io.github.movetakeitblue.iitcexpressive.bridge

object UrlConfig {
    const val INGRESS_INTEL_MAP = "https://intel.ingress.com/intel"
    const val DEMO_INTEL_HOST = "demo.iitc.app"
    const val DEMO_INTEL_MAP = "https://demo.iitc.app/intel"

    val INITIAL_INTERNAL_HOSTNAMES = listOf(
        "intel.ingress.com",
        "login.ingress.com",
        "signin.nianticlabs.com",
        "signin.nianticspatial.com",
        DEMO_INTEL_HOST
    )

    private val INTEL_HOSTS = setOf("intel.ingress.com", DEMO_INTEL_HOST)

    private val LOGIN_HOST_PATTERNS = listOf(
        Regex("^(accounts|myaccount|appengine|gds)\\.google\\.(com|[a-z]{2}|com?\\.[a-z]{2})$"),
        Regex("^accounts\\.youtube\\.com$"),
        Regex("^appleid\\.apple\\.com$")
    )

    private val WEBVIEW_SCHEMES = setOf(
        "http", "https", "about", "file", "data", "blob", "javascript", "x-local"
    )

    fun getHostname(url: String): String {
        return try {
            val schemeEnd = url.indexOf("://")
            if (schemeEnd == -1) return ""
            val withoutScheme = url.substring(schemeEnd + 3)
            val pathStart = withoutScheme.indexOfAny(charArrayOf('/', '?', '#', ':'))
            val host = if (pathStart == -1) withoutScheme else withoutScheme.substring(0, pathStart)
            host.lowercase()
        } catch (e: Exception) {
            ""
        }
    }

    fun isIntelUrl(url: String): Boolean {
        return INTEL_HOSTS.contains(getHostname(url))
    }

    fun isDemoUrl(url: String): Boolean {
        return getHostname(url) == DEMO_INTEL_HOST
    }

    fun isLoginUrl(url: String): Boolean {
        val host = getHostname(url)
        return LOGIN_HOST_PATTERNS.any { it.matches(host) }
    }

    fun isUrlOnHostnames(url: String, hostnames: List<String>): Boolean {
        val host = getHostname(url)
        return hostnames.any { host == it || host.endsWith(".$it") }
    }

    fun isExternalAppSchemeUrl(url: String): Boolean {
        if (url.isBlank()) return false
        val colon = url.indexOf(':')
        if (colon <= 0) return false
        val scheme = url.substring(0, colon).lowercase()
        return !WEBVIEW_SCHEMES.contains(scheme)
    }

    fun addViewportParam(url: String, desktopMode: Boolean): String {
        val vpValue = if (desktopMode) "f" else "m"
        val sep = if (url.contains("?")) "&" else "?"
        return if (url.contains("vp=")) {
            url.replace(Regex("vp=[^&#]+"), "vp=$vpValue")
        } else {
            "$url${sep}vp=$vpValue"
        }
    }
}
