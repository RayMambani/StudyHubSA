package com.studyhub.sa.ui.groups

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.dto.MessageDto

class MessageAdapter(private val currentUserId: String?) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val items = mutableListOf<MessageDto>()

    fun submitList(newItems: List<MessageDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val root = view as LinearLayout
        private val tvSender = view.findViewById<TextView>(R.id.tvSender)
        private val tvBubble = view.findViewById<TextView>(R.id.tvBubble)

        fun bind(message: MessageDto) {
            val isMine = message.sender_id == currentUserId
            val context = itemView.context

            root.gravity = if (isMine) Gravity.END else Gravity.START
            tvSender.visibility = if (isMine) View.GONE else View.VISIBLE
            tvSender.text = message.sender_id.take(6)
            tvBubble.text = message.content

            if (isMine) {
                tvBubble.setBackgroundResource(R.drawable.bg_bubble_out)
                tvBubble.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                tvBubble.setBackgroundResource(R.drawable.bg_bubble_in)
                tvBubble.setTextColor(ContextCompat.getColor(context, R.color.ink))
            }
        }
    }
}
