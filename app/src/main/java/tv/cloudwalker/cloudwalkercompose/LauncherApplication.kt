package tv.cloudwalker.cloudwalkercompose

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LauncherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}