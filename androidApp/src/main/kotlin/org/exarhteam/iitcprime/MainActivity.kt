package org.exarhteam.iitcprime

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.exarhteam.iitcprime.manager.IITCManager
import org.exarhteam.iitcprime.platform.PlatformContext
import org.exarhteam.iitcprime.storage.AppStorage
import org.exarhteam.iitcprime.theme.IITCTheme
import org.exarhteam.iitcprime.ui.main.MainScreen
import org.exarhteam.iitcprime.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private lateinit var storage: AppStorage
    private lateinit var manager: IITCManager
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        PlatformContext.setActivity(this)

        storage = AppStorage()
        manager = IITCManager(storage)
        viewModel = MainViewModel(storage, manager)

        // Seed fallback core if needed
        manager.seedFallbackCoreIfNeeded()

        // Handle back press navigation
        onBackPressedDispatcher.addCallback(this) {
            if (!viewModel.navigateBack()) {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

        handleIntent(intent)

        setContent {
            IITCTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        val dataUri = intent.data
        if (dataUri != null) {
            val scheme = dataUri.scheme
            val urlString = dataUri.toString()

            // File or Content URI (e.g. .user.js)
            if (scheme == "file" || scheme == "content") {
                try {
                    contentResolver.openInputStream(dataUri)?.use { stream ->
                        val bytes = stream.readBytes()
                        val code = bytes.decodeToString()
                        val plugin = manager.installUserPlugin(code)
                        if (plugin != null) {
                            viewModel.navigateTo("plugins")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return
            }

            // Web URL or iitc:// URL
            viewModel.processDeepLink(urlString)
        }
    }

    override fun onResume() {
        super.onResume()
        PlatformContext.setActivity(this)
        viewModel.checkClipboard()
    }

    override fun onPause() {
        super.onPause()
        if (PlatformContext.activity == this) {
            PlatformContext.setActivity(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (PlatformContext.activity == this) {
            PlatformContext.setActivity(null)
        }
    }
}
