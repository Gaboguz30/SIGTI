package com.auvenix.sigti.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.provider.catalog.ServiceCatalog
import java.util.Locale

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
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPublicServicePrice)

        fun bind(service: ServiceCatalog) {
            tvName.text = service.name

            if (service.description.isEmpty()) {
                tvDesc.visibility = View.GONE
            } else {
                tvDesc.visibility = View.VISIBLE
                tvDesc.text = service.description
            }

            tvPrice.text = String.format(Locale.getDefault(), "$%.2f", service.price)
        }
    }
}