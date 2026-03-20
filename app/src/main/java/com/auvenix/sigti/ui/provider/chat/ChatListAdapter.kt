package com.auvenix.sigti.ui.provider.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

data class ChatModel(
    val id         : String = "",
    val name       : String = "",
    val lastMessage: String = "",
    val time       : String = ""
)

class ChatListAdapter(
    private val chatList   : List<ChatModel>,
    private val onChatClick: (ChatModel) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName       : TextView = view.findViewById(R.id.tvUserName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime       : TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_preview, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.tvName.text        = chat.name
        holder.tvLastMessage.text = chat.lastMessage
        holder.tvTime.text        = chat.time

        // ✅ Al tocar → abre ChatDetailActivity con datos reales
        holder.itemView.setOnClickListener { onChatClick(chat) }
    }

    override fun getItemCount() = chatList.size
}