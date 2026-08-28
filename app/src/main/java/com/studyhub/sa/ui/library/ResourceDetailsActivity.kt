package com.studyhub.sa.ui.library

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.studyhub.sa.BuildConfig
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.CommentDto
import com.studyhub.sa.data.remote.dto.CreateCommentRequest
import com.studyhub.sa.util.DownloadHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/** Screen 7: Resource Details Screen — with a real document preview and a real per-resource comment thread. */
class ResourceDetailsActivity : AppCompatActivity() {

    private var resourceId: String? = null

    private val previewClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resource_details)

        resourceId = intent.getStringExtra(EXTRA_RESOURCE_ID)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Untitled resource"
        val module = intent.getStringExtra(EXTRA_MODULE) ?: ""
        val description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: "No description provided."
        val fileUrl = intent.getStringExtra(EXTRA_FILE_URL)
        val fileType = intent.getStringExtra(EXTRA_FILE_TYPE) ?: ""

        findViewById<TextView>(R.id.tvModule).text = module.ifEmpty { "General" }
        findViewById<TextView>(R.id.tvTitle).text = title
        findViewById<TextView>(R.id.tvDescription).text = description

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<android.widget.Button>(R.id.btnDownload).setOnClickListener {
            DownloadHelper.downloadResource(this, fileUrl, title)
        }

        findViewById<View>(R.id.btnPostComment).setOnClickListener { postComment() }

        loadPreview(fileUrl, fileType)
        loadComments()
    }

    /** Text-based files (TXT, and most seed docs) get a real inline preview; other
     * types fall back to the placeholder icon since they can't render as plain text. */
    private fun loadPreview(fileUrl: String?, fileType: String) {
        if (fileUrl.isNullOrBlank()) return
        val previewableTypes = setOf("TXT", "MD", "CSV", "JSON")
        if (fileType.uppercase() !in previewableTypes) return

        val fullUrl = BuildConfig.BASE_URL.trimEnd('/') + fileUrl

        lifecycleScope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    val request = Request.Builder().url(fullUrl).build()
                    previewClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) null else response.body?.string()
                    }
                }
                if (!text.isNullOrBlank()) {
                    findViewById<TextView>(R.id.tvPreviewText).text = text.take(4000)
                    findViewById<View>(R.id.ivPreviewPlaceholder).visibility = View.GONE
                    findViewById<View>(R.id.svPreviewText).visibility = View.VISIBLE
                }
            } catch (_: Exception) {
                // Leave the placeholder icon showing — download still works regardless.
            }
        }
    }

    private fun loadComments() {
        val id = resourceId ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@ResourceDetailsActivity).getComments(id)
                if (response.isSuccessful) renderComments(response.body().orEmpty())
            } catch (_: Exception) {
                // Offline — leave whatever was last shown.
            }
        }
    }

    private fun renderComments(comments: List<CommentDto>) {
        val container = findViewById<LinearLayout>(R.id.commentsContainer)
        container.removeAllViews()

        if (comments.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No comments yet — be the first to share a tip."
                setTextColor(ContextCompat.getColor(this@ResourceDetailsActivity, R.color.muted))
                textSize = 12.5f
            }
            container.addView(empty)
            return
        }

        comments.forEach { c ->
            val name = TextView(this).apply {
                text = c.author_name
                setTextColor(ContextCompat.getColor(this@ResourceDetailsActivity, R.color.ink))
                textSize = 12.5f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            val body = TextView(this).apply {
                text = c.content
                setTextColor(ContextCompat.getColor(this@ResourceDetailsActivity, R.color.ink))
                textSize = 12.5f
                setPadding(0, 0, 0, 28)
            }
            container.addView(name)
            container.addView(body)
        }
    }

    private fun postComment() {
        val id = resourceId ?: return
        val etComment = findViewById<EditText>(R.id.etNewComment)
        val content = etComment.text.toString().trim()
        if (content.isEmpty()) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@ResourceDetailsActivity)
                    .postComment(id, CreateCommentRequest(content))
                if (response.isSuccessful) {
                    etComment.text.clear()
                    loadComments()
                }
            } catch (_: Exception) { /* ignore — comment stays in the box to retry */ }
        }
    }

    companion object {
        const val EXTRA_RESOURCE_ID = "extra_resource_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MODULE = "extra_module"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_FILE_URL = "extra_file_url"
        const val EXTRA_FILE_TYPE = "extra_file_type"
    }
}
