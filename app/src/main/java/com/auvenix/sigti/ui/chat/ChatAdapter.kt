package com.auvenix.sigti.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

class ChatAdapter(
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llLeftChat: LinearLayout = itemView.findViewById(R.id.llLeftChat)
        val tvLeftMessage: TextView = itemView.findViewById(R.id.tvLeftMessage)
        val tvLeftTime: TextView = itemView.findViewById(R.id.tvLeftTime)

        val llRightChat: LinearLayout = itemView.findViewById(R.id.llRightChat)
        val tvRightMessage: TextView = itemView.findViewById(R.id.tvRightMessage)
        val tvRightTime: TextView = itemView.findViewById(R.id.tvRightTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        if (message.isMine) {
            holder.llRightChat.visibility = View.VISIBLE
            holder.tvRightMessage.text = message.text  // 🔥 ESTA LÍNEA ES CLAVE
            holder.tvRightTime.text =
                message.time + if (message.seen) " ✓✓" else " ✓"

            holder.llLeftChat.visibility = View.GONE
        } else {
            holder.llLeftChat.visibility = View.VISIBLE
            holder.tvLeftMessage.text = message.text  // 🔥 ESTA TAMBIÉN
            holder.tvLeftTime.text = message.time

            holder.llRightChat.visibility = View.GONE
        }
    }

    override fun getItemCount() = messages.size
}