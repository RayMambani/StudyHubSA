package com.studyhub.sa.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.studyhub.sa.BuildConfig
import com.studyhub.sa.R
import com.studyhub.sa.data.local.SessionManager
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.ui.common.BottomNavHelper
import com.studyhub.sa.ui.offline.OfflineNotesActivity
import com.studyhub.sa.ui.settings.SettingsActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

/**
 * Screen 12: Profile Screen.
 * Stats and badges are real — pulled from GET /api/users/me, which computes
 * them from actual uploads, group memberships, and login streak, so a badge
 * only shows once the student has genuinely earned it.
 */
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val session = SessionManager(this)
        val fullName = session.fullName ?: "Student"
        val initials = fullName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")

        findViewById<TextView>(R.id.tvInitials).text = initials.uppercase()
        findViewById<TextView>(R.id.tvName).text = fullName
        findViewById<TextView>(R.id.tvMeta).text = session.email ?: ""

        findViewById<android.view.View>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<android.view.View>(R.id.rowSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<android.view.View>(R.id.rowOfflineNotes).setOnClickListener {
            startActivity(Intent(this, OfflineNotesActivity::class.java))
        }

        BottomNavHelper.setup(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_profile)

        loadRealStats()
    }

    override fun onResume() {
        super.onResume()
        loadRealStats() // badges/streak may have changed since a resource was uploaded, etc.
    }

    private fun loadRealStats() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@ProfileActivity).getProfile()
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        findViewById<TextView>(R.id.tvUploadsCount).text = (user.uploadsCount ?: 0).toString()
                        findViewById<TextView>(R.id.tvGroupsCount).text = (user.groupsCount ?: 0).toString()
                        findViewById<TextView>(R.id.tvPointsCount).text = (user.points ?: 0).toString()
                        renderBadges(user.badges.orEmpty())

                        user.profileImage?.let { path ->
                            val fullUrl = BuildConfig.BASE_URL.trimEnd('/') + path
                            val ivAvatar = findViewById<CircleImageView>(R.id.ivAvatar)
                            ivAvatar.visibility = android.view.View.VISIBLE
                            Glide.with(this@ProfileActivity).load(fullUrl).into(ivAvatar)
                        }
                    }
                }
            } catch (e: Exception) {
                // Offline — leave whatever was last shown; stats will refresh next time online.
            }
        }
    }

    private fun renderBadges(earned: List<String>) {
        val grid = findViewById<GridLayout>(R.id.badgesGrid)
        grid.removeAllViews()

        if (earned.isEmpty()) {
            val empty = TextView(this).apply {
                text = getString(R.string.no_badges_yet)
                setTextColor(getColorCompat(R.color.muted))
                textSize = 12.5f
            }
            grid.addView(empty)
            return
        }

        earned.forEach { name ->
            val badgeView = layoutInflater.inflate(R.layout.item_badge, grid, false)
            badgeView.findViewById<TextView>(R.id.tvBadgeName).text = name
            grid.addView(badgeView)
        }
    }

    private fun getColorCompat(resId: Int) = androidx.core.content.ContextCompat.getColor(this, resId)
}
