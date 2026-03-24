package com.auvenix.sigti.ui.provider.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import java.util.Locale

class CatalogAdapter(
    private val serviceList: List<ServiceCatalog>,
    private val onEdit: (ServiceCatalog) -> Unit,
    private val onDelete: (ServiceCatalog) -> Unit
) : RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        // 🔥 CORRECCIÓN: Nombre exacto de tu archivo XML
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_provider_service, parent, false)
        return CatalogViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        holder.bind(serviceList[position])
    }

    override fun getItemCount(): Int = serviceList.size

    inner class CatalogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 🔥 CORRECCIÓN: IDs exactos de tu archivo XML
        private val tvName: TextView = itemView.findViewById(R.id.tvItemServiceName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvItemServiceDesc)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvItemServicePrice)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEditService)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteService)

        fun bind(service: ServiceCatalog) {
            tvName.text = service.name

            // Si no hay descripción, ocultamos el TextView
            if (service.description.isEmpty()) {
                tvDesc.visibility = View.GONE
            } else {
                tvDesc.visibility = View.VISIBLE
                tvDesc.text = service.description
            }

            // Formato de precio local (ej. $250.00)
            tvPrice.text = String.format(Locale.getDefault(), "$%.2f", service.price)

            btnEdit.setOnClickListener { onEdit(service) }
            btnDelete.setOnClickListener { onDelete(service) }
        }
    }
}