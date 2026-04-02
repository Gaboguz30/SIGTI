package com.auvenix.sigti.ui.chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

data class ChatPreview(
    val serviceId: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0
)

class ChatListAdapter(
    private val chats: List<ChatPreview>
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val message: TextView = view.findViewById(R.id.tvMessage)
        val time: TextView = view.findViewById(R.id.tvTime)
        val avatar: TextView = view.findViewById(R.id.tvAvatar)
        val badge: TextView = view.findViewById(R.id.tvBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chats[position]

        holder.name.text = chat.name
        holder.message.text = chat.lastMessage
        holder.time.text = chat.time

        // 🔥 Avatar con inicial (más pro)
        holder.avatar.text = chat.name.firstOrNull()?.uppercase() ?: "👤"

        // 🔥 Badge (ejemplo simple)
        if (chat.unreadCount > 0) {
            holder.badge.visibility = View.VISIBLE
            holder.badge.text = chat.unreadCount.toString()
        } else {
            holder.badge.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatDetailActivity::class.java).apply {
                putExtra("serviceId", chat.serviceId)
                putExtra("contactName", chat.name)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = chats.size
}