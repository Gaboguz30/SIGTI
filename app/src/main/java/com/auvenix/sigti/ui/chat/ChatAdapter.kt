package com.auvenix.sigti.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

data class ChatMessage(val text: String, val isMine: Boolean)

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llLeftChat: LinearLayout = itemView.findViewById(R.id.llLeftChat)
        val tvLeftMessage: TextView = itemView.findViewById(R.id.tvLeftMessage)

        val llRightChat: LinearLayout = itemView.findViewById(R.id.llRightChat)
        val tvRightMessage: TextView = itemView.findViewById(R.id.tvRightMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        if (message.isMine) {
            // Es mío (Azul - Derecha)
            holder.llRightChat.visibility = View.VISIBLE
            holder.tvRightMessage.text = message.text
            holder.llLeftChat.visibility = View.GONE
        } else {
            // Es de Vianca (Gris - Izquierda)
            holder.llLeftChat.visibility = View.VISIBLE
            holder.tvLeftMessage.text = message.text
            holder.llRightChat.visibility = View.GONE
        }
    }

    override fun getItemCount() = messages.size
}