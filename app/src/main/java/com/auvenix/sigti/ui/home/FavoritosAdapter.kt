package com.auvenix.sigti.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.bumptech.glide.Glide
import android.graphics.Color

data class FavoriteWorker(
    val favoriteDocId: String,
    val providerId: String,
    val name: String,
    val profession: String,
    val photoUrl: String,
    val availability: String // 🔥 NUEVO
)

class FavoritosAdapter(
    private val lista: List<FavoriteWorker>,
    private val onRemoveClick: (FavoriteWorker) -> Unit
) : RecyclerView.Adapter<FavoritosAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtServicio: TextView = itemView.findViewById(R.id.txtServicio)
        val btnFavorito: ImageView = itemView.findViewById(R.id.btnFavorito)
        val imgFoto: ImageView = itemView.findViewById(R.id.imgFoto) // 🔥 NUEVO
        val txtDisponibilidad: TextView = itemView.findViewById(R.id.txtDisponibilidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false) // 🔥 Usa tu nuevo diseño
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fav = lista[position]

        holder.txtNombre.text = fav.name
        holder.txtServicio.text = fav.profession

        // 🔥 CARGAR IMAGEN
        Glide.with(holder.itemView.context)
            .load(fav.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .circleCrop()
            .into(holder.imgFoto)

        holder.txtDisponibilidad.text = fav.availability

        if (fav.availability == "Disponible") {
            holder.txtDisponibilidad.setTextColor(Color.parseColor("#16A34A"))
        } else {
            holder.txtDisponibilidad.setTextColor(Color.parseColor("#DC2626"))
        }

        holder.btnFavorito.setOnClickListener {
            onRemoveClick(fav)
        }
    }
}