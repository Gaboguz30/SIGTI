package com.auvenix.sigti.ui.provider.jobs

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
    private val userRole: String, // 🔥 NUEVO: Recibe el rol del usuario
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
        private val tvPrice: TextView = itemView.findViewById(R.id.tvJobPrice)
        private val btnChat: ImageView = itemView.findViewById(R.id.btnJobChat)
        private val btnComplete: MaterialButton = itemView.findViewById(R.id.btnCompleteJob)
        private val tvWaiting: TextView = itemView.findViewById(R.id.tvJobWaiting)

        fun bind(job: RequestModel) {
            // Si es prestador dice Cliente, si es cliente dice Técnico
            tvClient.text = if (userRole == "PRESTADOR") "👤 Cliente: ${job.clientName}" else "👷 Técnico: ${job.clientName}"

            tvTitle.text = job.title
            tvDate.text = "📅 Fecha: ${job.fecha}"
            tvAddress.text = "📍 Ver detalles en el chat"
            tvPrice.text = String.format(Locale.getDefault(), "$%.2f", job.priceOffer)

            // 🔥 LA MAGIA DE LOS ESTADOS DEPENDIENDO DEL ROL
            when (job.status) {
                "in_progress" -> {
                    if (userRole == "PRESTADOR") {
                        // El técnico ve el botón para cobrar/finalizar
                        btnComplete.visibility = View.VISIBLE
                        tvWaiting.visibility = View.GONE
                    } else {
                        // El cliente solo ve un aviso de que están trabajando
                        btnComplete.visibility = View.GONE
                        tvWaiting.visibility = View.VISIBLE
                        tvWaiting.text = "🔧 Trabajo en proceso..."
                    }
                }
                "pending_client_confirmation" -> {
                    btnComplete.visibility = View.GONE
                    tvWaiting.visibility = View.VISIBLE
                    tvWaiting.text = if (userRole == "PRESTADOR") "Esperando respuesta del cliente..." else "Revisa el chat para confirmar"
                }
                "completed" -> {
                    // Ya se acabó el jale. Ocultamos ambos.
                    btnComplete.visibility = View.GONE
                    tvWaiting.visibility = View.GONE
                }
            }

            btnChat.setOnClickListener { onChatClick(job) }
            btnComplete.setOnClickListener { onCompleteClick(job) }
        }
    }
}