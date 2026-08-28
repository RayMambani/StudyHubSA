package com.studyhub.sa.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.studyhub.sa.BuildConfig
import com.studyhub.sa.R

/**
 * Wires the Download buttons in Resource Library / Resource Details to a
 * real download via Android's system DownloadManager — the file lands in
 * the device's Downloads folder with a progress notification, same as any
 * other app's downloads.
 */
object DownloadHelper {

    fun downloadResource(context: Context, relativeFileUrl: String?, displayName: String) {
        if (relativeFileUrl.isNullOrBlank()) {
            Toast.makeText(context, "No file available for this resource", Toast.LENGTH_SHORT).show()
            return
        }

        val fullUrl = BuildConfig.BASE_URL.trimEnd('/') + relativeFileUrl
        val fileName = relativeFileUrl.substringAfterLast('/')

        val request = DownloadManager.Request(Uri.parse(fullUrl))
            .setTitle(displayName)
            .setDescription("Downloading from StudyHub SA")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        Toast.makeText(context, context.getString(R.string.download_started), Toast.LENGTH_SHORT).show()
    }
}
