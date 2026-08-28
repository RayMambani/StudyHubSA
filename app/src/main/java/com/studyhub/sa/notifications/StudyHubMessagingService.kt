package com.studyhub.sa.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.studyhub.sa.data.local.SessionManager
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.SettingsUpdateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives real-time push notifications from the StudyHub SA backend
 * (new resources, group messages, assignment/exam reminders, group invites).
 */
class StudyHubMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val session = SessionManager(applicationContext)
        if (!session.isLoggedIn) return

        // Send the refreshed FCM token to the backend so it knows where to push to.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.getInstance(applicationContext)
                    .updateSettings(SettingsUpdateRequest(fcmToken = token))
            } catch (_: Exception) {
                // Will retry the next time SyncWorker or app-open refreshes the token.
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "StudyHub SA"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        NotificationHelper.showNotification(applicationContext, title, body)
    }
}
