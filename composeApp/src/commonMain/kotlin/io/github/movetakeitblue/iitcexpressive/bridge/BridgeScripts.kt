package io.github.movetakeitblue.iitcexpressive.bridge

import io.github.movetakeitblue.iitcexpressive.model.Plugin

object BridgeScripts {

    const val M3_EXPRESSIVE_CSS = """
/* Material 3 Expressive (M3E) In-WebView Style Adaptation */
:root {
  --m3-surface: #111415;
  --m3-surface-container-lowest: #0C0F10;
  --m3-surface-container-low: #191C1D;
  --m3-surface-container: #1D2022;
  --m3-surface-container-high: #282B2D;
  --m3-surface-container-highest: #333638;
  --m3-on-surface: #E0E3E3;
  --m3-on-surface-variant: #C4C7C7;
  --m3-outline: #8E9192;
  --m3-outline-variant: #444748;
  --m3-primary: #95F4E3;
  --m3-primary-container: #005047;
  --m3-on-primary-container: #95F4E3;
  --m3-secondary-container: #334B46;
  --m3-on-secondary-container: #CCE8E0;
  --m3-faction-res: #70D2FF;
  --m3-faction-enl: #66DC96;
  --m3-faction-mac: #FF7164;
  --m3-radius-sm: 8px;
  --m3-radius-md: 16px;
  --m3-radius-lg: 24px;
  --m3-radius-full: 9999px;
  --m3-font: system-ui, -apple-system, Roboto, "Segoe UI", sans-serif;
}

html, body {
  min-height: initial;
  min-width: initial;
  font-family: var(--m3-font);
  background-color: var(--m3-surface);
  color: var(--m3-on-surface);
  -webkit-tap-highlight-color: transparent;
}

a {
  color: var(--m3-faction-res);
  text-decoration: none;
}
a:hover, a:focus {
  text-decoration: underline;
}

.res, .resistance, .text-res { color: var(--m3-faction-res) !important; }
.enl, .enlightened, .text-enl { color: var(--m3-faction-enl) !important; }
.mac, .machina, .text-mac { color: var(--m3-faction-mac) !important; }

#sidebartoggle,
#sidebar,
#scrollwrapper,
#chatcontrols,
#chat,
#chatinput,
#chattext,
#updatestatus,
#innerstatus,
#mobileinfo,
#loadlevel,
body.show_controls #chatcontrols,
#chatcontrols a,
#chatcontrols .toggle {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  height: 0 !important;
  min-height: 0 !important;
  max-height: 0 !important;
  width: 0 !important;
  min-width: 0 !important;
  max-width: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
  border: none !important;
  outline: none !important;
  pointer-events: none !important;
  position: absolute !important;
  top: -9999px !important;
  left: -9999px !important;
  z-index: -9999 !important;
  clip: rect(0, 0, 0, 0) !important;
  clip-path: inset(50%) !important;
  transform: scale(0) !important;
}

#map,
body #map,
body.show_controls #map {
  visibility: visible !important;
  opacity: 1 !important;
  display: block !important;
  position: absolute !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100% !important;
  height: 100% !important;
  min-height: 100% !important;
  max-height: 100% !important;
  margin: 0 !important;
  padding: 0 !important;
}

body.native_map,
body.native_map #map {
  background: transparent !important;
  background-color: transparent !important;
}

body.native_map .leaflet-pane,
body.native_map .leaflet-control-container {
  opacity: 0 !important;
  pointer-events: none !important;
}

body.show_controls {
  padding-top: 0 !important;
  margin-top: 0 !important;
}

.ui-dialog {
  position: absolute;
  z-index: 9500;
  background-color: var(--m3-surface-container) !important;
  color: var(--m3-on-surface) !important;
  border: none !important;
  border-radius: var(--m3-radius-lg) !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.55), 0 1px 3px rgba(0, 0, 0, 0.3) !important;
  padding: 0 !important;
  overflow: hidden !important;
  font-family: var(--m3-font) !important;
  max-width: calc(100vw - 32px) !important;
  box-sizing: border-box !important;
}

@media (min-width: 600px) {
  .ui-dialog { max-width: 540px !important; }
}

.ui-widget-overlay {
  height: 100%;
  left: 0;
  position: fixed;
  top: 0;
  width: 100%;
  z-index: 9000;
  background: rgba(0, 0, 0, 0.65) !important;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.ui-dialog-titlebar {
  background-color: var(--m3-surface-container-high) !important;
  border: none !important;
  color: var(--m3-on-surface) !important;
  padding: 16px 20px 14px 20px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: space-between !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  line-height: 24px !important;
  letter-spacing: 0.15px !important;
  position: relative !important;
}

.ui-dialog-title {
  color: var(--m3-on-surface) !important;
  font-weight: 600 !important;
  font-size: 16px !important;
  margin: 0 !important;
  width: auto !important;
  flex: 1 1 auto !important;
  white-space: nowrap !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
}

.ui-dialog-title-active { color: var(--m3-primary) !important; }
.ui-dialog-title-inactive { color: var(--m3-on-surface-variant) !important; }

.ui-dialog-titlebar-button {
  position: static !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  width: 32px !important;
  height: 32px !important;
  border-radius: var(--m3-radius-full) !important;
  border: none !important;
  background-color: rgba(255, 255, 255, 0.08) !important;
  color: var(--m3-on-surface) !important;
  cursor: pointer !important;
  margin-left: 8px !important;
  padding: 0 !important;
  transition: background-color 0.2s ease !important;
}

.ui-dialog-titlebar-button:active,
.ui-dialog-titlebar-button:hover {
  background-color: rgba(255, 255, 255, 0.16) !important;
}

.ui-dialog-titlebar-button-close::after,
.ui-dialog-titlebar-button-close::before {
  content: "" !important;
  position: absolute !important;
  width: 14px !important;
  height: 2px !important;
  background-color: var(--m3-on-surface) !important;
  border: none !important;
  left: 50% !important;
  top: 50% !important;
  margin: 0 !important;
}

.ui-dialog-titlebar-button-close::after {
  transform: translate(-50%, -50%) rotate(45deg) !important;
  -webkit-transform: translate(-50%, -50%) rotate(45deg) !important;
}

.ui-dialog-titlebar-button-close::before {
  transform: translate(-50%, -50%) rotate(-45deg) !important;
  -webkit-transform: translate(-50%, -50%) rotate(-45deg) !important;
}

.ui-dialog-titlebar-button-collapse { display: none !important; }

.ui-dialog-content {
  padding: 16px 20px !important;
  color: var(--m3-on-surface-variant) !important;
  font-size: 14px !important;
  line-height: 20px !important;
  overflow-y: auto !important;
  max-height: calc(85vh - 120px) !important;
  background-color: transparent !important;
}

.ui-dialog-buttonpane {
  padding: 12px 20px 16px 20px !important;
  border-top: none !important;
  background-color: transparent !important;
  margin: 0 !important;
}

.ui-dialog-buttonset {
  display: flex !important;
  justify-content: flex-end !important;
  gap: 8px !important;
}

.ui-dialog-buttonset button,
.ui-dialog-content button,
.ui-dialog input[type="button"],
.ui-dialog input[type="submit"] {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  min-height: 40px !important;
  padding: 8px 18px !important;
  border-radius: var(--m3-radius-full) !important;
  border: none !important;
  background-color: var(--m3-primary-container) !important;
  color: var(--m3-on-primary-container) !important;
  font-family: var(--m3-font) !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  letter-spacing: 0.1px !important;
  cursor: pointer !important;
  transition: opacity 0.2s ease, transform 0.1s ease !important;
  text-decoration: none !important;
  box-shadow: none !important;
}

.ui-dialog-buttonset button:active,
.ui-dialog-content button:active {
  opacity: 0.8 !important;
  transform: scale(0.98) !important;
}

.ui-dialog-buttonset button:not(:last-child) {
  background-color: var(--m3-surface-container-high) !important;
  color: var(--m3-on-surface) !important;
}

.ui-dialog input[type="text"],
.ui-dialog input[type="number"],
.ui-dialog input[type="search"],
.ui-dialog select,
.ui-dialog textarea {
  background-color: var(--m3-surface-container-lowest) !important;
  color: var(--m3-on-surface) !important;
  border: 1px solid var(--m3-outline-variant) !important;
  border-radius: var(--m3-radius-sm) !important;
  padding: 8px 12px !important;
  font-family: var(--m3-font) !important;
  font-size: max(16px, 14px) !important;
  box-sizing: border-box !important;
  outline: none !important;
  transition: border-color 0.2s ease !important;
}

.ui-dialog input:focus,
.ui-dialog select:focus,
.ui-dialog textarea:focus {
  border-color: var(--m3-primary) !important;
}

.ui-tooltip {
  position: absolute !important;
  z-index: 10002 !important;
  background-color: var(--m3-surface-container-high) !important;
  color: var(--m3-on-surface) !important;
  border: none !important;
  border-radius: var(--m3-radius-sm) !important;
  padding: 8px 12px !important;
  font-size: 13px !important;
  line-height: 18px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4) !important;
  pointer-events: none !important;
}

.leaflet-bar {
  border: none !important;
  border-radius: var(--m3-radius-md) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4) !important;
  background-color: var(--m3-surface-container) !important;
  overflow: hidden !important;
}

.leaflet-bar a,
.leaflet-bar a:hover {
  background-color: var(--m3-surface-container) !important;
  color: var(--m3-on-surface) !important;
  border-bottom: 1px solid var(--m3-outline-variant) !important;
  width: 40px !important;
  height: 40px !important;
  line-height: 40px !important;
  font-size: 20px !important;
  font-weight: 500 !important;
  text-align: center !important;
  transition: background-color 0.2s ease !important;
}

.leaflet-bar a:last-child {
  border-bottom: none !important;
}

.leaflet-bar a:hover,
.leaflet-bar a:active {
  background-color: var(--m3-surface-container-highest) !important;
}

.leaflet-bar a.leaflet-disabled {
  opacity: 0.38 !important;
  background-color: var(--m3-surface-container) !important;
}

.leaflet-popup-content-wrapper {
  background-color: var(--m3-surface-container) !important;
  color: var(--m3-on-surface) !important;
  border: none !important;
  border-radius: var(--m3-radius-md) !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5) !important;
  padding: 6px !important;
  font-family: var(--m3-font) !important;
}

.leaflet-popup-tip {
  background-color: var(--m3-surface-container) !important;
  box-shadow: none !important;
}

.leaflet-container a.leaflet-popup-close-button {
  color: var(--m3-on-surface-variant) !important;
  font-size: 18px !important;
  padding: 4px 6px !important;
}

.leaflet-container a.leaflet-popup-close-button:hover {
  color: var(--m3-on-surface) !important;
}

.leaflet-control-layers {
  background-color: var(--m3-surface-container) !important;
  color: var(--m3-on-surface) !important;
  border: none !important;
  border-radius: var(--m3-radius-md) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4) !important;
  padding: 8px 12px !important;
}

.leaflet-control-scale-line {
  background: rgba(17, 20, 21, 0.8) !important;
  color: var(--m3-on-surface) !important;
  border-color: var(--m3-outline) !important;
  border-radius: 4px !important;
  padding: 2px 6px !important;
  font-size: 11px !important;
}

#dashboard_container {
  display: flex;
  flex-direction: column;
  min-width: 245px;
  min-height: 365px;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}

.button {
  margin: 12px auto;
  padding: 0;
  width: 100%;
  max-width: 320px;
}

#dashboard_container a,
.button_link {
  display: block;
  padding: 14px 20px;
  border-radius: var(--m3-radius-full);
  background-color: var(--m3-primary-container) !important;
  color: var(--m3-on-primary-container) !important;
  font-family: var(--m3-font);
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  text-decoration: none;
  border: none !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  transition: opacity 0.2s ease;
}

#dashboard_container a:active,
.button_link:active {
  opacity: 0.85;
}

input, textarea, select {
  font-size: max(16px, 1em) !important;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--m3-surface-container-highest);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--m3-outline);
}
"""

