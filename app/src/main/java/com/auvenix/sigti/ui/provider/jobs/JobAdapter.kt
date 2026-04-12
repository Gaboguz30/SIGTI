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

enum class JobStatus {
    PENDING,
    IN_PROGRESS,
    PENDING_CLIENT_CONFIRMATION,
    COMPLETED,
    REJECTED;

    companion object {
        fun from(status: String): JobStatus {
            return try {
                valueOf(status.uppercase(Locale.getDefault()))
            } catch (e: Exception) {
                PENDING
            }
        }
    }
}

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

        private fun showWaiting(text: String, colorHex: String, bgRes: Int) {
            tvWaiting.visibility = View.VISIBLE
            tvWaiting.text = text
            tvWaiting.setTextColor(Color.parseColor(colorHex))
            tvWaiting.setBackgroundResource(bgRes)
        }

        private fun showButton(text: String) {
            btnComplete.visibility = View.VISIBLE
            btnComplete.text = text
            animateButton(btnComplete)
        }

        private fun animateButton(view: View) {
            view.scaleX = 0.8f
            view.scaleY = 0.8f
            view.alpha = 0f

            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(200)
                .start()
        }

        private fun setStatusIcon(status: JobStatus) {
            when (status) {
                JobStatus.PENDING -> btnChat.setImageResource(R.drawable.ic_clock1)
                JobStatus.IN_PROGRESS -> btnChat.setImageResource(R.drawable.ic_work)
                JobStatus.PENDING_CLIENT_CONFIRMATION -> btnChat.setImageResource(R.drawable.ic_warning)
                JobStatus.COMPLETED -> btnChat.setImageResource(R.drawable.ic_check)
                JobStatus.REJECTED -> btnChat.setImageResource(R.drawable.ic_cancel)
            }
        }

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

            val status = JobStatus.from(job.status)
            setStatusIcon(status)

            btnComplete.visibility = View.GONE
            tvWaiting.visibility = View.GONE
            btnChat.visibility = View.VISIBLE

            when (status) {

                JobStatus.PENDING -> {
                    showWaiting(
                        "Esperando confirmación",
                        "#EF6C00",
                        R.drawable.bg_status_pending
                    )
                }

                JobStatus.IN_PROGRESS -> {
                    if (userRole == "PRESTADOR") {
                        showButton("Finalizar")
                    } else {
                        showWaiting(
                            "En proceso",
                            "#1976D2",
                            R.drawable.bg_status_progress
                        )
                    }
                }

                JobStatus.PENDING_CLIENT_CONFIRMATION -> {
                    if (userRole == "PRESTADOR") {
                        showWaiting(
                            "Esperando confirmación...",
                            "#F57C00",
                            R.drawable.bg_status_pending
                        )
                    } else {
                        showButton("Aceptar por $${job.priceOffer}")
                    }
                }

                JobStatus.COMPLETED -> {
                    showWaiting(
                        "Finalizado",
                        "#388E3C",
                        R.drawable.bg_status_completed
                    )
                }

                JobStatus.REJECTED -> {
                    showWaiting(
                        "Solicitud rechazada",
                        "#D32F2F",
                        R.drawable.bg_status_rejected
                    )
                    btnChat.visibility = View.GONE
                }
            }

            btnChat.setOnClickListener { onChatClick(job) }
            btnComplete.setOnClickListener { onCompleteClick(job) }
        }
    }
}