package com.auvenix.sigti.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.databinding.ItemServiceBinding

class ServiceAdapter(private val services: List<ServiceItem>) :
    RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(val binding: ItemServiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.binding.tvServiceName.text = service.name
        holder.binding.tvServiceDesc.text = service.description
        holder.binding.tvServicePrice.text = service.price
    }

    override fun getItemCount(): Int = services.size
}