package com.studyhub.sa.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.ChangePasswordRequest
import kotlinx.coroutines.launch

/** Settings > Account Security. Real bcrypt-verified password change against the backend. */
class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnUpdatePassword).setOnClickListener { submit() }
    }

    private fun submit() {
        val current = findViewById<EditText>(R.id.etCurrentPassword).text.toString()
        val newPass = findViewById<EditText>(R.id.etNewPassword).text.toString()
        val confirm = findViewById<EditText>(R.id.etConfirmPassword).text.toString()

        if (newPass.length < 8) {
            toast("New password must be at least 8 characters")
            return
        }
        if (newPass != confirm) {
            toast("Passwords do not match")
            return
        }

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btn = findViewById<Button>(R.id.btnUpdatePassword)
        progressBar.visibility = View.VISIBLE
        btn.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@ChangePasswordActivity)
                    .changePassword(ChangePasswordRequest(current.ifEmpty { null }, newPass))
                if (response.isSuccessful) {
                    toast(getString(R.string.password_updated))
                    finish()
                } else {
                    toast(response.errorBody()?.string() ?: "Could not update password (${response.code()})")
                }
            } catch (e: Exception) {
                toast("Could not reach the server — check your connection")
            } finally {
                progressBar.visibility = View.GONE
                btn.isEnabled = true
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
