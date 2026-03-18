package com.auvenix.sigti.ui.provider.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton

// El molde de datos (Cajita)
data class ServiceRequest(
    val id: String = "",
    val title: String = "",
    val clientName: String = "",
    val distance: String = ""
)

// El Adaptador que controla la lista
class RequestAdapter(
    private val requestList: List<ServiceRequest>,
    private val onAccept: (ServiceRequest) -> Unit,
    private val onReject: (ServiceRequest) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvRequestTitle)
        val tvClient: TextView = view.findViewById(R.id.tvRequestClient)
        val btnAccept: MaterialButton = view.findViewById(R.id.btnAccept)
        val btnReject: MaterialButton = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestList[position]
        holder.tvTitle.text = request.title
        holder.tvClient.text = "${request.clientName} • ${request.distance}"

        holder.btnAccept.setOnClickListener { onAccept(request) }
        holder.btnReject.setOnClickListener { onReject(request) }
    }

    override fun getItemCount() = requestList.size
}