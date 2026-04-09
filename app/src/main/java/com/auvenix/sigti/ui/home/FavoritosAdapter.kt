package com.auvenix.sigti.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

// 🔥 Creamos un modelo rápido para manejar los favoritos
data class FavoriteWorker(
    val favoriteDocId: String, // El ID del documento en Firebase para poder borrarlo
    val providerId: String,
    val name: String,
    val profession: String
)

class FavoritosAdapter(
    private val lista: List<FavoriteWorker>,
    private val onRemoveClick: (FavoriteWorker) -> Unit
) : RecyclerView.Adapter<FavoritosAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtServicio: TextView = itemView.findViewById(R.id.txtServicio)
        val btnFavorito: ImageView = itemView.findViewById(R.id.btnFavorito)
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

        // Al darle clic al corazón en esta pantalla, disparamos la función de borrar
        holder.btnFavorito.setOnClickListener {
            onRemoveClick(fav)
        }
    }
}