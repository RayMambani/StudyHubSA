package com.studyhub.sa.ui.library

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.ui.common.BottomNavHelper
import kotlinx.coroutines.launch

/** Screen 5: Resource Library Screen — search, list, and download shared study materials. */
class ResourceLibraryActivity : AppCompatActivity() {

    private lateinit var adapter: ResourceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resource_library)

        val rv = findViewById<RecyclerView>(R.id.rvResources)
        adapter = ResourceAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { loadResources(null) }

        findViewById<EditText>(R.id.etSearch).setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadResources(v.text.toString().trim())
                true
            } else false
        }

        findViewById<android.view.View>(R.id.btnUpload).setOnClickListener {
            startActivity(Intent(this, UploadResourceActivity::class.java))
        }

        BottomNavHelper.setup(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_library)

        loadResources(null)
    }

    override fun onResume() {
        super.onResume()
        loadResources(null) // pick up anything just uploaded
    }

    private fun loadResources(search: String?) {
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@ResourceLibraryActivity).getResources(search = search)
                if (response.isSuccessful) {
                    adapter.submitList(response.body().orEmpty())
                } else {
                    toast("Could not load resources (${response.code()})")
                }
            } catch (e: Exception) {
                toast("Offline — showing the last loaded resources")
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
