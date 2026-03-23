package com.auvenix.sigti.ui.provider.chat // <-- Revisa tu ruta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

// El modelo de datos para cada mensajito
data class MessageModel(
    val text: String,
    val time: String,
    val isMine: Boolean // true = tu burbuja (derecha), false = cliente (izquierda)
)

class ChatMessageAdapter(private val messageList: List<MessageModel>) :
    RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llLeftChat: LinearLayout = view.findViewById(R.id.llLeftChat)
        val tvLeftMessage: TextView = view.findViewById(R.id.tvLeftMessage)
        val tvLeftTime: TextView = view.findViewById(R.id.tvLeftTime)

        val llRightChat: LinearLayout = view.findViewById(R.id.llRightChat)
        val tvRightMessage: TextView = view.findViewById(R.id.tvRightMessage)
        val tvRightTime: TextView = view.findViewById(R.id.tvRightTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // Conectamos tu molde de burbujas que hiciste
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messageList[position]

        if (msg.isMine) {
            // Es tuyo: Mostramos la derecha, ocultamos la izquierda
            holder.llRightChat.visibility = View.VISIBLE
            holder.llLeftChat.visibility = View.GONE
            holder.tvRightMessage.text = msg.text
            holder.tvRightTime.text = msg.time
        } else {
            // Es del cliente: Mostramos la izquierda, ocultamos la derecha
            holder.llLeftChat.visibility = View.VISIBLE
            holder.llRightChat.visibility = View.GONE
            holder.tvLeftMessage.text = msg.text
            holder.tvLeftTime.text = msg.time
        }
    }

    override fun getItemCount() = messageList.size
}