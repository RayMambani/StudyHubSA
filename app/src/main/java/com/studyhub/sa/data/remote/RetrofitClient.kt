package com.studyhub.sa.data.remote

import android.content.Context
import com.studyhub.sa.BuildConfig
import com.studyhub.sa.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile private var apiService: ApiService? = null

    fun getInstance(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: build(context.applicationContext).also { apiService = it }
        }
    }

    private fun build(context: Context): ApiService {
        val session = SessionManager(context)

        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            session.token?.let { requestBuilder.addHeader("Authorization", "Bearer $it") }
            chain.proceed(requestBuilder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            // Render's free tier spins the server down after inactivity — the
            // first request after a while can take 30-50s just to wake it up,
            // and file uploads need more time on top of that. Short timeouts
            // here were the likely cause of "upload isn't working."
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
