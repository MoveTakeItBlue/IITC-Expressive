package io.github.movetakeitblue.iitcexpressive

import android.app.Application
import io.github.movetakeitblue.iitcexpressive.platform.PlatformContext

class IITCApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformContext.init(this)
    }
}
