package com.auvenix.sigti.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityUserMapBinding
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserMapBinding
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val lista = mutableListOf<String>()
    private lateinit var adapter: FavoritosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

// 🔥 TITULO HEADER
        binding.headerMain.tvHeaderTitle.text = "Favoritos"

        // 🔥 ICONO ACTIVO (puedes cambiar si tienes otro id)
        binding.bottomNavigation.selectedItemId = R.id.nav_map

        // 🔥 RECYCLER
        adapter = FavoritosAdapter(lista)
        binding.rvFavoritos.layoutManager = LinearLayoutManager(this)
        binding.rvFavoritos.adapter = adapter

        // 🔥 CARGAR DATOS
        cargarFavoritos()

        // 🔥 NAV
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_map -> true

                R.id.nav_chat -> {
                    startActivity(Intent(this, com.auvenix.sigti.ui.provider.chat.ProviderChatActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_jobs -> {
                    startActivity(Intent(this, com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    // 🔥 FAVORITOS
    private fun cargarFavoritos() {

        db.collection("favorites")
            .whereEqualTo("user_uid", userId)
            .get()
            .addOnSuccessListener { result ->

                lista.clear()

                if (result.isEmpty) {
                    binding.tvEmptyFavoritos.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                binding.tvEmptyFavoritos.visibility = View.GONE

                for (doc in result) {
                    val providerId = doc.getString("technician_uid") ?: ""

                    db.collection("providers")
                        .document(providerId)
                        .get()
                        .addOnSuccessListener { prov ->

                            val nombre = (prov.getString("nombre") ?: "") + " " +
                                    (prov.getString("apPaterno") ?: "")
                            lista.add(nombre)
                            adapter.notifyDataSetChanged()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando favoritos", Toast.LENGTH_SHORT).show()
            }
    }
}