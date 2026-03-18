package com.auvenix.sigti.ui.provider.chat // <-- Revisa que sea tu ruta correcta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

// 1. La "cajita" donde guardaremos los datos de cada chat
data class ChatModel(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String
)

// 2. El trabajador que pega los datos en el molde
class ChatListAdapter(
    private val chatList: List<ChatModel>,
    private val onChatClick: (ChatModel) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvUserName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_list, parent, false) // <-- Usa tu molde
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.tvName.text = chat.name
        holder.tvLastMessage.text = chat.lastMessage
        holder.tvTime.text = chat.time

        // Si le pican a un chat, avisamos a la pantalla
        holder.itemView.setOnClickListener {
            onChatClick(chat)
        }
    }

    override fun getItemCount(): Int = chatList.size
}