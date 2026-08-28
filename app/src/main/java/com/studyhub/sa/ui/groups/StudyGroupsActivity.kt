package com.studyhub.sa.ui.groups

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.CreateGroupRequest
import com.studyhub.sa.ui.common.BottomNavHelper
import kotlinx.coroutines.launch

/** Screen 8: Study Groups Screen. */
class StudyGroupsActivity : AppCompatActivity() {

    private lateinit var adapter: StudyGroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_groups)

        adapter = StudyGroupAdapter(onJoin = { group ->
            lifecycleScope.launch {
                try {
                    RetrofitClient.getInstance(this@StudyGroupsActivity).joinGroup(group.id)
                    toast("Joined ${group.name}")
                    loadGroups()
                } catch (e: Exception) {
                    toast("Could not join — check your connection")
                }
            }
        })

        findViewById<RecyclerView>(R.id.rvGroups).apply {
            layoutManager = LinearLayoutManager(this@StudyGroupsActivity)
            adapter = this@StudyGroupsActivity.adapter
        }

        findViewById<SwipeRefreshLayout>(R.id.swipeRefresh).setOnRefreshListener { loadGroups() }
        findViewById<android.view.View>(R.id.btnCreateGroup).setOnClickListener { showCreateGroupDialog() }

        BottomNavHelper.setup(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_groups)

        loadGroups()
    }

    private fun loadGroups() {
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@StudyGroupsActivity).getGroups()
                if (response.isSuccessful) adapter.submitList(response.body().orEmpty())
            } catch (e: Exception) {
                toast("Offline — showing the last loaded groups")
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showCreateGroupDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_note, null) // reuse simple two-field layout
        val etName = view.findViewById<EditText>(R.id.etNoteTitle).apply { hint = "Group name" }
        val etModule = view.findViewById<EditText>(R.id.etNoteContent).apply { hint = "Module code"; minLines = 1 }

        AlertDialog.Builder(this)
            .setTitle(R.string.action_create_group)
            .setView(view)
            .setPositiveButton(R.string.action_create_group) { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) return@setPositiveButton
                lifecycleScope.launch {
                    try {
                        RetrofitClient.getInstance(this@StudyGroupsActivity)
                            .createGroup(CreateGroupRequest(name, etModule.text.toString().trim(), null))
                        loadGroups()
                    } catch (e: Exception) {
                        toast("Could not create group — check your connection")
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
