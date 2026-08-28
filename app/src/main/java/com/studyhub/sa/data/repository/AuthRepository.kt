package com.studyhub.sa.data.repository

import android.content.Context
import com.studyhub.sa.data.local.SessionManager
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.*

sealed class AuthResult {
    data class Success(val user: UserDto) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(private val context: Context) {

    private val api = RetrofitClient.getInstance(context)
    private val session = SessionManager(context)

    suspend fun register(fullName: String, studentNumber: String?, email: String, password: String): AuthResult {
        return try {
            val response = api.register(RegisterRequest(fullName, studentNumber, email, password))
            handle(response)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Could not reach the server. Check your connection.")
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = api.login(LoginRequest(email, password))
            handle(response)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Could not reach the server. Check your connection.")
        }
    }

    suspend fun googleSignIn(idToken: String): AuthResult {
        return try {
            val response = api.googleSignIn(GoogleAuthRequest(idToken))
            handle(response)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed. Check your connection.")
        }
    }

    private fun handle(response: retrofit2.Response<AuthResponse>): AuthResult {
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            session.token = body.token
            session.userId = body.user.id
            session.fullName = body.user.fullName
            session.email = body.user.email
            return AuthResult.Success(body.user)
        }
        val message = response.errorBody()?.string() ?: "Something went wrong (${response.code()})"
        return AuthResult.Error(message)
    }

    fun logout() = session.clear()
}
