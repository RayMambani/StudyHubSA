package com.studyhub.sa.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.studyhub.sa.R

/**
 * Wraps Google Sign-In (SSO). The ID token returned here is sent to
 * POST /api/auth/google, where the backend verifies it and logs the
 * student in — or creates their account on first use.
 */
object GoogleSignInHelper {

    fun getClient(context: Context): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun signInIntent(context: Context): Intent = getClient(context).signInIntent

    fun signOut(context: Context) {
        getClient(context).signOut()
    }
}
