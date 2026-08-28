package com.studyhub.sa.ui.notifications

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.NotificationDto
import kotlinx.coroutines.launch

/**
 * Screen 11: Notifications Screen.
 * Real feed from GET /api/notifications — rows are created server-side
 * whenever a resource is uploaded, a group message is sent, someone joins
 * a group, or a calendar event is added (see backend/notifications/feed.js).
 */
class NotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        loadNotifications()
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@NotificationsActivity).getNotifications()
                if (response.isSuccessful) {
                    render(response.body().orEmpty())
                }
            } catch (e: Exception) {
                // Offline — leave whatever was last rendered.
            }
        }
    }

    private fun render(items: List<NotificationDto>) {
        val container = findViewById<LinearLayout>(R.id.notificationsContainer)
        container.removeAllViews()

        if (items.isEmpty()) {
            val empty = TextView(this).apply {
                text = getString(R.string.no_notifications)
                setTextColor(androidx.core.content.ContextCompat.getColor(this@NotificationsActivity, R.color.muted))
                textSize = 13f
            }
            container.addView(empty)
            return
        }

        items.forEach { n ->
            val row = layoutInflater.inflate(R.layout.item_notification, container, false)
            row.findViewById<TextView>(R.id.tvNotifTitle).text = n.title
            row.findViewById<TextView>(R.id.tvNotifBody).text = n.body
            row.findViewById<TextView>(R.id.tvNotifTime).text = n.created_at
            row.alpha = if (n.is_read == 1) 0.55f else 1f

            val icon = when (n.type) {
                "resource" -> R.drawable.ic_upload
                "group" -> R.drawable.ic_users
                "message" -> R.drawable.ic_send
                "reminder" -> R.drawable.ic_calendar
                else -> R.drawable.ic_bell
            }
            row.findViewById<android.widget.ImageView>(R.id.ivIcon).setImageResource(icon)

            row.setOnClickListener {
                lifecycleScope.launch {
                    try {
                        RetrofitClient.getInstance(this@NotificationsActivity).markNotificationRead(n.id)
                        row.alpha = 0.55f
                    } catch (_: Exception) { /* ignore */ }
                }
            }
            row.findViewById<android.widget.ImageView>(R.id.btnDeleteNotif).setOnClickListener {
                lifecycleScope.launch {
                    try {
                        RetrofitClient.getInstance(this@NotificationsActivity).deleteNotification(n.id)
                        container.removeView(row)
                    } catch (_: Exception) { /* ignore */ }
                }
            }

            container.addView(row)
        }
    }
}
