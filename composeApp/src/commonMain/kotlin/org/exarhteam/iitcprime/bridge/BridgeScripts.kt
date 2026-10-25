package org.exarhteam.iitcprime.bridge

import org.exarhteam.iitcprime.model.Plugin

object BridgeScripts {

    fun buildBridgeScript(): String {
        return """
        (function() {
            if (window.nsWebViewBridge && window.app) return;
            
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
                    if (window.kmpJsBridge && window.kmpJsBridge.callNative) {
                        window.kmpJsBridge.callNative("bridge", JSON.stringify({ payload: payload }));
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
                'setFollowMode', 'setMapDarkMode', 'setPortalStatus', 'setMapStatus',
                'setProgress', 'setPermalink', 'addInternalHostname', 'saveFile',
                'reloadIITC', 'openDemo', 'dialogFocused', 'dialogOpened'
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
                setMapStatus: ['portalLevels', 'mapStatus', 'requests'],
                setProgress: ['progress'],
                setPermalink: ['href'],
                addInternalHostname: ['domain'],
                saveFile: ['filename', 'dataType', 'content'],
                reloadIITC: ['clearCache'],
                openDemo: [],
                dialogFocused: ['id'],
                dialogOpened: ['id', 'open']
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
        """.trimIndent()
    }

    fun buildCustomStylesScript(css: String): String {
        val jsonCss = escapeJsonString(css)
        return "window.nsWebViewBridge?.injectStyleSheet('iitcprimecss', $jsonCss, false);"
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

          var container = document.getElementById('dashboard_container');
          if (!container || document.getElementById('iitc-demo-button')) return;

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
            var dataKey = info.data_key;
            var meta = info.meta || {};

            return {
              info: { script: meta },
              _getValueSync: function(key, defaultValue) {
                var stored = storageObj[dataKey + ':' + key];
                return stored !== undefined ? stored : defaultValue;
              },
              _setValueSync: function(key, value) {
                storageObj[dataKey + ':' + key] = value;
                sendToBridge({ task_type: 'setValue', key: dataKey + ':' + key, value: value });
              },
              xmlHttpRequest: function(details) {
                sendToBridge({ task_type: 'xmlHttpRequest', details: details });
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
        val metaJson = """{"uid":"${escapeJsonString(uid)}","name":"${escapeJsonString(plugin.name)}","version":"${escapeJsonString(plugin.version)}"}"""
        
        return """
        ((GM) => {
            const GM_info = GM.info;
            const unsafeWindow = window;
            const GM_getValue = (key, value) => GM._getValueSync(key, value);
            const GM_setValue = (key, value) => GM._setValueSync(key, value);
            const GM_xmlhttpRequest = (details) => GM.xmlHttpRequest(details);

            $safeCode
        })(__iitc_gm__({ data_key: "${escapeJsonString(uid)}", meta: $metaJson }));
        void 0;
        //# sourceURL=${plugin.name.replace(" ", "_")}.user.js
        """.trimIndent()
    }

    private fun escapeJsonString(str: String): String {
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
