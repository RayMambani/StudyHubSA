package com.studyhub.sa.ui.library

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.dto.ResourceDto
import com.studyhub.sa.util.DownloadHelper

class ResourceAdapter : RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder>() {

    private val items = mutableListOf<ResourceDto>()

    fun submitList(newItems: List<ResourceDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resource, parent, false)
        return ResourceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ResourceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle = view.findViewById<TextView>(R.id.tvResourceTitle)
        private val tvMeta = view.findViewById<TextView>(R.id.tvResourceMeta)

        fun bind(resource: ResourceDto) {
            tvTitle.text = resource.title
            tvMeta.text = listOfNotNull(resource.module, resource.file_type).joinToString(" · ")

            itemView.findViewById<ImageView>(R.id.btnDownload).setOnClickListener {
                DownloadHelper.downloadResource(itemView.context, resource.file_url, resource.title)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ResourceDetailsActivity::class.java).apply {
                    putExtra(ResourceDetailsActivity.EXTRA_RESOURCE_ID, resource.id)
                    putExtra(ResourceDetailsActivity.EXTRA_TITLE, resource.title)
                    putExtra(ResourceDetailsActivity.EXTRA_MODULE, resource.module)
                    putExtra(ResourceDetailsActivity.EXTRA_DESCRIPTION, resource.description)
                    putExtra(ResourceDetailsActivity.EXTRA_FILE_URL, resource.file_url)
                    putExtra(ResourceDetailsActivity.EXTRA_FILE_TYPE, resource.file_type)
                }
                context.startActivity(intent)
            }
        }
    }
}
