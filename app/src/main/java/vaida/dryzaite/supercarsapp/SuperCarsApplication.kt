package vaida.dryzaite.supercarsapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SuperCarsApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        // Timber for Logging
        Timber.plant(Timber.DebugTree())
    }
}