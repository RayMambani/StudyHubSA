package com.studyhub.sa.ui.groups

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.studyhub.sa.R
import com.studyhub.sa.data.local.SessionManager
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.SendMessageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen 9: Group Chat Screen.
 * Polls for new messages every few seconds while open — a lightweight
 * stand-in for a websocket connection that keeps the REST-only backend simple.
 * New messages also arrive as FCM push notifications when the app is closed.
 */
class GroupChatActivity : AppCompatActivity() {

    private lateinit var groupId: String
    private lateinit var adapter: MessageAdapter
    private var polling = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        groupId = intent.getStringExtra(EXTRA_GROUP_ID) ?: return finish()
        val groupName = intent.getStringExtra(EXTRA_GROUP_NAME) ?: "Group Chat"
        findViewById<TextView>(R.id.tvGroupName).text = groupName
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        val session = SessionManager(this)
        adapter = MessageAdapter(session.userId)
        findViewById<RecyclerView>(R.id.rvMessages).apply {
            layoutManager = LinearLayoutManager(this@GroupChatActivity)
            adapter = this@GroupChatActivity.adapter
        }

        findViewById<android.view.View>(R.id.btnSend).setOnClickListener { sendMessage() }

        startPolling()
    }

    private fun startPolling() {
        lifecycleScope.launch {
            while (polling) {
                loadMessages()
                delay(4000)
            }
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@GroupChatActivity).getMessages(groupId)
                if (response.isSuccessful) {
                    adapter.submitList(response.body().orEmpty())
                    findViewById<RecyclerView>(R.id.rvMessages).scrollToPosition(adapter.itemCount - 1)
                }
            } catch (e: Exception) {
                // Silent — chat just won't refresh until connectivity returns.
            }
        }
    }

    private fun sendMessage() {
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val content = etMessage.text.toString().trim()
        if (content.isEmpty()) return

        lifecycleScope.launch {
            try {
                RetrofitClient.getInstance(this@GroupChatActivity).sendMessage(groupId, SendMessageRequest(content))
                etMessage.text.clear()
                loadMessages()
            } catch (e: Exception) {
                Toast.makeText(this@GroupChatActivity, "Message not sent — check your connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        polling = false
        super.onDestroy()
    }

    companion object {
        const val EXTRA_GROUP_ID = "extra_group_id"
        const val EXTRA_GROUP_NAME = "extra_group_name"
    }
}
