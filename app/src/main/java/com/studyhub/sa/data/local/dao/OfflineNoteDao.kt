package com.studyhub.sa.data.local.dao

import androidx.room.*
import com.studyhub.sa.data.local.entity.OfflineNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineNoteDao {

    @Query("SELECT * FROM offline_notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<OfflineNoteEntity>>

    @Query("SELECT * FROM offline_notes WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<OfflineNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: OfflineNoteEntity)

    @Query("UPDATE offline_notes SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Delete
    suspend fun delete(note: OfflineNoteEntity)
}
