package com.auvenix.sigti.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton

// 🔥 IMPORTANTE: ahora tiene UID
data class Worker(
    val uid: String = "",
    val name: String = "",
    val profession: String = "",
    val rating: String = "",
    val price: String = "",
    val distance: String = ""
)

class WorkerAdapter(
    private val workerList: List<Worker>,
    private val onProfileClick: (Worker) -> Unit
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

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

        holder.tvName.text = worker.name
        holder.tvProfession.text = worker.profession
        holder.tvRating.text = "★ ${worker.rating}"
        holder.tvPrice.text = "$${worker.price}/hora"
        holder.tvDistance.text = worker.distance

        holder.btnViewProfile.setOnClickListener {
            onProfileClick(worker)
        }
    }

    override fun getItemCount(): Int = workerList.size
}