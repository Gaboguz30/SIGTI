package com.auvenix.sigti.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton

data class NotifRequestModel(
    val id: String = "",
    val providerName: String = "",
    val title: String = "",
    val finalPrice: Double = 0.0,
    val status: String = ""
)

class NotificationAdapter(
    private val notifList: List<NotifRequestModel>,
    private val onConfirm: (String) -> Unit,
    private val onReject: (String, String) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotifViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        holder.bind(notifList[position])
    }

    override fun getItemCount(): Int = notifList.size

    inner class NotifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvType: TextView = itemView.findViewById(R.id.tvNotifType)
        private val tvTime: TextView = itemView.findViewById(R.id.tvNotifTime)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNotifTitle)
        private val tvBody: TextView = itemView.findViewById(R.id.tvNotifBody)

        private val llButtons: LinearLayout = itemView.findViewById(R.id.llNotifButtons)
        private val btnConfirm: MaterialButton = itemView.findViewById(R.id.btnNotifConfirm)
        private val btnReject: MaterialButton = itemView.findViewById(R.id.btnNotifReject)

        fun bind(notif: NotifRequestModel) {
            // El título principal siempre será el nombre del prestador
            tvTitle.text = notif.providerName
            tvTime.text = "Reciente"

            if (notif.status == "rejected") {
                tvType.text = "🚫 Trabajo Rechazado"
                tvType.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))

                tvBody.text = "El prestador ha rechazado tu solicitud para el trabajo: '${notif.title}'."

                llButtons.visibility = View.VISIBLE
                btnConfirm.visibility = View.GONE
                btnReject.text = "Descartar aviso"

                btnReject.setOnClickListener { onReject(notif.id, notif.status) }

            } else if (notif.status == "pending_client_confirmation") {
                tvType.text = "💼 Oferta de Trabajo"
                tvType.setTextColor(itemView.context.getColor(R.color.sigti_blue))

                tvBody.text = "Acordó un precio final de $${notif.finalPrice} por el trabajo '${notif.title}'.\n¿Deseas confirmar el trato?"

                llButtons.visibility = View.VISIBLE
                btnConfirm.visibility = View.VISIBLE
                btnReject.text = "Rechazar trato"

                btnConfirm.setOnClickListener { onConfirm(notif.id) }
                btnReject.setOnClickListener { onReject(notif.id, notif.status) }
            }
        }
    }
}