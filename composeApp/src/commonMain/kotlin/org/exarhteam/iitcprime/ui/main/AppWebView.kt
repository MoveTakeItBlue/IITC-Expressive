package org.exarhteam.iitcprime.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.web.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.exarhteam.iitcprime.bridge.BridgeScripts
import org.exarhteam.iitcprime.bridge.UrlConfig
import org.exarhteam.iitcprime.platform.PlatformUtils
import org.exarhteam.iitcprime.viewmodel.MainViewModel

@Composable
fun AppWebView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUrl by viewModel.currentUrl.collectAsState()
    val desktopMode by viewModel.desktopMode.collectAsState()
    val zoomControl by viewModel.zoomControl.collectAsState()
    val reloadTrigger by viewModel.reloadTrigger.collectAsState()

    val webViewState = rememberWebViewState(url = UrlConfig.addViewportParam(currentUrl, desktopMode)).apply {
        webSettings.apply {
            isJavaScriptEnabled = true
            supportZoom = true
            customUserAgentString = if (desktopMode) {
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            } else null
            androidWebSettings.domStorageEnabled = true
            androidWebSettings.allowFileAccess = true
        }
    }
    val webViewNavigator = rememberWebViewNavigator()
    val jsBridge = rememberWebViewJsBridge(webViewNavigator)

    // Bridge message handler
    DisposableEffect(jsBridge) {
        val handler = object : IJsMessageHandler {
            override fun methodName(): String = "bridge"
            override fun handle(
                message: JsMessage,
                navigator: WebViewNavigator?,
                callback: (String) -> Unit
            ) {
                try {
                    val rawParams = message.params
                    val payload = if (rawParams.startsWith("{")) {
                        try {
                            val jsonObj = Json.parseToJsonElement(rawParams).jsonObject
                            jsonObj["payload"]?.jsonPrimitive?.content ?: rawParams
                        } catch (_: Exception) {
                            rawParams
                        }
                    } else rawParams

                    val parsed = Json.parseToJsonElement(payload)
                    if (parsed is JsonArray && parsed.size >= 2) {
                        val event = parsed[0].jsonPrimitive.content
                        val data = parsed[1]
                        if (event == "JSBridge" && data is JsonArray && data.size >= 2) {
                            val innerEvent = data[0].jsonPrimitive.content
                            val innerData = data[1]
                            viewModel.handleBridgeMessage(innerEvent, innerData)
                        } else {
                            viewModel.handleBridgeMessage(event, data)
                        }
                    }
                } catch (e: Exception) {
                    viewModel.addLog("debug", "Error parsing bridge message: ${e.message}", "WebViewBridge")
                }
            }
        }
        jsBridge.register(handler)
        onDispose {
            jsBridge.unregister(handler)
        }
    }

    // Configure WebSettings
    LaunchedEffect(webViewState, desktopMode) {
        webViewState.webSettings.apply {
            isJavaScriptEnabled = true
            supportZoom = true
            customUserAgentString = if (desktopMode) {
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            } else null
            androidWebSettings.domStorageEnabled = true
        }
    }

    // Handle reload trigger
    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger > 0) {
            webViewNavigator.reload()
        }
    }

    // Handle evaluate JavaScript requests from ViewModel
    LaunchedEffect(Unit) {
        for (script in viewModel.jsChannel) {
            webViewNavigator.evaluateJavaScript(script)
        }
    }

    // Watch loading state
    val loadingState = webViewState.loadingState
    LaunchedEffect(loadingState) {
        when (loadingState) {
            is LoadingState.Loading -> {
                viewModel.setProgress(loadingState.progress)
            }
            is LoadingState.Finished -> {
                viewModel.setProgress(1f)

                // Inject bridge scripts and styles
                val bridgeScript = BridgeScripts.buildBridgeScript()
                webViewNavigator.evaluateJavaScript(bridgeScript)

                val primeParams = BridgeScripts.buildPrimeParamsScript("1.2.0", showZoom = zoomControl)
                webViewNavigator.evaluateJavaScript(primeParams)

                val debugScript = BridgeScripts.buildDebugBridgeScript()
                webViewNavigator.evaluateJavaScript(debugScript)

                // Inject demo mode script for both login page (adds Try Demo button) and demo server
                val demoScript = BridgeScripts.buildDemoModeScript()
                webViewNavigator.evaluateJavaScript(demoScript)

                // Inject enabled plugins (IITC Core + plugins)
                val scripts = viewModel.manager.getEnabledPluginScripts()
                for (script in scripts) {
                    webViewNavigator.evaluateJavaScript(script)
                }

                // Ensure bootFinished is signaled if IITC has completed boot
                webViewNavigator.evaluateJavaScript("""
                    if (window.iitcLoaded || window.bootDone || (window.map && window.portals)) {
                        window.app?.bootFinished?.();
                    }
                """.trimIndent())
            }
            else -> {}
        }
    }

    WebView(
        state = webViewState,
        navigator = webViewNavigator,
        webViewJsBridge = jsBridge,
        modifier = modifier.fillMaxSize(),
        captureBackPresses = false,
        onCreated = { nativeWebView ->
            PlatformUtils.configureNativeWebView(nativeWebView)
        }
    )
}
