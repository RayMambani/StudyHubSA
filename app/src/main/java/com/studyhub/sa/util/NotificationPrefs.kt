package com.studyhub.sa.util

import android.content.Context

/** Backs the Settings > Notifications switch — checked by NotificationHelper
 * before showing any system notification, so turning it off actually stops them. */
object NotificationPrefs {
    private const val PREFS_NAME = "studyhub_prefs"
    private const val KEY_ENABLED = "notifications_enabled"

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, true)
}
