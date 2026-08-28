package com.studyhub.sa.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Multi-language support: English (default), isiZulu ("zu") and Tshivenda ("ve").
 * Uses AppCompatDelegate's per-app language API so the change applies
 * immediately without restarting the whole app, and persists across launches.
 */
object LocaleHelper {

    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_ZULU = "zu"
    const val LANGUAGE_TSHIVENDA = "ve"

    private const val PREFS_NAME = "studyhub_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setLanguage(context: Context, languageCode: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, languageCode).apply()
        applyLocale(languageCode)
    }

    fun getLanguage(context: Context): String =
        prefs(context).getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH

    /** Call from Application.onCreate() so the saved language is applied on cold start. */
    fun applyStoredLocale(context: Context) {
        applyLocale(getLanguage(context))
    }

    private fun applyLocale(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun displayNameFor(languageCode: String): String = when (languageCode) {
        LANGUAGE_ZULU -> "isiZulu"
        LANGUAGE_TSHIVENDA -> "Tshivenda"
        else -> "English"
    }
}
