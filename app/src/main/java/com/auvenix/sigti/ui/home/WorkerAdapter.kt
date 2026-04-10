package com.auvenix.sigti.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide

data class Worker(
    val uid: String = "",
    val name: String = "",
    val profession: String = "",
    val rating: String? = null,
    val price: String? = null,
    val distance: String? = null,
    val availability: String? = null,
    val photoUrl: String? = null

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

        val imgWorker: ImageView = itemView.findViewById(R.id.ivWorkerProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_worker, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workerList[position]
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // 🔥 PINTAMOS EL CORAZÓN INICIAL (Revisa si ya es favorito)
        db.collection("favorites")
            .whereEqualTo("user_uid", userId)
            .whereEqualTo("technician_uid", worker.uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // NO favorito → gris
                    holder.btnFavorito.setImageResource(R.drawable.ic_favorite_border)
                    holder.btnFavorito.setColorFilter(android.graphics.Color.parseColor("#9E9E9E"))
                } else {
                    // favorito → rojo
                    holder.btnFavorito.setImageResource(R.drawable.ic_favorite)
                    holder.btnFavorito.setColorFilter(android.graphics.Color.parseColor("#E53935"))
                }
            }

        holder.tvProfession.text = worker.profession
        holder.tvName.text = worker.name


                Glide.with(holder.itemView.context)
                    .load(worker.photoUrl)
                    .placeholder(R.drawable.ic_profile1)
                    .error(R.drawable.ic_profile1)
                    .circleCrop()
                    .into(holder.imgWorker)

        // RATING
        if (!worker.rating.isNullOrEmpty()) {
            holder.tvRating.text = worker.rating
            holder.tvRating.visibility = View.VISIBLE
        } else {
            holder.tvRating.visibility = View.GONE
        }

        // DISTANCIA
        if (!worker.distance.isNullOrEmpty()) {
            holder.tvDistance.text = worker.distance
            holder.tvDistance.visibility = View.VISIBLE
        } else {
            holder.tvDistance.visibility = View.GONE
        }

        // DISPONIBILIDAD
        if (!worker.availability.isNullOrEmpty()) {
            holder.tvAvailability.visibility = View.VISIBLE
            if (worker.availability == "Disponible") {
                holder.tvAvailability.text = "Disponible"
                holder.tvAvailability.setTextColor(android.graphics.Color.parseColor("#16A34A"))
            } else {
                holder.tvAvailability.text = "No disponible"
                holder.tvAvailability.setTextColor(android.graphics.Color.parseColor("#DC2626"))
            }
        } else {
            holder.tvAvailability.visibility = View.GONE
        }

        // OCULTAR STATS
        if (worker.rating.isNullOrEmpty() && worker.distance.isNullOrEmpty()) {
            holder.layoutStats.visibility = View.GONE
        } else {
            holder.layoutStats.visibility = View.VISIBLE
        }

        holder.btnViewProfile.setOnClickListener { onProfileClick(worker) }

        // 🔥 LA MAGIA DE LOS POPUPS AL TOCAR EL CORAZÓN
        holder.btnFavorito.setOnClickListener {
            // Checamos en qué estado está ahorita
            db.collection("favorites")
                .whereEqualTo("user_uid", userId)
                .whereEqualTo("technician_uid", worker.uid)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        mostrarDialogConfirmacion(
                            holder.itemView.context,
                            "Agregar a Favoritos",
                            "¿Deseas agregar a ${worker.name} a tus favoritos?"
                        ) {
                            val data = hashMapOf(
                                "user_uid" to userId,
                                "technician_uid" to worker.uid
                            )

                            db.collection("favorites").add(data)
                            holder.btnFavorito.setImageResource(R.drawable.ic_favorite)
                            holder.btnFavorito.setColorFilter(android.graphics.Color.parseColor("#E53935"))
                            animarCorazon(holder.btnFavorito)

                            Toast.makeText(holder.itemView.context, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        mostrarDialogConfirmacion(
                            holder.itemView.context,
                            "Quitar de Favoritos",
                            "¿Estás seguro de quitar a este prestador de tus favoritos?"
                        ) {
                            for (doc in result) {
                                db.collection("favorites").document(doc.id).delete()
                            }
                            holder.btnFavorito.setImageResource(R.drawable.ic_favorite_border)
                            holder.btnFavorito.setColorFilter(android.graphics.Color.parseColor("#9E9E9E"))
                            animarCorazon(holder.btnFavorito)
                            Toast.makeText(holder.itemView.context, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }
    private fun animarCorazon(view: ImageView) {
        view.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(150)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    override fun getItemCount(): Int = workerList.size
    private fun mostrarDialogConfirmacion(
        context: android.content.Context,
        titulo: String,
        mensaje: String,
        onConfirm: () -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_confirmacion, null)

        val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensaje)
        val btnSi = view.findViewById<TextView>(R.id.btnSi)
        val btnNo = view.findViewById<TextView>(R.id.btnNo)

        tvTitulo.text = titulo
        tvMensaje.text = mensaje

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        btnSi.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }
    }
}