    fun buildBridgeScript(): String {
        val escapedM3Css = escapeJsonString(M3_EXPRESSIVE_CSS)
        return """
        (function() {
            // Defensive localStorage fallback if uninitialized or broken
            try {
                if (!window.localStorage) throw new Error();
                window.localStorage.getItem('__iitc_test__');
            } catch(e) {
                try {
                    var _memStore = {};
                    var fakeStorage = new Proxy(_memStore, {
                        get: function(target, prop) {
                            if (prop === 'getItem') return function(k) { return target.hasOwnProperty(k) ? target[k] : null; };
                            if (prop === 'setItem') return function(k, v) { target[k] = String(v); };
                            if (prop === 'removeItem') return function(k) { delete target[k]; };
                            if (prop === 'clear') return function() { for (var k in target) delete target[k]; };
                            if (prop === 'length') return Object.keys(target).length;
                            if (prop === 'key') return function(i) { return Object.keys(target)[i] || null; };
                            return target.hasOwnProperty(prop) ? target[prop] : undefined;
                        },
                        set: function(target, prop, val) {
                            target[prop] = String(val);
                            return true;
                        }
                    });
                    Object.defineProperty(window, 'localStorage', {
                        value: fakeStorage,
                        configurable: true,
                        enumerable: true,
                        writable: true
                    });
                } catch(e2) {}
            }

            // Fallback stubs for pre-boot hooks to prevent race condition crashes
            if (typeof window.runOnSmartphonesBeforeBoot !== 'function') {
                window.runOnSmartphonesBeforeBoot = function() {};
            }
            if (typeof window.runOnAppBeforeBoot !== 'function') {
                window.runOnAppBeforeBoot = function() {};
            }
            if (!window.plugin) window.plugin = function() {};
            if (!window.bootPlugins) window.bootPlugins = [];
            if (window.iitcLoaded === undefined) window.iitcLoaded = false;
            window.isApp = true;

            // Permanently hide legacy HTML chat/sidebar controls so native M3E UI and Leaflet map are unobstructed
            (function() {
                var styleId = 'm3e-hide-legacy-chat';
                var hideCss = '#map, body #map, body.show_controls #map { ' +
                    'visibility: visible !important; opacity: 1 !important; display: block !important; ' +
                    'position: absolute !important; top: 0 !important; left: 0 !important; right: 0 !important; bottom: 0 !important; ' +
                    'width: 100% !important; height: 100% !important; min-height: 100% !important; max-height: 100% !important; ' +
                    'margin: 0 !important; padding: 0 !important; } ' +
                    'body.show_controls { padding-top: 0 !important; margin-top: 0 !important; } ' +
                    'body.show_controls #map { margin-top: 0 !important; height: 100% !important; } ' +
                    '#sidebartoggle, #sidebar, #scrollwrapper, #chatcontrols, #chat, #chatinput, #chattext, ' +
                    '#updatestatus, #innerstatus, #mobileinfo, #loadlevel, body.show_controls #chatcontrols, #chatcontrols a, #chatcontrols .toggle { ' +
                    'display: none !important; visibility: hidden !important; opacity: 0 !important; ' +
                    'height: 0 !important; min-height: 0 !important; max-height: 0 !important; ' +
                    'width: 0 !important; min-width: 0 !important; max-width: 0 !important; ' +
                    'margin: 0 !important; padding: 0 !important; border: none !important; outline: none !important; ' +
                    'pointer-events: none !important; position: absolute !important; top: -9999px !important; left: -9999px !important; ' +
                    'z-index: -9999 !important; clip: rect(0,0,0,0) !important; clip-path: inset(50%) !important; transform: scale(0) !important; }';

                function applyHideStyle() {
                    try {
                        var existing = document.getElementById(styleId);
                        if (!existing) {
                            var style = document.createElement('style');
                            style.id = styleId;
                            style.textContent = hideCss;
                            (document.documentElement || document.head).appendChild(style);
                        }
                    } catch(e) {}
                }

                applyHideStyle();
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', applyHideStyle);
                }
                window.addEventListener('load', applyHideStyle);
            })();

            if (window.nsWebViewBridge && window.app) {
                try {
                    window.nsWebViewBridge.injectStyleSheet('iitcprimecss', $escapedM3Css, true);
                } catch(e) {}
                return;
            }
            
            var bridge = {
                _listeners: {},
                on: function(event, callback) {
                    if (!this._listeners[event]) this._listeners[event] = [];
                    this._listeners[event].push(callback);
                },
                off: function(event, callback) {
                    if (!this._listeners[event]) return;
                    if (!callback) { delete this._listeners[event]; return; }
                    this._listeners[event] = this._listeners[event].filter(function(cb) { return cb !== callback; });
                },
                emit: function(event, data) {
                    var payload = JSON.stringify([event, data]);
                    if (window.androidJsBridge && window.androidJsBridge.callAndroid) {
                        window.androidJsBridge.callAndroid(0, "bridge", payload);
                    } else if (window.kmpJsBridge && window.kmpJsBridge.callNative) {
                        window.kmpJsBridge.callNative("bridge", payload);
                    } else if (window.androidBridge && window.androidBridge.postMessage) {
                        window.androidBridge.postMessage(payload);
                    } else if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.nsWebViewBridge) {
                        window.webkit.messageHandlers.nsWebViewBridge.postMessage(payload);
                    }
                },
                injectStyleSheet: function(id, css, replace) {
                    var existing = document.getElementById(id);
                    if (existing) {
                        if (replace) existing.textContent = css;
                        return;
                    }
                    var style = document.createElement('style');
                    style.id = id;
                    style.textContent = css;
                    (document.head || document.documentElement).appendChild(style);
                },
                getVersionName: function() { return window.__iitcVersionName || '1.2.0'; },
                showZoom: function() { return window.__iitcShowZoom === true; }
            };

            var syncEvents = [
                'intentPosLink', 'shareString', 'copy', 'switchToPane', 'bootFinished',
                'setLayers', 'addPortalHighlighter', 'setActiveHighlighter', 'addPane',
                'setFollowMode', 'setMapDarkMode', 'setPortalStatus', 'setPortalDetails', 'setChatMessages', 'setMapStatus',
                'setProgress', 'setPermalink', 'addInternalHostname', 'saveFile',
                'reloadIITC', 'openDemo', 'dialogFocused', 'dialogOpened', 'spinnerEnabled',
                'setEntities'
            ];

            var argNames = {
                intentPosLink: ['lat', 'lng', 'zoom', 'title', 'isPortal', 'guid'],
                shareString: ['str'],
                copy: ['s'],
                switchToPane: ['id'],
                bootFinished: [],
                setLayers: ['base_layer', 'overlay_layer'],
                addPortalHighlighter: ['name'],
                setActiveHighlighter: ['name'],
                addPane: ['name', 'label', 'icon'],
                setFollowMode: ['follow'],
                setMapDarkMode: ['isDark'],
                setPortalStatus: ['guid', 'team', 'level', 'title', 'health', 'resonators', 'levelColor', 'isLoading'],
                setPortalDetails: ['guid', 'details'],
                setChatMessages: ['channel', 'messages'],
                setMapStatus: ['portalLevels', 'mapStatus', 'requests'],
                setProgress: ['progress'],
                setPermalink: ['href'],
                addInternalHostname: ['domain'],
                saveFile: ['filename', 'dataType', 'content'],
                reloadIITC: ['clearCache'],
                openDemo: [],
                dialogFocused: ['id'],
                dialogOpened: ['id', 'open'],
                spinnerEnabled: ['enabled'],
                setEntities: ['data']
            };

            syncEvents.forEach(function(evt) {
                var names = argNames[evt] || [];
                bridge[evt] = function() {
                    var args = arguments;
                    var data = {};
                    names.forEach(function(name, idx) {
                        data[name] = args[idx];
                    });
                    return bridge.emit('JSBridge', [evt, data]);
                };
            });

            window.nsWebViewBridge = bridge;
            window.app = bridge;
            window.android = bridge;
            window.isApp = true;

            // Inject M3E Expressive CSS Sheet
            bridge.injectStyleSheet('iitcprimecss', $escapedM3Css, true);

            // Defensive override for window.hideall to never hide the Leaflet map in M3E native layout
            var origHideAll = window.hideall;
            window.hideall = function() {
                if (origHideAll) {
                    try { origHideAll(); } catch(e) {}
                }
                var mapEl = document.getElementById('map');
                if (mapEl) {
                    mapEl.style.visibility = 'visible';
                    mapEl.style.opacity = '1';
                    mapEl.style.display = 'block';
                }
            };

            // Polyfill smartphone button references to prevent window.show crashes
            if (!window.smartphone) window.smartphone = function() {};
            if (!window.smartphone.mapButton) {
                window.smartphone.mapButton = {
                    click: function() {
                        var el = document.getElementById('map');
                        if (el) { el.style.visibility = 'visible'; el.style.opacity = '1'; el.style.display = 'block'; }
                        if (window.map && window.map.invalidateSize) { window.map.invalidateSize(); }
                    }
                };
            }
            if (!window.smartphone.sideButton) {
                window.smartphone.sideButton = {
                    click: function() {
                        var el = document.getElementById('scrollwrapper');
                        if (el) el.style.display = 'block';
                    }
                };
            }

            // Safe window.show wrapper that restores map display safely
            var origShow = window.show;
            window.show = function(id) {
                try {
                    if (window.smartphone && !window.smartphone.mapButton) {
                        window.smartphone.mapButton = { click: function() { var el = document.getElementById('map'); if (el) { el.style.visibility = 'visible'; el.style.opacity = '1'; } } };
                    }
                    if (window.smartphone && !window.smartphone.sideButton) {
                        window.smartphone.sideButton = { click: function() { var el = document.getElementById('scrollwrapper'); if (el) el.style.display = 'block'; } };
                    }
                    if (origShow) origShow(id);
                } catch(e) {
                    console.warn('[Bridge] safe show exception: ' + e);
                }
                if (id === 'map') {
                    var m = document.getElementById('map');
                    if (m) {
                        m.style.visibility = 'visible';
                        m.style.opacity = '1';
                        m.style.display = 'block';
                    }
                    if (window.map && window.map.invalidateSize) {
                        window.map.invalidateSize();
                    }
                }
            };

            // Hook for automatic portal details extraction
            function setupPortalHook() {
                if (window.__portalHookInstalled) return;
                if (typeof window.addHook !== 'function') {
                    setTimeout(setupPortalHook, 300);
                    return;
                }
                window.__portalHookInstalled = true;

                function sendPortalDetails(guid, hookData) {
                    if (!guid) {
                        bridge.setPortalDetails(null, {});
                        return;
                    }
                    var p = window.portals && window.portals[guid];
                    var d = (hookData && hookData.portalDetails) || (window.portalDetail && window.portalDetail.get(guid)) || (p && p.options && p.options.data) || {};
                    var rangeObj = window.getPortalRange ? window.getPortalRange(d) : null;
                    var linksObj = window.getPortalLinks ? window.getPortalLinks(guid) : null;
                    var fieldsCount = window.getPortalFieldsCount ? window.getPortalFieldsCount(guid) : 0;
                    var energyVal = 0;
                    var maxEnergyVal = 0;
                    var resList = d.resonators || [];
                    if (resList.length) {
                        var energyCaps = [0, 1000, 1500, 2000, 2500, 3000, 4000, 5000, 6000];
                        resList.forEach(function(r) {
                            if (r) {
                                energyVal += (r.energy || 0);
                                maxEnergyVal += (energyCaps[r.level || 0] || 0);
                            }
                        });
                    }

                    var mappedResonators = [];
                    var rawRes = d.resonators || [];
                    for (var rIdx = 0; rIdx < rawRes.length; rIdx++) {
                        var rItem = rawRes[rIdx];
                        if (rItem) {
                            var rLvl = rItem.level || 0;
                            var rNrg = rItem.energy || 0;
                            var rMax = [0, 1000, 1500, 2000, 2500, 3000, 4000, 5000, 6000][rLvl] || 1000;
                            mappedResonators.push({
                                level: rLvl,
                                energy: rNrg,
                                owner: rItem.owner || '',
                                healthPct: Math.min(100, Math.round((rNrg * 100) / rMax))
                            });
                        } else {
                            mappedResonators.push(null);
                        }
                    }

                    var mappedMods = [];
                    var rawMods = d.mods || [];
                    for (var mIdx = 0; mIdx < rawMods.length; mIdx++) {
                        var mItem = rawMods[mIdx];
                        if (mItem) {
                            mappedMods.push({
                                name: mItem.name || '',
                                rarity: mItem.rarity || '',
                                owner: mItem.owner || '',
                                stats: mItem.stats || null
                            });
                        } else {
                            mappedMods.push(null);
                        }
                    }

                    var payload = {
                        guid: guid,
                        title: d.title || (p && p.options && p.options.data && p.options.data.title) || '',
                        image: d.image || (p && p.options && p.options.data && p.options.data.image) || '',
                        owner: d.owner || (p && p.options && p.options.data && p.options.data.owner) || '',
                        team: d.team || (p && p.options && p.options.data && p.options.data.team) || 'N',
                        level: d.level !== undefined ? d.level : ((p && p.options && p.options.data && p.options.data.level) || 0),
                        health: d.health !== undefined ? d.health : ((p && p.options && p.options.data && p.options.data.health) || 0),
                        latE6: d.latE6 || (p && p.options && p.options.data && p.options.data.latE6) || 0,
                        lngE6: d.lngE6 || (p && p.options && p.options.data && p.options.data.lngE6) || 0,
                        address: d.address || '',
                        rangeMeters: rangeObj ? Math.round(rangeObj.range) : 0,
                        linksIn: linksObj && linksObj.in ? linksObj.in.length : 0,
                        linksOut: linksObj && linksObj.out ? linksObj.out.length : 0,
                        fieldsCount: fieldsCount || 0,
                        energy: energyVal,
                        maxEnergy: maxEnergyVal,
                        apGain: window.portalApGain ? window.portalApGain(d) : 0,
                        historyVisited: !!(d.history && d.history.visited),
                        historyCaptured: !!(d.history && d.history.captured),
                        historyScoutControlled: !!(d.history && d.history.scoutControlled),
                        resCount: d.resCount !== undefined ? d.resCount : (d.resonators ? d.resonators.length : 0),
                        resonators: mappedResonators,
                        mods: mappedMods
                    };
                    bridge.setPortalDetails(guid, payload);
                }

                window.addHook('portalSelected', function(data) {
                    sendPortalDetails(data ? data.selectedPortalGuid : null, data);
                });
                window.addHook('portalDetailsUpdated', function(data) {
                    sendPortalDetails(data ? data.guid : window.selectedPortal, data);
                });
                window.addHook('iitcLoaded', function() {
                    if (bridge && bridge.bootFinished) {
                        bridge.bootFinished();
                    }
                });

                if (window.selectedPortal) {
                    sendPortalDetails(window.selectedPortal);
                }
                if (window.iitcLoaded || window.bootDone || (window.map && window.portals)) {
                    if (bridge && bridge.bootFinished) {
                        bridge.bootFinished();
                    }
                }

                if (window.chat && !window.chat.__m3e_hooked) {
                    window.chat.__m3e_hooked = true;
                    window.chat.show = function(name) {
                        if (window.chat.chooseTab) {
                            try { window.chat.chooseTab(name); } catch(e) {}
                        }
                    };
                    window.chat.toggle = function() {};
                }

                // Chat message bridge
                function extractChatMessages(channel) {
                    if (!window.chat) return;
                    var cData = channel === 'faction' ? window.chat._faction : (channel === 'alerts' ? window.chat._alerts : window.chat._public);
                    if (!cData || !cData.data) return;
                    var list = [];
                    var dMap = cData.data;
                    var keys = Object.keys(dMap);
                    for (var i = 0; i < keys.length; i++) {
                        var item = dMap[keys[i]];
                        if (!item) continue;
                        var time = item[0];
                        var sender = item[3] || (item[4] && item[4].player && item[4].player.name) || '';
                        var msgObj = item[4] || {};
                        var text = '';
                        var portal = null;
                        if (msgObj.markup) {
                            msgObj.markup.forEach(function(m) {
                                var type = m[0];
                                var data = m[1] || {};
                                if (type === 'TEXT') text += data.plain || '';
                                else if (type === 'PLAYER') text += (data.plain || '');
                                else if (type === 'PORTAL') {
                                    text += (data.name || data.plain || '');
                                    portal = {
                                        name: data.name || data.plain || '',
                                        lat: (data.latE6 || 0) / 1e6,
                                        lng: (data.lngE6 || 0) / 1e6
                                    };
                                }
                            });
                        } else {
                            text = item[2] ? item[2].replace(/<[^>]*>/g, '') : '';
                        }
                        var team = (msgObj.player && (msgObj.player.team === 2 ? 'ENLIGHTENED' : (msgObj.player.team === 1 ? 'RESISTANCE' : 'NEUTRAL'))) ||
                                   (item[2] && item[2].indexOf('class="nickname enl"') !== -1 ? 'ENLIGHTENED' : (item[2] && item[2].indexOf('class="nickname res"') !== -1 ? 'RESISTANCE' : 'NEUTRAL'));

                        var date = new Date(time);
                        var hours = ('0' + date.getHours()).slice(-2);
                        var mins = ('0' + date.getMinutes()).slice(-2);

                        list.push({
                            id: keys[i],
                            time: time,
                            formattedTime: hours + ':' + mins,
                            sender: sender,
                            team: team,
                            text: text.trim(),
                            isSecure: channel === 'faction',
                            portal: portal
                        });
                    }
                    list.sort(function(a, b) { return a.time - b.time; });
                    bridge.setChatMessages(channel, list.slice(-100));
                }

                window.__sendChat = extractChatMessages;
                window.addHook('factionChatDataAvailable', function() { extractChatMessages('faction'); });
                window.addHook('publicChatDataAvailable', function() { extractChatMessages('all'); });
                window.addHook('alertsChatDataAvailable', function() { extractChatMessages('alerts'); });

                // Native Map Entity Extraction and Synchronization
                var entitySyncTimer = null;
                function scheduleEntitySync() {
                    if (entitySyncTimer) clearTimeout(entitySyncTimer);
                    entitySyncTimer = setTimeout(extractAndSendEntities, 200);
                }

                function extractAndSendEntities() {
                    if (!bridge || !bridge.setEntities) return;
                    var portals = [];
                    if (window.portals) {
                        for (var guid in window.portals) {
                            var p = window.portals[guid];
                            if (!p) continue;
                            var ll = p.getLatLng ? p.getLatLng() : p._latlng;
                            if (!ll) continue;
                            var d = (p.options && p.options.data) || {};
                            var t = p.options.team !== undefined ? p.options.team : (d.team !== undefined ? d.team : 0);
                            var l = p.options.level !== undefined ? p.options.level : (d.level || 0);
                            var h = d.health !== undefined ? d.health : 100;
                            portals.push([guid, ll.lat, ll.lng, t, l, h]);
                        }
                    }

                    var links = [];
                    if (window.links) {
                        for (var lGuid in window.links) {
                            var l = window.links[lGuid];
                            if (!l) continue;
                            var lls = l.getLatLngs ? l.getLatLngs() : l._latlngs;
                            if (!lls || lls.length < 2) continue;
                            var lt = l.options.team !== undefined ? l.options.team : ((l.options.data && l.options.data.team) || 0);
                            links.push([lGuid, lt, lls[0].lat, lls[0].lng, lls[1].lat, lls[1].lng]);
                        }
                    }

                    var fields = [];
                    if (window.fields) {
                        for (var fGuid in window.fields) {
                            var f = window.fields[fGuid];
                            if (!f) continue;
                            var flls = f.getLatLngs ? f.getLatLngs() : f._latlngs;
                            if (!flls || flls.length < 3) continue;
                            var pts = Array.isArray(flls[0]) ? flls[0] : flls;
                            if (pts.length < 3) continue;
                            var ft = f.options.team !== undefined ? f.options.team : ((f.options.data && f.options.data.team) || 0);
                            fields.push([
                                fGuid,
                                ft,
                                [
                                    [pts[0].lat, pts[0].lng],
                                    [pts[1].lat, pts[1].lng],
                                    [pts[2].lat, pts[2].lng]
                                ]
                            ]);
                        }
                    }

                    bridge.setEntities(JSON.stringify({
                        portals: portals,
                        links: links,
                        fields: fields
                    }));
                }

                window.__syncEntitiesToNative = extractAndSendEntities;
                window.addHook('mapDataRefreshEnd', scheduleEntitySync);
                window.addHook('iitcLoaded', scheduleEntitySync);

                // Initial extraction if portals already loaded
                if (window.portals && Object.keys(window.portals).length > 0) {
                    scheduleEntitySync();
                }
            }
            setupPortalHook();

            // GM bridge transport
            window.__iitc_gm_bridge__ = {
                send: function(data) {
                    window.nsWebViewBridge.emit('gmBridgeRequest', typeof data === 'string' ? data : JSON.stringify(data));
                },
                onResponse: function(cb) {
                    window.addEventListener('gmBridgeResponse', function(e) { cb(e.detail); });
                }
            };

            window.dispatchEvent(new CustomEvent('ns-bridge-ready'));
        })();
        """.trimIndent()
    }

