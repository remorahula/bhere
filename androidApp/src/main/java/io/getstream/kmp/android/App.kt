package io.getstream.kmp.android

import android.app.Application
import io.getstream.kmp.platform.AndroidPlatform

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidPlatform.appContext = applicationContext
    }
}
