package com.auvenix.sigti.ui.provider.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

// 1. El modelo de datos real
data class RequestModel(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val title: String = "",
    val priceOffer: Double = 0.0,
    val fecha: String = "",
    val status: String = ""
)

// 2. El adaptador
class RequestAdapter(
    private val requestList: List<RequestModel>,
    private val onViewOffer: (RequestModel) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requestList[position])
    }

    override fun getItemCount(): Int = requestList.size

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvRequestTitle)
        private val tvClient: TextView = itemView.findViewById(R.id.tvRequestClient)
        private val tvOffer: TextView = itemView.findViewById(R.id.tvRequestOffer)
        private val btnViewOffer: MaterialButton = itemView.findViewById(R.id.btnViewOffer)

        fun bind(request: RequestModel) {
            tvTitle.text = request.title
            tvClient.text = "Cliente: ${request.clientName}"
            tvOffer.text = String.format(Locale.getDefault(), "Oferta: $%.2f", request.priceOffer)

            btnViewOffer.setOnClickListener { onViewOffer(request) }
        }
    }
}