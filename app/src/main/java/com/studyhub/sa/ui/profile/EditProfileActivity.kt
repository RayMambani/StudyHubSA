package com.studyhub.sa.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.studyhub.sa.BuildConfig
import com.studyhub.sa.R
import com.studyhub.sa.data.local.SessionManager
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.UpdateProfileRequest
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/** Profile > Edit — real name/student-number updates plus a real profile picture upload. */
class EditProfileActivity : AppCompatActivity() {

    private var selectedPhotoUri: Uri? = null

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            Glide.with(this).load(uri).circleCrop().into(findViewById<CircleImageView>(R.id.ivAvatar))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val session = SessionManager(this)
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etStudentNumber = findViewById<EditText>(R.id.etStudentNumber)
        val ivAvatar = findViewById<CircleImageView>(R.id.ivAvatar)
        etFullName.setText(session.fullName ?: "")

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnChangePhoto).setOnClickListener { photoPickerLauncher.launch("image/*") }

        // Pre-fill student number + existing photo from the backend.
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@EditProfileActivity).getProfile()
                if (response.isSuccessful) {
                    val user = response.body()
                    etStudentNumber.setText(user?.studentNumber ?: "")
                    user?.profileImage?.let { path ->
                        val fullUrl = BuildConfig.BASE_URL.trimEnd('/') + path
                        Glide.with(this@EditProfileActivity).load(fullUrl).circleCrop().into(ivAvatar)
                    }
                }
            } catch (_: Exception) { /* offline — fields stay editable regardless */ }
        }

        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener { submit() }
    }

    private fun submit() {
        val fullName = findViewById<EditText>(R.id.etFullName).text.toString().trim()
        val studentNumber = findViewById<EditText>(R.id.etStudentNumber).text.toString().trim()

        if (fullName.isEmpty()) {
            toast("Full name cannot be empty")
            return
        }

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btn = findViewById<Button>(R.id.btnSaveProfile)
        progressBar.visibility = View.VISIBLE
        btn.isEnabled = false

        lifecycleScope.launch {
            try {
                selectedPhotoUri?.let { uploadPhoto(it) }

                val response = RetrofitClient.getInstance(this@EditProfileActivity)
                    .updateProfile(UpdateProfileRequest(fullName, studentNumber))
                if (response.isSuccessful) {
                    SessionManager(this@EditProfileActivity).fullName = fullName
                    toast("Profile updated")
                    finish()
                } else {
                    toast(response.errorBody()?.string() ?: "Could not update profile (${response.code()})")
                }
            } catch (e: Exception) {
                toast("Could not reach the server — check your connection")
            } finally {
                progressBar.visibility = View.GONE
                btn.isEnabled = true
            }
        }
    }

    private suspend fun uploadPhoto(uri: Uri) {
        val cachedFile = withContext(Dispatchers.IO) {
            val outFile = File(cacheDir, "avatar_upload.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output -> input.copyTo(output) }
            }
            outFile
        }
        val requestFile = cachedFile.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", cachedFile.name, requestFile)
        RetrofitClient.getInstance(this).uploadAvatar(part)
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
