package org.yac.llamarangers

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LlamaRangersApp : Application() {

    @Inject
    lateinit var appEnvironment: AppEnvironment

    override fun onCreate() {
        super.onCreate()
        appEnvironment.initialize()
    }
}
