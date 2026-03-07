package com.auvenix.sigti.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton

// 1. Un modelo de datos sencillo para probar (luego esto vendrá de tu Firebase)
data class Worker(
    val name: String,
    val profession: String,
    val rating: String,
    val price: String,
    val distance: String
)

// 2. El Adaptador
class WorkerAdapter(
    private val workerList: List<Worker>,
    private val onProfileClick: (Worker) -> Unit // Acción para cuando le den clic al botón
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    // 3. El ViewHolder (Busca los elementos en tu XML)
    class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvWorkerName)
        val tvProfession: TextView = itemView.findViewById(R.id.tvWorkerProfession)
        val tvRating: TextView = itemView.findViewById(R.id.tvWorkerRating)
        val tvPrice: TextView = itemView.findViewById(R.id.tvWorkerPrice)
        val tvDistance: TextView = itemView.findViewById(R.id.tvWorkerDistance)
        val btnViewProfile: MaterialButton = itemView.findViewById(R.id.btnViewProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workerList[position]

        // Asignamos los datos a la tarjeta
        holder.tvName.text = worker.name
        holder.tvProfession.text = worker.profession
        holder.tvRating.text = "★ ${worker.rating}"
        holder.tvPrice.text = "$${worker.price}\n/hora"
        holder.tvDistance.text = worker.distance

        // Evento del botón "Ver Perfil"
        holder.btnViewProfile.setOnClickListener {
            onProfileClick(worker)
        }
    }

    override fun getItemCount(): Int {
        return workerList.size
    }
}