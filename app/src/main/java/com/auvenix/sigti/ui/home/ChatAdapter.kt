package com.auvenix.sigti.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.databinding.ItemMessageReceivedBinding
import com.auvenix.sigti.databinding.ItemMessageSentBinding

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isMine) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            SentViewHolder(ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            ReceivedViewHolder(ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentViewHolder) holder.binding.tvMessage.text = message.text
        else if (holder is ReceivedViewHolder) holder.binding.tvMessage.text = message.text
    }

    override fun getItemCount(): Int = messages.size

    class SentViewHolder(val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root)
    class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root)
}