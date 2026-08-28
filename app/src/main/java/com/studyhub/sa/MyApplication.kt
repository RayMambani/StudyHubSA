package com.studyhub.sa

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.studyhub.sa.util.LocaleHelper
import com.studyhub.sa.util.ThemeHelper

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Restore the student's saved language (English / isiZulu / Tshivenda) on process start.
        LocaleHelper.applyStoredLocale(this)
        ThemeHelper.applyStoredTheme(this)
        createNotificationChannel()
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
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