    fun buildPrimeParamsScript(versionName: String, showZoom: Boolean): String {
        return """
            window.__iitcVersionName = '$versionName';
            window.__iitcShowZoom = $showZoom;
            window.isApp = true;
        """.trimIndent()
    }

    fun buildCustomStylesScript(css: String): String {
        val jsonCss = escapeJsonString(css)
        return "window.nsWebViewBridge?.injectStyleSheet('iitcprimecss', $jsonCss, true);"
    }

    fun buildDemoModeScript(): String {
        return """
        (function () {
          if (window.__iitcDemoModeRan) return;
          window.__iitcDemoModeRan = true;

          if (location.hostname === '${UrlConfig.DEMO_INTEL_HOST}') {
            try {
              localStorage['pass-checking-intel-url'] = 'true';
              var baseMap = localStorage['iitc-base-map'];
              if (!baseMap || baseMap.indexOf('Google') === 0) {
                localStorage['iitc-base-map'] = 'CartoDB Dark Matter';
              }
            } catch (e) {}
          }

          function tryAttachDemoButton() {
            var container = document.getElementById('dashboard_container');
            if (!container) return false;
            if (document.getElementById('iitc-demo-button')) return true;

            var wrapper = document.createElement('div');
            wrapper.className = 'button unselectable';

            var link = document.createElement('a');
            link.id = 'iitc-demo-button';
            link.className = 'button_link';
            link.href = '#';
            link.textContent = 'Try demo (no sign-in required)';
            link.addEventListener('click', function (event) {
              event.preventDefault();
              if (window.app && window.app.openDemo) {
                window.app.openDemo();
              }
            });

            wrapper.appendChild(link);
            container.appendChild(wrapper);
            return true;
          }

          if (!tryAttachDemoButton()) {
            var tries = 0;
            var timer = setInterval(function() {
              tries++;
              if (tryAttachDemoButton() || tries > 30) {
                clearInterval(timer);
              }
            }, 300);
          }
        })();
        """.trimIndent()
    }

