package com.auvenix.sigti.ui.provider.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

// El modelo de datos
data class ServiceCatalog(
    var id: String = "", // ID del documento en Firebase
    val nombre: String = "",
    val precio: Double = 0.0
)

class CatalogAdapter(
    private val serviceList: List<ServiceCatalog>,
    private val onEdit: (ServiceCatalog) -> Unit,
    private val onDelete: (ServiceCatalog) -> Unit
) : RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {

    class CatalogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemServiceName)
        val tvPrice: TextView = view.findViewById(R.id.tvItemServicePrice)
        val btnEdit: ImageView = view.findViewById(R.id.btnEditService)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteService)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_provider_service, parent, false)
        return CatalogViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        val service = serviceList[position]
        holder.tvName.text = service.nombre
        holder.tvPrice.text = "$${service.precio}"

        holder.btnEdit.setOnClickListener { onEdit(service) }
        holder.btnDelete.setOnClickListener { onDelete(service) }
    }

    override fun getItemCount() = serviceList.size
}