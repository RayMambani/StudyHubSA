package com.studyhub.sa.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Real theme switching (Settings > Theme). Backed by AppCompatDelegate's
 * night mode, so it actually recolors the whole app via the values-night/
 * resource overrides — not just a label that does nothing.
 */
object ThemeHelper {

    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"

    private const val PREFS_NAME = "studyhub_prefs"
    private const val KEY_THEME = "selected_theme"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setTheme(context: Context, theme: String) {
        prefs(context).edit().putString(KEY_THEME, theme).apply()
        applyTheme(theme)
    }

    fun getTheme(context: Context): String =
        prefs(context).getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM

    /** Call from Application.onCreate() so the saved theme applies on cold start. */
    fun applyStoredTheme(context: Context) {
        applyTheme(getTheme(context))
    }

    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun displayNameFor(theme: String): String = when (theme) {
        THEME_LIGHT -> "Light"
        THEME_DARK -> "Dark"
        else -> "System default"
    }
}