    fun buildDebugBridgeScript(): String {
        return """
        (function() {
          function debugBridge() {
            if (window.__debugBridgeInstalled) return;

            function formatArg(arg) {
              if (arg === null) return 'null';
              if (arg === undefined) return 'undefined';
              if (typeof arg === 'object') {
                try { return JSON.stringify(arg); } catch (e) { return String(arg); }
              }
              return String(arg);
            }

            var isAndroid = typeof androidBridge !== 'undefined';
            if (!isAndroid) {
              var originalConsole = {
                log: console.log,
                warn: console.warn,
                error: console.error,
                info: console.info,
                debug: console.debug
              };

              ['log', 'warn', 'error', 'info', 'debug'].forEach(function(level) {
                console[level] = function() {
                  var args = Array.from(arguments);
                  window.nsWebViewBridge?.emit('console:log', {
                    type: level,
                    message: args.map(function(arg) { return formatArg(arg); }).join(' '),
                    timestamp: new Date().toISOString(),
                    source: 'webview'
                  });
                  return originalConsole[level].apply(console, args);
                };
              });

              window.onerror = function(message, source, lineno, colno, error) {
                window.nsWebViewBridge?.emit('console:log', {
                  type: 'error',
                  message: (error && error.stack) ? error.stack : message,
                  timestamp: new Date().toISOString(),
                  source: source ? source + ':' + lineno : 'webview'
                });
              };
            }

            window.nsWebViewBridge?.on('console:execute', function(data) {
              try {
                console.log(">>> " + data.command);
                var result = eval(data.command);
                if (result !== undefined) {
                  console.log(formatArg(result));
                }
              } catch (error) {
                console.error(error.stack || error.message);
              }
            });

            window.__debugBridgeInstalled = true;
          }

          if (window.nsWebViewBridge) {
            debugBridge();
          } else {
            window.addEventListener('ns-bridge-ready', debugBridge);
          }
        })();
        """.trimIndent()
    }

