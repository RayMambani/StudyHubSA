package com.studyhub.sa.ui.ai

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.AskRequest
import com.studyhub.sa.data.remote.dto.SummarizeRequest
import kotlinx.coroutines.launch

/**
 * AI Study Assist — generate study summaries, ask academic questions, and
 * get study recommendations, all backed by real endpoints on the server
 * (see backend/routes/ai.js for exactly how each one works, and how to
 * swap in a real LLM later).
 */
class AiAssistActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_assist)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSummarize).setOnClickListener { summarize() }
        findViewById<View>(R.id.btnAsk).setOnClickListener { askQuestion() }
        findViewById<Button>(R.id.btnGetRecommendations).setOnClickListener { getRecommendations() }
    }

    private fun summarize() {
        val text = findViewById<EditText>(R.id.etSummarizeInput).text.toString().trim()
        val resultView = findViewById<TextView>(R.id.tvSummaryResult)
        if (text.length < 40) {
            toast("Paste at least a paragraph of text to summarise")
            return
        }
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@AiAssistActivity).summarize(SummarizeRequest(text))
                if (response.isSuccessful) {
                    resultView.visibility = View.VISIBLE
                    resultView.text = response.body()?.summary ?: ""
                } else {
                    toast(response.errorBody()?.string() ?: "Could not summarise")
                }
            } catch (e: Exception) {
                toast("Could not reach the server — check your connection")
            }
        }
    }

    private fun askQuestion() {
        val etQuestion = findViewById<EditText>(R.id.etQuestion)
        val question = etQuestion.text.toString().trim()
        val resultView = findViewById<TextView>(R.id.tvAskResult)
        if (question.isEmpty()) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@AiAssistActivity).askQuestion(AskRequest(question))
                if (response.isSuccessful) {
                    resultView.visibility = View.VISIBLE
                    resultView.text = response.body()?.answer ?: ""
                    etQuestion.text.clear()
                } else {
                    toast("Could not get an answer right now")
                }
            } catch (e: Exception) {
                toast("Could not reach the server — check your connection")
            }
        }
    }

    private fun getRecommendations() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@AiAssistActivity).getRecommendations()
                if (response.isSuccessful) {
                    renderRecommendations(response.body()?.recommendations.orEmpty())
                } else {
                    toast("Could not load recommendations")
                }
            } catch (e: Exception) {
                toast("Could not reach the server — check your connection")
            }
        }
    }

    private fun renderRecommendations(items: List<String>) {
        val container = findViewById<LinearLayout>(R.id.recommendationsContainer)
        container.removeAllViews()
        items.forEach { text ->
            val row = TextView(this).apply {
                this.text = "•  $text"
                setTextColor(androidx.core.content.ContextCompat.getColor(this@AiAssistActivity, R.color.ink))
                textSize = 12.5f
                setPadding(0, 0, 0, 20)
            }
            container.addView(row)
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
