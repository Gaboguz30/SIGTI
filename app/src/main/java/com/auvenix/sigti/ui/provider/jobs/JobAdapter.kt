package com.auvenix.sigti.ui.provider.jobs

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.provider.home.RequestModel
import com.google.android.material.button.MaterialButton
import java.util.Locale

class JobAdapter(
    private val jobList: List<RequestModel>,
    private val userRole: String,
    private val onChatClick: (RequestModel) -> Unit,
    private val onCompleteClick: (RequestModel) -> Unit
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobList[position])
    }

    override fun getItemCount(): Int = jobList.size

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvClient: TextView = itemView.findViewById(R.id.tvJobClient)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvJobDate)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvJobAddress)
        private val btnChat: ImageView = itemView.findViewById(R.id.btnJobChat)
        private val btnComplete: MaterialButton = itemView.findViewById(R.id.btnCompleteJob)
        private val tvWaiting: TextView = itemView.findViewById(R.id.tvJobWaiting)

        fun bind(job: RequestModel) {

            // 🔥 ADIÓS EMOJIS, HOLA DISEÑO LIMPIO
            if (userRole == "PRESTADOR") {
                tvClient.text = "Cliente: ${job.clientName}"
            } else {
                tvClient.text = "Técnico: ${job.providerName}"
            }

            tvTitle.text = job.title
            tvDate.text = "Fecha: ${job.fecha}"
            tvAddress.text = "Ver detalles en el chat"

            tvWaiting.setTextColor(Color.parseColor("#F57C00"))
            btnChat.visibility = View.VISIBLE

            // 🔥 LA MAGIA DE LOS ESTADOS CORREGIDA
            when (job.status) {
                "pending" -> { // 🔥 ESTO FALTABA PARA LAS NUEVAS SOLICITUDES
                    btnComplete.visibility = View.GONE
                    tvWaiting.visibility = View.VISIBLE
                    tvWaiting.text = if (userRole == "PRESTADOR") "Revisa tu pantalla de Inicio" else "A la espera de respuesta..."
                }
                "in_progress" -> {
                    if (userRole == "PRESTADOR") {
                        btnComplete.visibility = View.VISIBLE
                        btnComplete.text = "Finalizar"
                        tvWaiting.visibility = View.GONE
                    } else {
                        btnComplete.visibility = View.GONE
                        tvWaiting.visibility = View.VISIBLE
                        tvWaiting.text = "Trabajo en proceso..."
                    }
                }
                "pending_client_confirmation" -> {
                    if (userRole == "PRESTADOR") {
                        btnComplete.visibility = View.GONE
                        tvWaiting.visibility = View.VISIBLE
                        tvWaiting.text = "Esperando confirmación..."
                    } else {
                        btnComplete.visibility = View.VISIBLE
                        btnComplete.text = "Aceptar por $${job.priceOffer}"
                        tvWaiting.visibility = View.GONE
                    }
                }
                "completed" -> {
                    btnComplete.visibility = View.GONE
                    tvWaiting.visibility = View.GONE
                }
                "rejected" -> {
                    btnComplete.visibility = View.GONE
                    tvWaiting.visibility = View.VISIBLE
                    tvWaiting.text = "Solicitud Rechazada"
                    tvWaiting.setTextColor(Color.parseColor("#D32F2F"))
                    btnChat.visibility = View.GONE
                }
            }

            btnChat.setOnClickListener { onChatClick(job) }
            btnComplete.setOnClickListener { onCompleteClick(job) }
        }
    }
}