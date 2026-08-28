package com.studyhub.sa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A note the student created/edited on-device. Works fully offline via
 * Room/SQLite; `isSynced` tracks whether the backend has this exact version yet.
 */
@Entity(tableName = "offline_notes")
data class OfflineNoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = false
)
