package com.studyhub.sa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.studyhub.sa.data.local.dao.OfflineNoteDao
import com.studyhub.sa.data.local.entity.OfflineNoteEntity

@Database(entities = [OfflineNoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun offlineNoteDao(): OfflineNoteDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studyhub_offline.db"
                ).build().also { instance = it }
            }
    }
}
