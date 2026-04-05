package com.auvenix.sigti.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

// 1. Modelo de datos para cada servicio
data class ServiceItem(
    val title: String,
    val description: String
    // 🔥 Quitamos el price de aquí porque ya no existe en el diseño
)

// 2. El Adaptador
class ServiceAdapter(
    private val serviceList: List<ServiceItem>
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 🔥 CORREGIDO: Ahora apuntan a los IDs reales de item_public_service.xml
        val tvTitle: TextView = itemView.findViewById(R.id.tvPublicServiceName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvPublicServiceDesc)
        // 🔥 tvPrice eliminado para que no marque error
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_public_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = serviceList[position]
        holder.tvTitle.text = service.title

        // Validamos si hay descripción para ocultarla o mostrarla
        if (service.description.isEmpty()) {
            holder.tvDesc.visibility = View.GONE
        } else {
            holder.tvDesc.visibility = View.VISIBLE
            holder.tvDesc.text = service.description
        }
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }
}