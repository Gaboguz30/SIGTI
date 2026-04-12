package com.auvenix.sigti.ui.support

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

class ReportAdapter(private val reports: List<ReportModel>) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoleContext: TextView = view.findViewById(R.id.tvRoleContext) // 🔥 Nuevo
        val tvIncidentType: TextView = view.findViewById(R.id.tvIncidentType)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]

        holder.tvIncidentType.text = report.incidentType
        holder.tvDescription.text = report.description
        holder.tvDate.text = report.dateReport
        holder.tvStatusBadge.text = report.status

        // 🔥 LÓGICA DEL CONTEXTO (PRESTADOR VS CLIENTE) 🔥
        if (report.isProviderView) {
            holder.tvRoleContext.text = "REPORTE EN TU CONTRA"
            holder.tvRoleContext.setTextColor(Color.parseColor("#D32F2F")) // Rojo
        } else {
            holder.tvRoleContext.text = "REPORTE ENVIADO"
            holder.tvRoleContext.setTextColor(Color.parseColor("#1976D2")) // Azul
        }

        // Cambiar color de la etiqueta según el estado
        when (report.status.lowercase()) {
            "pendiente" -> {
                holder.tvStatusBadge.setTextColor(Color.parseColor("#F57C00")) // Naranja
            }
            "en revisión" -> {
                holder.tvStatusBadge.setTextColor(Color.parseColor("#1976D2")) // Azul
            }
            "resuelto", "penalizado" -> {
                holder.tvStatusBadge.setTextColor(Color.parseColor("#388E3C")) // Verde
            }
            else -> {
                holder.tvStatusBadge.setTextColor(Color.parseColor("#757575")) // Gris
            }
        }
    }

    override fun getItemCount(): Int = reports.size
}