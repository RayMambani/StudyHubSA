package com.studyhub.sa

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.studyhub.sa.util.LocaleHelper
import com.studyhub.sa.util.ThemeHelper

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LocaleHelper.applyStoredLocale(this)
        ThemeHelper.applyStoredTheme(this)
        createNotificationChannel()
        registerActivityLifecycleCallbacks(EdgeToEdgeInsets())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}

/** Android 15+ draws apps edge-to-edge and API 36 cannot opt out. This pads every
 * screen by the status bar, navigation bar, camera cutout and keyboard so nothing
 * sits underneath them. */
private class EdgeToEdgeInsets : Application.ActivityLifecycleCallbacks {
    override fun onActivityStarted(activity: Activity) {
        val content = activity.findViewById<View>(android.R.id.content) ?: return
        ViewCompat.setOnApplyWindowInsetsListener(content) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(content)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}