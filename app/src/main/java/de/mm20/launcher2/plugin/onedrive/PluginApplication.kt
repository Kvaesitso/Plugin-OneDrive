package de.mm20.launcher2.plugin.onedrive

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PluginApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PluginApplication)
            modules(module)
        }
    }
}