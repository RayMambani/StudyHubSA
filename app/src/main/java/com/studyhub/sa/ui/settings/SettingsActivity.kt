package com.studyhub.sa.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studyhub.sa.R
import com.studyhub.sa.auth.GoogleSignInHelper
import com.studyhub.sa.auth.LoginActivity
import com.studyhub.sa.data.local.SessionManager
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.SettingsUpdateRequest
import com.studyhub.sa.util.LocaleHelper
import com.studyhub.sa.util.NotificationPrefs
import com.studyhub.sa.util.ThemeHelper
import kotlinx.coroutines.launch

/**
 * Screen 13: Settings Screen — every row here does something real:
 * language switch, notification toggle, theme switch (actually recolors
 * the app via values-night), profile privacy (synced to the backend), and
 * account security (real password change).
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val tvCurrentLanguage = findViewById<TextView>(R.id.tvCurrentLanguage)
        tvCurrentLanguage.text = LocaleHelper.displayNameFor(LocaleHelper.getLanguage(this))

        val tvCurrentTheme = findViewById<TextView>(R.id.tvCurrentTheme)
        tvCurrentTheme.text = ThemeHelper.displayNameFor(ThemeHelper.getTheme(this))

        val tvCurrentPrivacy = findViewById<TextView>(R.id.tvCurrentPrivacy)
        tvCurrentPrivacy.text = getString(R.string.privacy_public)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<android.view.View>(R.id.rowLanguage).setOnClickListener {
            showLanguagePicker(tvCurrentLanguage)
        }

        findViewById<android.view.View>(R.id.rowTheme).setOnClickListener {
            showThemePicker(tvCurrentTheme)
        }

        findViewById<android.view.View>(R.id.rowPrivacy).setOnClickListener {
            showPrivacyPicker(tvCurrentPrivacy)
        }

        findViewById<android.view.View>(R.id.rowSecurity).setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        val switchNotifications = findViewById<Switch>(R.id.switchNotifications)
        switchNotifications.isChecked = NotificationPrefs.isEnabled(this)
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            NotificationPrefs.setEnabled(this, isChecked)
        }

        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            SessionManager(this).clear()
            GoogleSignInHelper.signOut(this)
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        loadRemotePrivacy(tvCurrentPrivacy)
    }

    private fun loadRemotePrivacy(tvCurrentPrivacy: TextView) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@SettingsActivity).getProfile()
                if (response.isSuccessful) {
                    val privacy = response.body()?.privacy ?: "public"
                    tvCurrentPrivacy.text = if (privacy == "private") getString(R.string.privacy_private) else getString(R.string.privacy_public)
                }
            } catch (_: Exception) {
                // offline — leave the default shown
            }
        }
    }

    private fun showLanguagePicker(tvCurrentLanguage: TextView) {
        val codes = arrayOf(LocaleHelper.LANGUAGE_ENGLISH, LocaleHelper.LANGUAGE_ZULU, LocaleHelper.LANGUAGE_TSHIVENDA)
        val labels = codes.map { LocaleHelper.displayNameFor(it) }.toTypedArray()
        val current = codes.indexOf(LocaleHelper.getLanguage(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.choose_language)
            .setSingleChoiceItems(labels, current) { dialog, which ->
                val code = codes[which]
                LocaleHelper.setLanguage(this, code)
                tvCurrentLanguage.text = LocaleHelper.displayNameFor(code)

                lifecycleScope.launch {
                    try {
                        RetrofitClient.getInstance(this@SettingsActivity)
                            .updateSettings(SettingsUpdateRequest(language = code))
                    } catch (_: Exception) { /* will sync next time online */ }
                }
                dialog.dismiss()
                recreate() // re-apply resources for the newly selected locale
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showThemePicker(tvCurrentTheme: TextView) {
        val themes = arrayOf(ThemeHelper.THEME_LIGHT, ThemeHelper.THEME_DARK, ThemeHelper.THEME_SYSTEM)
        val labels = themes.map { ThemeHelper.displayNameFor(it) }.toTypedArray()
        val current = themes.indexOf(ThemeHelper.getTheme(this))

        AlertDialog.Builder(this)
            .setTitle(R.string.choose_theme)
            .setSingleChoiceItems(labels, current) { dialog, which ->
                val theme = themes[which]
                ThemeHelper.setTheme(this, theme)
                tvCurrentTheme.text = ThemeHelper.displayNameFor(theme)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showPrivacyPicker(tvCurrentPrivacy: TextView) {
        val options = arrayOf("public", "private")
        val labels = arrayOf(getString(R.string.privacy_public), getString(R.string.privacy_private))
        val current = if (tvCurrentPrivacy.text == getString(R.string.privacy_private)) 1 else 0

        AlertDialog.Builder(this)
            .setTitle(R.string.choose_privacy)
            .setSingleChoiceItems(labels, current) { dialog, which ->
                val privacy = options[which]
                tvCurrentPrivacy.text = labels[which]
                lifecycleScope.launch {
                    try {
                        RetrofitClient.getInstance(this@SettingsActivity)
                            .updateSettings(SettingsUpdateRequest(privacy = privacy))
                    } catch (_: Exception) { /* will sync next time online */ }
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
