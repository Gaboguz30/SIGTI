package com.auvenix.sigti.notifications

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.models.NotificationModel
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(
    private val notifications: List<NotificationModel>
) : RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        // 🔥 Asegúrate de que guardaste el XML de la Opción 1 con este nombre:
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotifViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle = itemView.findViewById<TextView>(R.id.tvNotifTitle)
        private val tvBody = itemView.findViewById<TextView>(R.id.tvNotifBody)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvNotifTime)
        private val vUnreadDot = itemView.findViewById<View>(R.id.vUnreadDot)

        // Elementos de la Opción 1
        private val ivNotifIcon = itemView.findViewById<ImageView>(R.id.ivNotifIcon)
        private val flIconContainer = itemView.findViewById<FrameLayout>(R.id.flIconContainer)

        fun bind(notif: NotificationModel) {
            tvTitle.text = notif.title
            tvBody.text = notif.body

            // FECHA: De Firebase a texto (Ej: 14:30)
            notif.timestamp?.toDate()?.let { date ->
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                tvTime.text = formatter.format(date)
            } ?: run {
                tvTime.text = "Ahora"
            }

            // PUNTITO ROJO
            vUnreadDot.visibility = if (notif.is_read) View.GONE else View.VISIBLE

            // 🔥 COLORES CONTEXTUALES
            when(notif.type) {
                "PAGO" -> {
                    ivNotifIcon.setImageResource(R.drawable.icon_notification) // Cambiar por ic_pago si tienes
                    ivNotifIcon.setColorFilter(Color.parseColor("#166534"))
                    flIconContainer.setBackgroundResource(R.drawable.bg_icon_green)
                }
                "ALERTA" -> {
                    ivNotifIcon.setImageResource(R.drawable.icon_notification) // Cambiar por ic_alerta si tienes
                    ivNotifIcon.setColorFilter(Color.parseColor("#991B1B"))
                    flIconContainer.setBackgroundResource(R.drawable.bg_icon_red)
                }
                else -> { // SERVICE_UPDATE, INFO, etc.
                    ivNotifIcon.setImageResource(R.drawable.icon_notification)
                    ivNotifIcon.setColorFilter(Color.parseColor("#1E40AF"))
                    flIconContainer.setBackgroundResource(R.drawable.bg_icon_blue)
                }
            }
        }
    }
}