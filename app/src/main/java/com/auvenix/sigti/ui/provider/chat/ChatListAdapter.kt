package com.auvenix.sigti.ui.provider.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

data class ChatModel(
    val id         : String = "",
    val name       : String = "",
    val lastMessage: String = "",
    val time       : String = "",
    val unreadCount: Int = 0   // 🔥 NUEVO
)


class ChatListAdapter(
    val chatList: MutableList<ChatModel>,
    private val onChatClick: (ChatModel) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val fullList = mutableListOf<ChatModel>()
    fun setFullList(list: List<ChatModel>) {
        fullList.clear()

        fullList.addAll(list)
    }

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvUserName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvBadge: TextView = view.findViewById(R.id.tvBadge)
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar) // 🔥 NUEVO


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_preview, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        println("🔥 ADAPTER EJECUTANDO")


        val typeface = ResourcesCompat.getFont(holder.itemView.context, R.font.poppins_medium)
        val chat = chatList[position]
        val inicial = chat.name.trim().firstOrNull()?.uppercase() ?: "?"


        holder.tvName.text        = chat.name
        holder.tvLastMessage.text = chat.lastMessage
        holder.tvTime.text        = chat.time
        holder.tvAvatar.text = inicial
        holder.tvName.typeface = typeface
        holder.tvLastMessage.typeface = typeface
        holder.tvTime.typeface = typeface
        holder.tvBadge.typeface = typeface
        holder.tvAvatar.typeface = typeface

        // 🔥 AQUÍ VIVE TU BADGE
        if (chat.unreadCount > 0) {
            holder.tvBadge.visibility = View.VISIBLE
            holder.tvBadge.text = chat.unreadCount.toString()
        } else {
            holder.tvBadge.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onChatClick(chat) }
    }


    override fun getItemCount() = chatList.size

    fun filter(text: String) {
        chatList.clear()

        if (text.isEmpty()) {
            chatList.addAll(fullList)
        } else {
            val searchText = text.lowercase()

            for (item in fullList) {
                if (item.name.lowercase().contains(searchText) ||
                    item.lastMessage.lowercase().contains(searchText)
                ) {
                    chatList.add(item)
                }
            }
        }

        notifyDataSetChanged()
    }
}