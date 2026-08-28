package com.studyhub.sa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.studyhub.sa.auth.LoginActivity
import com.studyhub.sa.data.local.SessionManager
import com.studyhub.sa.sync.SyncWorker
import com.studyhub.sa.ui.dashboard.DashboardActivity

/**
 * Screen 1: Splash Screen — shows the logo while the app confirms whether a
 * session already exists, then routes straight to the Dashboard, or to
 * Login if not. Also kicks off the periodic offline-notes sync worker.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        SyncWorker.schedulePeriodic(applicationContext)

        Handler(Looper.getMainLooper()).postDelayed({
            val session = SessionManager(this)
            val next = if (session.isLoggedIn) DashboardActivity::class.java else LoginActivity::class.java
            startActivity(Intent(this, next))
            finish()
        }, 1200)
    }
}
