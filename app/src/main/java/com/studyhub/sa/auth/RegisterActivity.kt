package com.studyhub.sa.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studyhub.sa.R
import com.studyhub.sa.data.repository.AuthRepository
import com.studyhub.sa.data.repository.AuthResult
import com.studyhub.sa.ui.dashboard.DashboardActivity
import kotlinx.coroutines.launch

/** Screen 3: Registration Screen. */
class RegisterActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        authRepository = AuthRepository(this)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etStudentNumber = findViewById<EditText>(R.id.etStudentNumber)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        tvGoToLogin.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val studentNumber = etStudentNumber.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                toast("Please fill in all required fields")
                return@setOnClickListener
            }
            if (password.length < 8) {
                toast("Password must be at least 8 characters")
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnRegister.isEnabled = false

            lifecycleScope.launch {
                when (val result = authRepository.register(fullName, studentNumber, email, password)) {
                    is AuthResult.Success -> {
                        startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                        finish()
                    }
                    is AuthResult.Error -> {
                        progressBar.visibility = View.GONE
                        btnRegister.isEnabled = true
                        toast(result.message)
                    }
                }
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
