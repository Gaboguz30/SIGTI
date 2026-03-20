package com.auvenix.sigti.ui.provider.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

// Modelo exclusivo del chat del prestador
data class ChatModel(
    val id         : String = "",
    val name       : String = "",
    val lastMessage: String = "",
    val time       : String = ""
)

class ChatListAdapter(
    private val chats  : List<ChatModel>,
    private val onClick: (ChatModel) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName       : TextView = view.findViewById(R.id.tvUserName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime       : TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chats[position]
        holder.tvName.text        = chat.name
        holder.tvLastMessage.text = chat.lastMessage
        holder.tvTime.text        = chat.time
        holder.itemView.setOnClickListener { onClick(chat) }
    }

    override fun getItemCount() = chats.size
}