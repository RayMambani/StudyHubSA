package com.studyhub.sa.sync

import android.content.Context
import androidx.work.*
import com.studyhub.sa.data.local.AppDatabase
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.NoteSyncItem
import com.studyhub.sa.data.remote.dto.NoteSyncRequest
import java.util.concurrent.TimeUnit

/**
 * Pushes every locally-queued (unsynced) note to the REST API in one batch.
 * Runs (a) immediately when the app detects connectivity has returned, and
 * (b) as a periodic safety-net every 15 minutes via WorkManager's network
 * constraint, so nothing created offline is silently lost.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!ConnectivityObserver.isOnline(applicationContext)) {
            return Result.retry()
        }

        return try {
            val dao = AppDatabase.getInstance(applicationContext).offlineNoteDao()
            val pending = dao.getUnsyncedNotes()
            if (pending.isEmpty()) return Result.success()

            val api = RetrofitClient.getInstance(applicationContext)
            val request = NoteSyncRequest(
                notes = pending.map {
                    NoteSyncItem(it.id, it.title, it.content, it.createdAt, it.updatedAt)
                }
            )
            val response = api.syncNotes(request)

            if (response.isSuccessful) {
                response.body()?.syncedIds?.let { dao.markSynced(it) }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "studyhub_note_sync_periodic"
        private const val ONE_OFF_WORK_NAME = "studyhub_note_sync_now"

        private val networkConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        /** Call once (e.g. from Application.onCreate) to keep syncing in the background. */
        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(networkConstraint)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }

        /** Call right after saving a note, or when a connectivity-changed broadcast fires. */
        fun syncNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(networkConstraint)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_OFF_WORK_NAME, ExistingWorkPolicy.REPLACE, request
            )
        }
    }
}
