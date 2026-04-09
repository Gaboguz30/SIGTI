package com.auvenix.sigti.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        val btnFavorito: ImageView = itemView.findViewById(R.id.btnFavorito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)

            .inflate(R.layout.item_worker, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {

        val worker = workerList[position]
        // 🔥 FIREBASE
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        // 🔥 VERIFICAR SI YA ES FAVORITO
        db.collection("favorites")
            .whereEqualTo("user_uid", userId)
            .whereEqualTo("technician_uid", worker.uid)
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {
                    holder.btnFavorito.setImageResource(R.drawable.ic_favorite_border)
                } else {
                    holder.btnFavorito.setImageResource(R.drawable.ic_favorite)
                }
            }

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

        // 🔥 TOGGLE FAVORITO
        holder.btnFavorito.setOnClickListener {

            db.collection("favorites")
                .whereEqualTo("user_uid", userId)
                .whereEqualTo("technician_uid", worker.uid)
                .get()
                .addOnSuccessListener { result ->

                    if (result.isEmpty) {
                        // ❤️ GUARDAR
                        val data = hashMapOf(
                            "user_uid" to userId,
                            "technician_uid" to worker.uid
                        )

                        db.collection("favorites").add(data)
                        holder.btnFavorito.setImageResource(R.drawable.ic_favorite)

                    } else {
                        // ❌ ELIMINAR
                        for (doc in result) {
                            db.collection("favorites").document(doc.id).delete()
                        }
                        holder.btnFavorito.setImageResource(R.drawable.ic_favorite_border)
                    }
                }
        }
    }

    override fun getItemCount(): Int = workerList.size
}