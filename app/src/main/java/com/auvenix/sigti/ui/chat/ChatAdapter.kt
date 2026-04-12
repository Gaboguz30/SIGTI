package com.auvenix.sigti.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import android.widget.ImageView
import com.bumptech.glide.Glide
import android.content.Intent
import android.net.Uri

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
        val imgLeft: ImageView = itemView.findViewById(R.id.imgLeft)
        val imgRight: ImageView = itemView.findViewById(R.id.imgRight)
        val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        if (message.mine) {

            holder.llRightChat.visibility = View.VISIBLE
            holder.llLeftChat.visibility = View.GONE

            if (message.type == "image") {

                Glide.with(holder.itemView.context)
                    .load(message.message)
                    .into(holder.imgRight)

                holder.imgRight.visibility = View.VISIBLE
                holder.tvRightMessage.visibility = View.GONE

                holder.imgRight.setOnClickListener {
                    val intent = Intent(holder.itemView.context, FullScreenImageActivity::class.java)
                    intent.putExtra("imageUrl", message.message)
                    holder.itemView.context.startActivity(intent)
                }

            } else if (message.type == "file") {

                holder.imgRight.visibility = View.GONE
                holder.tvRightMessage.visibility = View.VISIBLE
                holder.tvRightMessage.text = "Archivo"

                holder.tvRightMessage.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message.message))
                    holder.itemView.context.startActivity(intent)
                }

            } else {

                holder.imgRight.visibility = View.GONE
                holder.tvRightMessage.visibility = View.VISIBLE
                holder.tvRightMessage.text = message.message
            }

            holder.tvRightTime.text = message.time

            if (message.seen) {
                holder.imgCheck.setImageResource(R.drawable.ic_check_double)
                holder.imgCheck.setColorFilter(android.graphics.Color.WHITE)
            } else {
                holder.imgCheck.setImageResource(R.drawable.ic_check_single)
                holder.imgCheck.setColorFilter(android.graphics.Color.WHITE)
            }

        } else {

            holder.llLeftChat.visibility = View.VISIBLE
            holder.llRightChat.visibility = View.GONE

            if (message.type == "image") {

                Glide.with(holder.itemView.context)
                    .load(message.message)
                    .into(holder.imgLeft)

                holder.imgLeft.visibility = View.VISIBLE
                holder.tvLeftMessage.visibility = View.GONE

                holder.imgLeft.setOnClickListener {
                    val intent = Intent(holder.itemView.context, FullScreenImageActivity::class.java)
                    intent.putExtra("imageUrl", message.message)
                    holder.itemView.context.startActivity(intent)
                }

            } else if (message.type == "file") {

                holder.imgLeft.visibility = View.GONE
                holder.tvLeftMessage.visibility = View.VISIBLE
                holder.tvLeftMessage.text = "📎 Archivo"

                holder.tvLeftMessage.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message.message))
                    holder.itemView.context.startActivity(intent)
                }

            } else {

                holder.imgLeft.visibility = View.GONE
                holder.tvLeftMessage.visibility = View.VISIBLE
                holder.tvLeftMessage.text = message.message
            }

            holder.tvLeftTime.text = message.time
        }
    }

    override fun getItemCount() = messages.size
}