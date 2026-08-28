package com.studyhub.sa.ui.offline

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.studyhub.sa.R
import com.studyhub.sa.data.local.entity.OfflineNoteEntity

class OfflineNoteAdapter(
    private val onOpen: (OfflineNoteEntity) -> Unit,
    private val onDelete: (OfflineNoteEntity) -> Unit
) : RecyclerView.Adapter<OfflineNoteAdapter.NoteViewHolder>() {

    private val items = mutableListOf<OfflineNoteEntity>()

    fun submitList(newItems: List<OfflineNoteEntity>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = items[oldPos].id == newItems[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = items[oldPos] == newItems[newPos]
        })
        items.clear()
        items.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_offline_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle = view.findViewById<TextView>(R.id.tvNoteTitle)
        private val tvStatus = view.findViewById<TextView>(R.id.tvSyncStatus)
        private val ivStatus = view.findViewById<ImageView>(R.id.ivSyncStatus)
        private val btnDelete = view.findViewById<ImageView>(R.id.btnDeleteNote)

        fun bind(note: OfflineNoteEntity) {
            tvTitle.text = note.title
            val context = itemView.context
            if (note.isSynced) {
                tvStatus.text = context.getString(R.string.status_synced)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
                ivStatus.setImageResource(R.drawable.ic_check)
                ivStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green))
            } else {
                tvStatus.text = context.getString(R.string.status_pending_sync)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.gold))
                ivStatus.setImageResource(R.drawable.ic_wifi_off)
                ivStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gold))
            }
            btnDelete.setOnClickListener { onDelete(note) }
            itemView.setOnClickListener { onOpen(note) }
        }
    }
}
