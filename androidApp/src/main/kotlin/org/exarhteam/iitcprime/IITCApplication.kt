package org.exarhteam.iitcprime

import android.app.Application
import org.exarhteam.iitcprime.platform.PlatformContext

class IITCApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformContext.init(this)
    }
}
