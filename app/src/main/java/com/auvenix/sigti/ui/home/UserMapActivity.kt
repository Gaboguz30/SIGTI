package com.auvenix.sigti.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityUserMapBinding
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView

class UserMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserMapBinding
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val lista = mutableListOf<FavoriteWorker>()
    private lateinit var adapter: FavoritosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.headerMain.tvHeaderTitle.text = "Favoritos"
        binding.bottomNavigation.selectedItemId = R.id.nav_map

        adapter = FavoritosAdapter(lista) { fav ->
            mostrarDialogConfirmacion(
                "Quitar de Favoritos",
                "¿Estás seguro de quitar a ${fav.name} de tus favoritos?"
            ) {
                db.collection("favorites").document(fav.favoriteDocId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                        cargarFavoritos()
                    }
            }
        }
        binding.rvFavoritos.layoutManager = LinearLayoutManager(this)
        binding.rvFavoritos.adapter = adapter

        cargarFavoritos()

        // 🔥 NAVEGACIÓN SUAVE
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { irAPantalla(HomeActivity::class.java); true }
                R.id.nav_map -> true
                R.id.nav_chat -> { irAPantalla(com.auvenix.sigti.ui.provider.chat.ProviderChatActivity::class.java); true }
                R.id.nav_jobs -> { irAPantalla(com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity::class.java); true }
                R.id.nav_profile -> { irAPantalla(ProfileActivity::class.java); true }
                else -> false
            }
        }
    }

    private fun cargarFavoritos() {
        db.collection("favorites")
            .whereEqualTo("user_uid", userId)
            .get()
            .addOnSuccessListener { result ->
                lista.clear()

                if (result.isEmpty) {
                    binding.tvEmptyFavoritos.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                binding.tvEmptyFavoritos.visibility = View.GONE

                for (doc in result) {
                    val favDocId = doc.id
                    val providerId = doc.getString("technician_uid") ?: ""

                    db.collection("users")
                        .document(providerId)
                        .get()
                        .addOnSuccessListener { prov ->
                            if (prov.exists()) {
                                val nombre = "${prov.getString("nombre") ?: ""} ${prov.getString("apPaterno") ?: ""}".trim()
                                val oficio = prov.getString("oficio") ?: "Servicio Profesional"

                                lista.add(FavoriteWorker(favDocId, providerId, nombre, oficio))
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando favoritos", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔥 LA FUNCIÓN MÁGICA
    private fun irAPantalla(activityClass: Class<*>) {
        if (this::class.java == activityClass) return
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
    private fun mostrarDialogConfirmacion(
        titulo: String,
        mensaje: String,
        onConfirm: () -> Unit
    ) {
        val view = layoutInflater.inflate(R.layout.dialog_confirmacion, null)

        val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensaje)
        val btnSi = view.findViewById<TextView>(R.id.btnSi)
        val btnNo = view.findViewById<TextView>(R.id.btnNo)

        tvTitulo.text = titulo
        tvMensaje.text = mensaje

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()

        // 🔥 animación
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