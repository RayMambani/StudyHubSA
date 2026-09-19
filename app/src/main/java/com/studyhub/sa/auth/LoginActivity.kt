package com.studyhub.sa.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.studyhub.sa.R
import com.studyhub.sa.data.repository.AuthRepository
import com.studyhub.sa.data.repository.AuthResult
import com.studyhub.sa.ui.dashboard.DashboardActivity
import kotlinx.coroutines.launch

/** Screen 2: Login Screen. */
class LoginActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                signInWithGoogle(idToken)
            } else {
                toast(getString(R.string.action_google_sign_in) + " failed")
            }
        } catch (e: ApiException) {
            toast("Google sign-in cancelled or failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        authRepository = AuthRepository(this)

        val isReturning = com.studyhub.sa.data.local.SessionManager(this).hasLoggedInBefore
        findViewById<android.widget.TextView>(R.id.tvLoginTitle).text =
            if (isReturning) getString(R.string.login_title) else getString(R.string.login_title_new)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogle = findViewById<Button>(R.id.btnGoogleSignIn)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Please enter your email and password")
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            lifecycleScope.launch {
                when (val result = authRepository.login(email, password)) {
                    is AuthResult.Success -> goToDashboard()
                    is AuthResult.Error -> {
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                        toast(result.message)
                    }
                }
            }
        }

        btnGoogle.setOnClickListener {
            googleSignInLauncher.launch(GoogleSignInHelper.signInIntent(this))
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun signInWithGoogle(idToken: String) {
        lifecycleScope.launch {
            when (val result = authRepository.googleSignIn(idToken)) {
                is AuthResult.Success -> goToDashboard()
                is AuthResult.Error -> toast(result.message)
            }
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
