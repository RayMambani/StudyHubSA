package com.studyhub.sa.ui.common

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.studyhub.sa.R
import com.studyhub.sa.ui.calendar.AcademicCalendarActivity
import com.studyhub.sa.ui.dashboard.DashboardActivity
import com.studyhub.sa.ui.groups.StudyGroupsActivity
import com.studyhub.sa.ui.library.ResourceLibraryActivity
import com.studyhub.sa.ui.profile.ProfileActivity

/**
 * Wires up the bottom tab bar (Home / Library / Groups / Calendar / Profile)
 * that appears on every top-level screen, matching the mockup's tabbar.
 */
object BottomNavHelper {

    fun setup(activity: Activity, bottomNav: BottomNavigationView, selectedItemId: Int) {
        bottomNav.selectedItemId = selectedItemId
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == selectedItemId) return@setOnItemSelectedListener true
            val target: Class<*> = when (item.itemId) {
                R.id.nav_home -> DashboardActivity::class.java
                R.id.nav_library -> ResourceLibraryActivity::class.java
                R.id.nav_groups -> StudyGroupsActivity::class.java
                R.id.nav_calendar -> AcademicCalendarActivity::class.java
                R.id.nav_profile -> ProfileActivity::class.java
                else -> return@setOnItemSelectedListener false
            }
            activity.startActivity(Intent(activity, target))
            activity.overridePendingTransition(0, 0)
            activity.finish()
            true
        }
    }
}
