package com.auvenix.sigti.ui.chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

data class ChatPreview(
    val serviceId  : String,
    val name       : String,
    val lastMessage: String,
    val time       : String
)

class ChatListAdapter(
    private val chats: List<ChatPreview>
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name   : TextView = view.findViewById(R.id.tvUserName)
        val message: TextView = view.findViewById(R.id.tvLastMessage)
        val time   : TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chats[position]

        holder.name.text    = chat.name
        holder.message.text = chat.lastMessage
        holder.time.text    = chat.time

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatDetailActivity::class.java).apply {
                putExtra("serviceId",   chat.serviceId)
                putExtra("contactName", chat.name)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = chats.size
}