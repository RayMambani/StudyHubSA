package com.studyhub.sa.ui.offline

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.studyhub.sa.R
import com.studyhub.sa.data.local.AppDatabase
import com.studyhub.sa.data.local.entity.OfflineNoteEntity
import com.studyhub.sa.sync.ConnectivityObserver
import com.studyhub.sa.sync.SyncWorker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen 14: Offline Notes Screen.
 * This is the feature that satisfies the "offline mode with sync" PoE
 * requirement: notes are written straight to Room (SQLite) regardless of
 * connectivity, then SyncWorker pushes anything unsynced to the REST API
 * the moment the device is back online.
 *
 * Tapping a note in the list opens it (title + full content) for reading
 * and editing — previously only the title showed in the list with no way
 * to open what you'd actually written.
 */
class OfflineNotesActivity : AppCompatActivity() {

    private val dao by lazy { AppDatabase.getInstance(this).offlineNoteDao() }
    private lateinit var adapter: OfflineNoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_notes)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvNotes)
        adapter = OfflineNoteAdapter(
            onOpen = { note -> showNoteDialog(existing = note) },
            onDelete = { note -> lifecycleScope.launch { dao.delete(note) } }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val badge = findViewById<android.widget.TextView>(R.id.tvConnectivityBadge)
        val online = ConnectivityObserver.isOnline(this)
        badge.text = if (online) "Online — syncing" else getString(R.string.offline_mode_badge)

        findViewById<android.view.View>(R.id.btnAddNote).setOnClickListener { showNoteDialog(existing = null) }

        lifecycleScope.launch {
            dao.observeAll().collectLatest { notes -> adapter.submitList(notes) }
        }

        // Try a sync pass every time this screen opens, in addition to the
        // periodic background worker scheduled from SplashActivity.
        SyncWorker.syncNow(this)
    }

    /** Shared dialog for both creating a new note and viewing/editing an existing one. */
    private fun showNoteDialog(existing: OfflineNoteEntity?) {
        val view = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val etTitle = view.findViewById<EditText>(R.id.etNoteTitle)
        val etContent = view.findViewById<EditText>(R.id.etNoteContent)

        if (existing != null) {
            etTitle.setText(existing.title)
            etContent.setText(existing.content)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.offline_notes_title)
            .setView(view)
            .setPositiveButton(R.string.action_save_note) { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isEmpty()) return@setPositiveButton

                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(java.util.Date())
                val note = OfflineNoteEntity(
                    id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                    title = title,
                    content = etContent.text.toString(),
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                    isSynced = false // any create or edit is written locally first, then synced
                )
                lifecycleScope.launch {
                    dao.upsert(note)
                    SyncWorker.syncNow(this@OfflineNotesActivity)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
