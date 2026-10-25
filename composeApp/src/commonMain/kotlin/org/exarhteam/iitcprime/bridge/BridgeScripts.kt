package org.exarhteam.iitcprime.bridge

import org.exarhteam.iitcprime.model.Plugin

object BridgeScripts {

    fun buildBridgeScript(): String {
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

            // Permanently hide legacy HTML chat/sidebar controls so the native M3E UI and Leaflet map are completely unobstructed
            (function() {
                function applyHideChat() {
                    try {
                        var id = 'm3e-hide-legacy-chat';
                        var el = document.getElementById(id);
                        if (!el) {
                            el = document.createElement('style');
                            el.id = id;
                            (document.head || document.documentElement).appendChild(el);
                        }
                        el.textContent = '#map { visibility: visible !important; opacity: 1 !important; display: block !important; } #chat, #chatcontrols, #chatinput, #scrollwrapper { display: none !important; visibility: hidden !important; height: 0 !important; max-height: 0 !important; pointer-events: none !important; }';
                    } catch(e) {}
                }
                applyHideChat();
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', applyHideChat);
                }
                window.addEventListener('load', applyHideChat);
                try {
                    var obs = new MutationObserver(function() {
                        if (!document.getElementById('m3e-hide-legacy-chat')) applyHideChat();
                    });
                    obs.observe(document.documentElement, { childList: true, subtree: true });
                } catch(e) {}
            })();

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
            window.android = bridge;

            // Hide legacy HTML chat/sidebar controls completely and ensure map is always visible
            bridge.injectStyleSheet('m3e-hide-legacy-chat', '#map { visibility: visible !important; opacity: 1 !important; display: block !important; } #chat, #chatcontrols, #chatinput, #scrollwrapper { display: none !important; visibility: hidden !important; height: 0 !important; max-height: 0 !important; pointer-events: none !important; }', true);

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
                        var us = document.getElementById('updatestatus');
                        if (us) us.style.display = 'block';
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
                    var us = document.getElementById('updatestatus');
                    if (us) us.style.display = 'block';
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
