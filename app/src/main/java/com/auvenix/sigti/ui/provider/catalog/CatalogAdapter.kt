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
    private val onDelete: (ServiceCatalog) -> Unit,
    private val onToggleStatus: (ServiceCatalog, Boolean) -> Unit // 🔥 AQUÍ RECIBIMOS EL NUEVO ENLACE
) : RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_provider_service, parent, false)
        return CatalogViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        holder.bind(serviceList[position])
    }

    override fun getItemCount(): Int = serviceList.size

    inner class CatalogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvItemServiceName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvItemServiceDesc)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEditService)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteService)
        private val btnToggleStatus: ImageView = itemView.findViewById(R.id.btnToggleStatus) // 🔥 EL BOTÓN LED

        fun bind(service: ServiceCatalog) {
            tvName.text = service.name

            if (service.description.isEmpty()) {
                tvDesc.visibility = View.GONE
            } else {
                tvDesc.visibility = View.VISIBLE
                tvDesc.text = service.description
            }


            // 🔥 1. PINTAMOS EL LED SEGÚN EL ESTADO ACTUAL
            if (service.active) {
                btnToggleStatus.setColorFilter(android.graphics.Color.parseColor("#4CAF50")) // Verde
            } else {
                btnToggleStatus.setColorFilter(android.graphics.Color.parseColor("#F44336")) // Rojo
            }

            // 🔥 2. MOSTRAMOS LA ALERTA MODERNA AL PICARLE AL LED
            btnToggleStatus.setOnClickListener { view ->
                val dialogView = LayoutInflater.from(view.context).inflate(R.layout.dialog_confirm_status, null)
                val builder = androidx.appcompat.app.AlertDialog.Builder(view.context)
                builder.setView(dialogView)
                val alertDialog = builder.create()
                // Hacemos el fondo transparente para que se vean las esquinas redondas
                alertDialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

                val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
                val btnNo = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogNo)
                val btnYes = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogYes)

                if (service.active) {
                    tvMessage.text = "¿Estás seguro de desactivar temporalmente este servicio de tu catálogo?"
                } else {
                    tvMessage.text = "¿Estás seguro de reactivar este servicio en tu catálogo?"
                }

                btnNo.setOnClickListener { alertDialog.dismiss() }

                btnYes.setOnClickListener {
                    alertDialog.dismiss()
                    // Disparamos la función que viaja hasta el Activity para actualizar Firebase
                    onToggleStatus(service, !service.active)
                }

                alertDialog.show()
            }

            btnEdit.setOnClickListener { onEdit(service) }
            btnDelete.setOnClickListener { onDelete(service) }
        }
    }
}