package org.exarhteam.iitcprime.bridge

object EventsToIitc {
    fun showLayer(id: Int, show: Boolean): String {
        return "window.layerChooser?.showLayer($id, $show); true;"
    }

    fun changePortalHighlights(name: String): String {
        val escaped = name.replace("'", "\\'")
        return "window.changePortalHighlights?.('$escaped'); true;"
    }

    fun switchToPane(name: String): String {
        val escaped = name.replace("'", "\\'")
        return "window.show?.('$escaped'); true;"
    }

    fun closeDialog(id: String): String {
        val escaped = id.replace("\"", "\\\"")
        return "(function() { var s = $(window.DIALOGS[\"$escaped\"]); if (s) { s.dialog('close'); s.remove(); } })(); true;"
    }

    fun userLocationLocate(lat: Double, lng: Double, accuracy: Double, persistentZoom: Boolean): String {
        return "window.plugin?.userLocation?.locate($lat, $lng, $accuracy, $persistentZoom); true;"
    }

    fun userLocationUpdate(lat: Double, lng: Double): String {
        return "window.plugin?.userLocation?.onLocationChange($lat, $lng); true;"
    }

    fun userLocationOrientation(direction: Double?): String {
        val dirStr = direction?.toString() ?: "null"
        return "window.plugin?.userLocation?.onOrientationChange($dirStr); true;"
    }

    fun setView(lat: Double, lng: Double, persistentZoom: Boolean): String {
        val zoom = if (persistentZoom) "window.map?.getZoom() || 15" else "window.DEFAULT_ZOOM || 15"
        return "window.map?.setView([$lat, $lng], $zoom); true;"
    }

    fun setSafeAreaInsets(top: Float = 0f, bottom: Float = 0f, left: Float = 0f, right: Float = 0f): String {
        return """
            document.documentElement.style.setProperty('--safe-area-inset-top', '${top}px');
            document.documentElement.style.setProperty('--safe-area-inset-bottom', '${bottom}px');
            document.documentElement.style.setProperty('--safe-area-inset-left', '${left}px');
            document.documentElement.style.setProperty('--safe-area-inset-right', '${right}px');
            true;
        """.trimIndent()
    }

    fun applyPaneSafeAreaInsets(): String = """
        (function() {
          if (!window.__paneSafeAreaEls) window.__paneSafeAreaEls = [];
          document.querySelectorAll('body > div').forEach(function(el) {
            var cs = getComputedStyle(el);
            if (!el.classList.contains('safe-area-insets') &&
                cs.display !== 'none' &&
                parseFloat(cs.paddingTop) === 0 &&
                parseFloat(cs.marginTop) === 0) {
              el.classList.add('safe-area-insets');
              window.__paneSafeAreaEls.push(el);
            }
          });
        })(); true;
    """.trimIndent()

    fun removePaneSafeAreaInsets(): String = """
        (function() {
          (window.__paneSafeAreaEls || []).forEach(function(el) {
            el.classList.remove('safe-area-insets');
          });
          window.__paneSafeAreaEls = [];
        })(); true;
    """.trimIndent()
}