    fun getGmApiCode(): String {
        return """
        (function() {
          document.addEventListener("DOMContentLoaded", function() {
            if (window.location.hostname === "intel.ingress.com") {
              window.onload = function() {};
              if (document.body) document.body.onload = function() {};
            }
          });

          var storageObj = {};
          function sendToBridge(data) {
            if (window.__iitc_gm_bridge__) {
              window.__iitc_gm_bridge__.send(data);
            }
          }

          window.__iitc_gm__ = function(info) {
            var dataKey = (info && info.data_key) || 'default';
            var meta = (info && info.meta) || {};

            return {
              info: { script: meta },
              _getValueSync: function(key, defaultValue) {
                var fullKey = '__gm__' + dataKey + ':' + key;
                try {
                  var v = localStorage.getItem(fullKey);
                  if (v !== null && v !== undefined) {
                    return JSON.parse(v);
                  }
                } catch(e) {}
                var mem = storageObj[fullKey];
                return mem !== undefined ? mem : defaultValue;
              },
              _setValueSync: function(key, value) {
                var fullKey = '__gm__' + dataKey + ':' + key;
                storageObj[fullKey] = value;
                try {
                  localStorage.setItem(fullKey, JSON.stringify(value));
                } catch(e) {}
                sendToBridge({ task_type: 'setValue', key: dataKey + ':' + key, value: value });
              },
              _deleteValueSync: function(key) {
                var fullKey = '__gm__' + dataKey + ':' + key;
                delete storageObj[fullKey];
                try {
                  localStorage.removeItem(fullKey);
                } catch(e) {}
                sendToBridge({ task_type: 'delValue', key: dataKey + ':' + key });
              },
              _listValuesSync: function() {
                var prefix = '__gm__' + dataKey + ':';
                var list = [];
                try {
                  for (var i = 0; i < localStorage.length; i++) {
                    var k = localStorage.key(i);
                    if (k && k.indexOf(prefix) === 0) {
                      list.push(k.substring(prefix.length));
                    }
                  }
                } catch(e) {}
                return list;
              },
              _addStyle: function(css) {
                try {
                  var style = document.createElement('style');
                  style.textContent = css;
                  (document.head || document.documentElement).appendChild(style);
                  return style;
                } catch(e) {
                  return null;
                }
              },
              xmlHttpRequest: function(details) {
                if (!details || typeof details !== 'object') return;
                try {
                  var xhr = new XMLHttpRequest();
                  xhr.open(details.method || 'GET', details.url, true);
                  if (details.headers) {
                    for (var h in details.headers) {
                      if (details.headers.hasOwnProperty(h)) {
                        xhr.setRequestHeader(h, details.headers[h]);
                      }
                    }
                  }
                  if (details.timeout) xhr.timeout = details.timeout;
                  xhr.onload = function() {
                    var resp = {
                      status: xhr.status,
                      statusText: xhr.statusText,
                      responseText: xhr.responseText,
                      readyState: xhr.readyState,
                      responseHeaders: xhr.getAllResponseHeaders()
                    };
                    if (typeof details.onload === 'function') details.onload(resp);
                    if (typeof details.onreadystatechange === 'function') details.onreadystatechange(resp);
                  };
                  xhr.onerror = function() {
                    if (typeof details.onerror === 'function') {
                      details.onerror({ status: xhr.status, statusText: xhr.statusText });
                    }
                  };
                  xhr.ontimeout = function() {
                    if (typeof details.ontimeout === 'function') details.ontimeout();
                  };
                  xhr.send(details.data || null);
                } catch(e) {
                  sendToBridge({ task_type: 'xmlHttpRequest', details: details });
                }
              }
            };
          };
        })();
        """.trimIndent()
    }

