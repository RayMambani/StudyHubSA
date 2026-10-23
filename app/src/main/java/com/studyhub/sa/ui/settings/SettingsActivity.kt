package com.studyhub.sa.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.studyhub.sa.R
import com.studyhub.sa.auth.GoogleSignInHelper
import com.studyhub.sa.auth.LoginActivity
import com.studyhub.sa.data.local.AppDatabase
import com.studyhub.sa.data.local.SessionManager
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.DeleteAccountRequest
import com.studyhub.sa.data.remote.dto.SettingsUpdateRequest
import com.studyhub.sa.util.LocaleHelper
import com.studyhub.sa.util.NotificationPrefs
import com.studyhub.sa.util.ThemeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Screen 13: Settings. Every row does something real, including permanent account deletion. */
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
        findViewById<android.view.View>(R.id.rowLanguage).setOnClickListener { showLanguagePicker(tvCurrentLanguage) }
        findViewById<android.view.View>(R.id.rowTheme).setOnClickListener { showThemePicker(tvCurrentTheme) }
        findViewById<android.view.View>(R.id.rowPrivacy).setOnClickListener { showPrivacyPicker(tvCurrentPrivacy) }
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
            goToLogin()
        }

        addDeleteAccountRow()
        loadRemotePrivacy(tvCurrentPrivacy)
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    /** Adds a "Delete account" link directly under "Log out". */
    private fun addDeleteAccountRow() {
        val logout = findViewById<TextView>(R.id.btnLogout)
        val parent = logout.parent as ViewGroup
        val pad = (14 * resources.displayMetrics.density).toInt()
        val delete = TextView(this).apply {
            text = "Delete account"
            setTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.danger))
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, pad, 0, pad)
            setOnClickListener { confirmDeleteAccount() }
        }
        parent.addView(
            delete,
            parent.indexOfChild(logout) + 1,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
    }

    private fun confirmDeleteAccount() {
        val input = EditText(this).apply {
            hint = "Password (leave blank if you signed in with Google)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val pad = (20 * resources.displayMetrics.density).toInt()
        val container = LinearLayout(this).apply {
            setPadding(pad, pad / 2, pad, 0)
            addView(input, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        AlertDialog.Builder(this)
            .setTitle("Delete your account?")
            .setMessage("This permanently deletes your profile, uploads, messages, notes and events. It cannot be undone.")
            .setView(container)
            .setPositiveButton("Delete") { _, _ -> deleteAccount(input.text.toString()) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteAccount(password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@SettingsActivity)
                    .deleteAccount(DeleteAccountRequest(password.ifEmpty { null }))
                if (response.isSuccessful) {
                    withContext(Dispatchers.IO) { AppDatabase.getInstance(this@SettingsActivity).clearAllTables() }
                    SessionManager(this@SettingsActivity).clear()
                    GoogleSignInHelper.signOut(this@SettingsActivity)
                    toast("Your account has been deleted")
                    goToLogin()
                } else {
                    val raw = response.errorBody()?.string()
                    val msg = try { org.json.JSONObject(raw ?: "").optString("error", "") } catch (e: Exception) { "" }
                    toast(if (msg.isNotEmpty()) msg else "Could not delete account (${response.code()})")
                }
            } catch (e: Exception) {
                toast("Could not reach the server - check your connection")
            }
        }
    }

    private fun loadRemotePrivacy(tvCurrentPrivacy: TextView) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@SettingsActivity).getProfile()
                if (response.isSuccessful) {
                    val privacy = response.body()?.privacy ?: "public"
                    tvCurrentPrivacy.text = if (privacy == "private") getString(R.string.privacy_private) else getString(R.string.privacy_public)
                }
            } catch (_: Exception) { }
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
                    } catch (_: Exception) { }
                }
                dialog.dismiss()
                recreate()
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
                tvCurrentPrivacy.text = labels[which]
                lifecycleScope.launch {
                    try {
                        RetrofitClient.getInstance(this@SettingsActivity)
                            .updateSettings(SettingsUpdateRequest(privacy = options[which]))
                    } catch (_: Exception) { }
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}