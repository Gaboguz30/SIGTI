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
    val description: String,
    val price: String
)

// 2. El Adaptador
class ServiceAdapter(
    private val serviceList: List<ServiceItem>
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvServiceTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvServiceDesc)
        val tvPrice: TextView = itemView.findViewById(R.id.tvServicePrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = serviceList[position]
        holder.tvTitle.text = service.title
        holder.tvDesc.text = service.description
        holder.tvPrice.text = "desde $${service.price}"
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }
}