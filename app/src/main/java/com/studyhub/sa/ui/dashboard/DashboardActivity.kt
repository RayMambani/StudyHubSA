package com.studyhub.sa.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.studyhub.sa.R
import com.studyhub.sa.data.local.SessionManager
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.EventDto
import com.studyhub.sa.data.remote.dto.StudyGroupDto
import com.studyhub.sa.ui.common.BottomNavHelper
import com.studyhub.sa.ui.groups.GroupChatActivity
import com.studyhub.sa.ui.notifications.NotificationsActivity
import kotlinx.coroutines.launch

/** Screen 4: Dashboard Screen — the app's central home hub, backed by real data. */
class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val session = SessionManager(this)
        findViewById<TextView>(R.id.tvGreetingName).text =
            "${session.fullName?.substringBefore(' ') ?: "Student"} 👋"

        findViewById<ImageView>(R.id.btnNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<android.view.View>(R.id.cardAiAssist).setOnClickListener {
            startActivity(Intent(this, com.studyhub.sa.ui.ai.AiAssistActivity::class.java))
        }

        BottomNavHelper.setup(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_home)

        loadProfileStats()
        loadUpcomingEvents()
        loadGroupsAndResources()
    }

    override fun onResume() {
        super.onResume()
        loadProfileStats()
        loadUpcomingEvents()
        loadGroupsAndResources()
    }

    private fun loadProfileStats() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@DashboardActivity).getProfile()
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        val streak = user.streak ?: 0
                        val fire = when {
                            streak >= 7 -> "🔥🔥🔥"
                            streak >= 3 -> "🔥🔥"
                            streak >= 1 -> "🔥"
                            else -> ""
                        }
                        findViewById<TextView>(R.id.tvStreakDays).text =
                            "$streak day${if (streak == 1) "" else "s"} $fire".trim()
                        val badges = user.badges.orEmpty()
                        findViewById<TextView>(R.id.tvTopBadge).text =
                            if (badges.isNotEmpty()) "🔥 ${badges.last()}" else "Log in daily to build your streak"
                    }
                }
            } catch (e: Exception) {
                // Offline — leave whatever was last shown.
            }
        }
    }

    private fun loadUpcomingEvents() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@DashboardActivity).getEvents()
                if (response.isSuccessful) {
                    populateUpcoming(response.body().orEmpty().take(2))
                }
            } catch (e: Exception) {
                // Offline — leave whatever was last shown.
            }
        }
    }

    private fun populateUpcoming(events: List<EventDto>) {
        val container = findViewById<LinearLayout>(R.id.upcomingContainer)
        container.removeAllViews()

        if (events.isEmpty()) {
            val empty = TextView(this).apply {
                text = getString(R.string.no_upcoming_events)
                setTextColor(ContextCompat.getColor(this@DashboardActivity, R.color.muted))
                textSize = 12.5f
            }
            container.addView(empty)
            return
        }

        events.forEach { e ->
            val colorRes = when (e.type) {
                "test" -> R.color.primary
                "assignment" -> R.color.gold
                else -> R.color.green
            }
            val row = layoutInflater.inflate(R.layout.item_upcoming_event, container, false)
            row.findViewById<TextView>(R.id.tvEventTitle).text = e.title
            row.findViewById<TextView>(R.id.tvEventTime).text =
                e.event_date + (e.event_time?.let { ", $it" } ?: "")
            row.findViewById<android.view.View>(R.id.eventColorBar)
                .setBackgroundColor(ContextCompat.getColor(this, colorRes))
            container.addView(row)
        }
    }

    private fun loadGroupsAndResources() {
        lifecycleScope.launch {
            try {
                val groupsResponse = RetrofitClient.getInstance(this@DashboardActivity).getGroups()
                if (groupsResponse.isSuccessful) {
                    val groups = groupsResponse.body().orEmpty()
                    populateGroups(groups)
                    val distinctModules = groups.mapNotNull { it.module }.filter { it.isNotEmpty() }.distinct().size
                    findViewById<TextView>(R.id.tvModulesCount).text = "$distinctModules active"
                }
            } catch (e: Exception) {
                // Offline — leave whatever was last shown.
            }

            try {
                val resourcesResponse = RetrofitClient.getInstance(this@DashboardActivity).getResources()
                if (resourcesResponse.isSuccessful) {
                    findViewById<TextView>(R.id.tvResourcesCount).text =
                        "${resourcesResponse.body().orEmpty().size} shared"
                }
            } catch (e: Exception) {
                // Offline — leave whatever was last shown.
            }
        }
    }

    private fun populateGroups(groups: List<StudyGroupDto>) {
        val container = findViewById<LinearLayout>(R.id.groupsContainer)
        container.removeAllViews()
        val palette = listOf("#1E3A5F", "#3FA34D", "#E0A526")

        groups.take(6).forEachIndexed { index, g ->
            val card = layoutInflater.inflate(R.layout.item_group_preview, container, false)
            card.findViewById<TextView>(R.id.tvGroupInitial).apply {
                text = g.name.take(1)
                background.setTint(Color.parseColor(palette[index % palette.size]))
            }
            card.findViewById<TextView>(R.id.tvGroupName).text = g.name
            card.findViewById<TextView>(R.id.tvGroupMembers).text = "${g.member_count} members"
            card.setOnClickListener {
                startActivity(Intent(this, GroupChatActivity::class.java).apply {
                    putExtra(GroupChatActivity.EXTRA_GROUP_ID, g.id)
                    putExtra(GroupChatActivity.EXTRA_GROUP_NAME, g.name)
                })
            }
            container.addView(card)
        }
    }
}
