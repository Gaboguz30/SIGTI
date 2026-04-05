package com.auvenix.sigti.ui.profile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.provider.catalog.ServiceCatalog
import com.google.android.material.card.MaterialCardView

class PublicServiceAdapter(
    private val serviceList: List<ServiceCatalog>
) : RecyclerView.Adapter<PublicServiceAdapter.PublicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_public_service, parent, false)
        return PublicViewHolder(view)
    }

    override fun onBindViewHolder(holder: PublicViewHolder, position: Int) {
        holder.bind(serviceList[position])
    }

    override fun getItemCount(): Int = serviceList.size

    inner class PublicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvPublicServiceName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvPublicServiceDesc)
        private val cvStatusBadge: MaterialCardView = itemView.findViewById(R.id.cvStatusBadge)
        private val tvStatusBadgeText: TextView = itemView.findViewById(R.id.tvStatusBadgeText)
        private val llMainContent: LinearLayout = itemView.findViewById(R.id.llMainContent)

        fun bind(service: ServiceCatalog) {
            tvName.text = service.name

            if (service.description.isEmpty()) {
                tvDesc.visibility = View.GONE
            } else {
                tvDesc.visibility = View.VISIBLE
                tvDesc.text = service.description
            }

            // 🔥 MAGIA DE COLORES Y TEXTO DINÁMICO
            if (service.active) {
                // ACTIVO: Tarjeta normal, badge verde, letras blancas
                llMainContent.alpha = 1.0f
                cvStatusBadge.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                tvStatusBadgeText.text = "ACTIVO"
                tvStatusBadgeText.setTextColor(Color.WHITE)
            } else {
                // INACTIVO: Tarjeta opaca (grisácea), badge rojito, letras rojas
                llMainContent.alpha = 0.5f
                cvStatusBadge.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                tvStatusBadgeText.text = "INACTIVO"
                tvStatusBadgeText.setTextColor(Color.parseColor("#D32F2F"))
            }
        }
    }
}