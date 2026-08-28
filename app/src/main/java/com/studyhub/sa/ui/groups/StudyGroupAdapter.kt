package com.studyhub.sa.ui.groups

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.dto.StudyGroupDto

class StudyGroupAdapter(
    private val onJoin: (StudyGroupDto) -> Unit
) : RecyclerView.Adapter<StudyGroupAdapter.GroupViewHolder>() {

    private val items = mutableListOf<StudyGroupDto>()
    private val palette = listOf("#1E3A5F", "#3FA34D", "#E0A526")

    fun submitList(newItems: List<StudyGroupDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_study_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) = holder.bind(items[position], position)
    override fun getItemCount() = items.size

    inner class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvInitial = view.findViewById<TextView>(R.id.tvGroupInitial)
        private val tvName = view.findViewById<TextView>(R.id.tvGroupName)
        private val tvMeta = view.findViewById<TextView>(R.id.tvGroupMeta)
        private val btnJoin = view.findViewById<TextView>(R.id.btnJoin)

        fun bind(group: StudyGroupDto, position: Int) {
            tvInitial.text = group.name.take(1)
            tvInitial.background.setTint(Color.parseColor(palette[position % palette.size]))
            tvName.text = group.name
            tvMeta.text = listOfNotNull(group.module, "${group.member_count} members").joinToString(" · ")

            btnJoin.setOnClickListener { onJoin(group) }
            itemView.setOnClickListener {
                val context = itemView.context
                context.startActivity(Intent(context, GroupChatActivity::class.java).apply {
                    putExtra(GroupChatActivity.EXTRA_GROUP_ID, group.id)
                    putExtra(GroupChatActivity.EXTRA_GROUP_NAME, group.name)
                })
            }
        }
    }
}