    fun wrapPluginCode(plugin: Plugin): String {
        val code = plugin.code ?: return ""
        val uid = plugin.uid
        val safeCode = code.replace("\u0000", "")
        val escapedUid = escapeJsonString(uid)
        val escapedName = escapeJsonString(plugin.name)
        val escapedVersion = escapeJsonString(plugin.version)
        val metaJson = """{"uid":$escapedUid,"name":$escapedName,"version":$escapedVersion}"""
        
        return """
        (function() {
            if (!window.__iitc_loaded_plugins) window.__iitc_loaded_plugins = {};
            if (window.__iitc_loaded_plugins[$escapedUid]) return;
            window.__iitc_loaded_plugins[$escapedUid] = true;

            try {
                ((GM) => {
                    const GM_info = GM.info;
                    const unsafeWindow = window;
                    const GM_getValue = (key, value) => GM._getValueSync(key, value);
                    const GM_setValue = (key, value) => GM._setValueSync(key, value);
                    const GM_deleteValue = (key) => GM._deleteValueSync(key);
                    const GM_listValues = () => GM._listValuesSync();
                    const GM_addStyle = (css) => GM._addStyle(css);
                    const GM_xmlhttpRequest = (details) => GM.xmlHttpRequest(details);

                    $safeCode
                })(__iitc_gm__({ data_key: $escapedUid, meta: $metaJson }));
            } catch (pluginError) {
                console.error('[IITC Plugin Error: ' + $escapedName + ']', pluginError);
            }
        })();
        void 0;
        //# sourceURL=${plugin.name.replace(" ", "_").replace("'", "")}.user.js
        """.trimIndent()
    }

    fun escapeJsonString(str: String): String {
        val sb = StringBuilder("\"")
        for (c in str) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    if (c.code in 0..0x1f) {
                        sb.append("\\u%04x".format(c.code))
                    } else {
                        sb.append(c)
                    }
                }
            }
        }
        sb.append("\"")
        return sb.toString()
    }
}
