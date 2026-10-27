package io.github.movetakeitblue.iitcexpressive.bridge

object EventsToIitc {
    fun showLayer(id: Int, show: Boolean): String {
        return "try { window.layerChooser?.showLayer($id, $show); } catch(e) {}; true;"
    }

    fun changePortalHighlights(name: String): String {
        val escaped = name.replace("'", "\\'")
        return "try { window.changePortalHighlights?.('$escaped'); } catch(e) {}; true;"
    }

    fun switchToPane(name: String): String {
        val escaped = name.replace("'", "\\'")
        return """
            (function() {
              try {
                var id = '$escaped';
                window.currentPane = '';
                if (window.show) {
                  window.show(id);
                }
                if (id === 'info') {
                  if (window.selectedPortal && window.renderPortalDetails) {
                    window.renderPortalDetails(window.selectedPortal);
                  }
                  var sw = document.getElementById('scrollwrapper');
                  if (sw) sw.style.display = 'block';
                  if (typeof $ !== 'undefined') {
                    $('#scrollwrapper').show();
                    if (window.resetScrollOnNewPortal) window.resetScrollOnNewPortal();
                  }
                } else if (id === 'map') {
                  var m = document.getElementById('map');
                  if (m) {
                    m.style.visibility = 'visible';
                    m.style.opacity = '1';
                    m.style.display = 'block';
                  }
                  if (typeof $ !== 'undefined') {
                    $('#map').css({ visibility: 'visible', opacity: '1', display: 'block' });
                    $('#updatestatus').show();
                    $('#sidebartoggle, #sidebar, #scrollwrapper, #chat, #chatcontrols, #chatinput').hide();
                  }
                  if (window.map) window.map.invalidateSize();
                } else if (id === 'all' || id === 'faction' || id === 'alerts') {
                  // Legacy HTML chat is permanently replaced by native M3E ChatSheet; ensure web chat stays hidden
                  if (typeof $ !== 'undefined') {
                    $('#sidebartoggle, #sidebar, #scrollwrapper, #chat, #chatcontrols, #chatinput').hide();
                  }
                }
              } catch(e) {
                console.error('[Bridge] switchToPane error: ' + e);
              }
            })(); true;
        """.trimIndent()
    }

    fun closeDialog(id: String): String {
        val escaped = id.replace("\"", "\\\"")
        return """
            (function() {
              try {
                if (typeof $ !== 'undefined' && window.DIALOGS && window.DIALOGS["$escaped"]) {
                  var s = $(window.DIALOGS["$escaped"]);
                  if (s && s.dialog) s.dialog('close');
                  if (s) s.remove();
                }
              } catch(e) {}
            })(); true;
        """.trimIndent()
    }

    fun userLocationLocate(lat: Double, lng: Double, accuracy: Double, persistentZoom: Boolean): String {
        val zoomExpr = if (persistentZoom) "window.map.getZoom()" else "(window.DEFAULT_ZOOM || 15)"
        return """
            (function() {
              try {
                if (window.plugin && window.plugin.userLocation && typeof window.plugin.userLocation.locate === 'function') {
                  window.plugin.userLocation.locate($lat, $lng, $accuracy, $persistentZoom);
                } else if (window.map) {
                  window.map.setView([$lat, $lng], $zoomExpr);
                }
              } catch(e) {
                console.error('[Bridge] userLocationLocate error: ' + e);
              }
            })(); true;
        """.trimIndent()
    }

    fun userLocationUpdate(lat: Double, lng: Double): String {
        return """
            (function() {
              try {
                if (window.plugin && window.plugin.userLocation && typeof window.plugin.userLocation.onLocationChange === 'function') {
                  window.plugin.userLocation.onLocationChange($lat, $lng);
                }
              } catch(e) {}
            })(); true;
        """.trimIndent()
    }

    fun userLocationOrientation(direction: Double?): String {
        val dirStr = direction?.toString() ?: "null"
        return """
            (function() {
              try {
                if (window.plugin && window.plugin.userLocation && typeof window.plugin.userLocation.onOrientationChange === 'function') {
                  window.plugin.userLocation.onOrientationChange($dirStr);
                }
              } catch(e) {}
            })(); true;
        """.trimIndent()
    }

    fun setView(lat: Double, lng: Double, persistentZoom: Boolean): String {
        val zoom = if (persistentZoom) "window.map?.getZoom() || 15" else "window.DEFAULT_ZOOM || 15"
        return "try { window.map?.setView([$lat, $lng], $zoom); } catch(e) {}; true;"
    }

    fun zoomIn(): String {
        return "try { window.map?.zoomIn(); } catch(e) {}; true;"
    }

    fun zoomOut(): String {
        return "try { window.map?.zoomOut(); } catch(e) {}; true;"
    }

    fun setSafeAreaInsets(top: Float = 0f, bottom: Float = 0f, left: Float = 0f, right: Float = 0f): String {
        return """
            try {
              document.documentElement.style.setProperty('--safe-area-inset-top', '${top}px');
              document.documentElement.style.setProperty('--safe-area-inset-bottom', '${bottom}px');
              document.documentElement.style.setProperty('--safe-area-inset-left', '${left}px');
              document.documentElement.style.setProperty('--safe-area-inset-right', '${right}px');
            } catch(e) {}
            true;
        """.trimIndent()
    }

    fun applyPaneSafeAreaInsets(): String = """
        (function() {
          try {
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
          } catch(e) {}
        })(); true;
    """.trimIndent()

    fun removePaneSafeAreaInsets(): String = """
        (function() {
          try {
            (window.__paneSafeAreaEls || []).forEach(function(el) {
              el.classList.remove('safe-area-insets');
            });
            window.__paneSafeAreaEls = [];
          } catch(e) {}
        })(); true;
    """.trimIndent()

    fun selectPortal(guid: String): String {
        val escaped = guid.replace("'", "\\'")
        return "try { if (window.renderPortalDetails) window.renderPortalDetails('$escaped'); } catch(e) {}; true;"
    }

    fun setMapCenterZoom(lat: Double, lng: Double, zoom: Double? = null): String {
        val zStr = if (zoom != null) zoom.toString() else "window.map?.getZoom() || 15"
        return "try { window.map?.setView([$lat, $lng], $zStr); } catch(e) {}; true;"
    }

    fun syncPluginStorage(key: String, jsonValue: String): String {
        val escapedKey = key.replace("'", "\\'")
        val escapedVal = jsonValue.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "")
        return """
            (function() {
                try {
                    if (window.localStorage) {
                        window.localStorage.setItem('$escapedKey', '$escapedVal');
                    }
                    if ('$escapedKey' === 'plugin-draw-tools-layer' && window.plugin && window.plugin.drawTools && typeof window.plugin.drawTools.load === 'function') {
                        window.plugin.drawTools.load();
                    }
                    if ('$escapedKey' === 'plugin-bookmarks-portals-data' && window.plugin && window.plugin.bookmarks && typeof window.plugin.bookmarks.loadStorage === 'function') {
                        window.plugin.bookmarks.loadStorage();
                    }
                } catch(e) {}
            })(); true;
        """.trimIndent()
    }
}
