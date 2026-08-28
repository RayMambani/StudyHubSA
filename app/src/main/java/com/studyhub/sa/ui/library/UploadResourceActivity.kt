package com.studyhub.sa.ui.library

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/** Screen 6: Upload Resource Screen — real multipart/form-data upload to the REST API. */
class UploadResourceActivity : AppCompatActivity() {

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = ""

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = queryFileName(uri) ?: "resource_file"
            findViewById<TextView>(R.id.tvSelectedFile).apply {
                visibility = View.VISIBLE
                text = selectedFileName
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_resource)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.filePicker).setOnClickListener {
            filePickerLauncher.launch("*/*")
        }
        findViewById<Button>(R.id.btnSubmit).setOnClickListener { submit() }
    }

    private fun submit() {
        val title = findViewById<EditText>(R.id.etTitle).text.toString().trim()
        val description = findViewById<EditText>(R.id.etDescription).text.toString().trim()
        val module = findViewById<EditText>(R.id.etModule).text.toString().trim()
        val fileUri = selectedFileUri

        if (title.isEmpty() || fileUri == null) {
            toast("Please add a title and choose a file")
            return
        }

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false
        toast("Uploading — this can take up to a minute if the server was asleep")

        lifecycleScope.launch {
            try {
                val cachedFile = withContext(Dispatchers.IO) { copyUriToCacheFile(fileUri, selectedFileName) }
                val requestFile = cachedFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", cachedFile.name, requestFile)

                val response = RetrofitClient.getInstance(this@UploadResourceActivity).uploadResource(
                    file = filePart,
                    title = title.toRequestBody("text/plain".toMediaTypeOrNull()),
                    description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                    module = module.toRequestBody("text/plain".toMediaTypeOrNull())
                )

                if (response.isSuccessful) {
                    toast("Resource uploaded")
                    finish()
                } else {
                    val serverError = response.errorBody()?.string()
                    toast(if (!serverError.isNullOrBlank()) serverError else "Upload failed (${response.code()})")
                }
            } catch (e: java.net.SocketTimeoutException) {
                toast("The server took too long to respond — it may still be waking up. Please try again.")
            } catch (e: Exception) {
                toast("Could not reach the server — check your connection. (${e.message})")
            } finally {
                progressBar.visibility = View.GONE
                btnSubmit.isEnabled = true
            }
        }
    }

    private fun queryFileName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) return cursor.getString(nameIndex)
        }
        return null
    }

    private fun copyUriToCacheFile(uri: Uri, fileName: String): File {
        val outFile = File(cacheDir, fileName)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output -> input.copyTo(output) }
        }
        return outFile
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
