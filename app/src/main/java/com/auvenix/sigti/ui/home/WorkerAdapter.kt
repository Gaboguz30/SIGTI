package com.auvenix.sigti.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton

data class Worker(
    val uid: String = "",
    val name: String = "",
    val profession: String = "",
    val rating: String? = null,
    val price: String? = null,
    val distance: String? = null,
    val availability: String? = null
)

class WorkerAdapter(
    private val workerList: List<Worker>,
    private val onProfileClick: (Worker) -> Unit
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvWorkerName)
        val tvRating: TextView = itemView.findViewById(R.id.tvWorkerRating)
        val tvDistance: TextView = itemView.findViewById(R.id.tvWorkerDistance)
        val tvAvailability: TextView = itemView.findViewById(R.id.tvWorkerAvailability)
        val layoutStats: View = itemView.findViewById(R.id.layoutStats)
        val btnViewProfile: MaterialButton = itemView.findViewById(R.id.btnViewProfile)
        val tvProfession: TextView = itemView.findViewById(R.id.tvWorkerProfession)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {

        val worker = workerList[position]

        holder.tvProfession.text = worker.profession

        // 🔹 NOMBRE
        holder.tvName.text = worker.name

        // 🔹 RATING
        if (!worker.rating.isNullOrEmpty()) {
            holder.tvRating.text = worker.rating
            holder.tvRating.visibility = View.VISIBLE
        } else {
            holder.tvRating.visibility = View.GONE
        }

        // 🔹 DISTANCIA
        if (!worker.distance.isNullOrEmpty()) {
            holder.tvDistance.text = worker.distance
            holder.tvDistance.visibility = View.VISIBLE
        } else {
            holder.tvDistance.visibility = View.GONE
        }

        // 🔹 DISPONIBILIDAD (PREPARADO)
        if (!worker.availability.isNullOrEmpty()) {

            holder.tvAvailability.visibility = View.VISIBLE

            if (worker.availability == "Disponible") {
                holder.tvAvailability.text = "Disponible"
                holder.tvAvailability.setTextColor(android.graphics.Color.parseColor("#16A34A")) // verde
            } else {
                holder.tvAvailability.text = "No disponible"
                holder.tvAvailability.setTextColor(android.graphics.Color.parseColor("#DC2626")) // rojo
            }

        } else {
            holder.tvAvailability.visibility = View.GONE
        }

        // 🔥 OCULTAR TODO EL BLOQUE SI NO HAY DATOS
        if (worker.rating.isNullOrEmpty() && worker.distance.isNullOrEmpty()) {
            holder.layoutStats.visibility = View.GONE
        } else {
            holder.layoutStats.visibility = View.VISIBLE
        }

        holder.btnViewProfile.setOnClickListener {
            onProfileClick(worker)
        }
    }

    override fun getItemCount(): Int = workerList.size
}