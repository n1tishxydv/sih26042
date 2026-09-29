package org.sih26042.coteacher

import android.app.Application
import org.sih26042.coteacher.di.AppContainer

class SIHApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        container.modelLifecycleManager.onTrimMemory(level)
    }
}
