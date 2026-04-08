package com.auvenix.sigti.ui.provider.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderHomeBinding
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.android.material.bottomnavigation.BottomNavigationView

// Imports de navegación
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity

class ProviderHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderHomeBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val requestList = mutableListOf<RequestModel>()
    private lateinit var adapter: RequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserData() // 🔥 Jalamos el nombre del usuario
        setupRecyclerView()
        escucharNuevasSolicitudes() // 🔥 Escucha datos y maneja el Loader
        setupBottomNavigation()
        cargarCalificacion()

        // 🔥 CONECTAR BOTÓN DE NOTIFICACIONES (PRESTADOR)
        val btnNotifProvider = findViewById<View>(R.id.btnNotificationsProvider)
        btnNotifProvider.setOnClickListener {
            // Levantamos el MISMO BottomSheet (Es universal)
            val bottomSheet = com.auvenix.sigti.notifications.NotificationsBottomSheet()
            bottomSheet.show(supportFragmentManager, "NotificationsSheet")
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nombre = doc.getString("nombre") ?: "Prestador"
                binding.tvProviderName.text = "$nombre!"
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = RequestAdapter(requestList) { request ->
            val intent = Intent(this, RequestDetailActivity::class.java)
            intent.putExtra("EXTRA_REQUEST_ID", request.id)
            startActivity(intent)
        }
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter
    }

    private fun escucharNuevasSolicitudes() {
        val myUid = auth.currentUser?.uid ?: return

        // Encendemos el loader y ocultamos listas al inicio
        binding.pbLoading.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.rvRequests.visibility = View.GONE

        db.collection("requests")
            .whereEqualTo("providerId", myUid)
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                // Apagamos el loader en cuanto Firebase nos responde
                binding.pbLoading.visibility = View.GONE

                if (e != null) {
                    Log.w("ProviderHome", "Error escuchando solicitudes", e)
                    binding.tvEmptyState.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                requestList.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    binding.rvRequests.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                    for (doc in snapshots) {
                        val req = doc.toObject(RequestModel::class.java).copy(id = doc.id)
                        requestList.add(req)
                    }
                } else {
                    binding.rvRequests.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationProvider)
        bottomNavigation.selectedItemId = R.id.nav_home_provider

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home_provider -> true
                R.id.nav_catalog -> {
                    startActivity(Intent(this, ProviderCatalogActivity::class.java)) // <-- Pon tu clase de catálogo
                    finish(); true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ProviderChatActivity::class.java))
                    true
                }
                // 🔥 AQUÍ CONECTAMOS EXACTAMENTE LA MISMA PANTALLA QUE EL USUARIO
                R.id.nav_jobs -> {
                    startActivity(Intent(this, ProviderJobsActivity::class.java)) // <-- MISMA CLASE
                    finish(); true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish(); true
                }
                else -> false
            }
        }
    }

    private fun cargarCalificacion() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("reviews")
            .whereEqualTo("technician_uid", myUid)
            .get()
            .addOnSuccessListener { result ->

                var total = 0f
                var count = 0

                for (doc in result) {
                    val rating = doc.getLong("rating")?.toFloat() ?: 0f
                    total += rating
                    count++
                }

                val promedio = if (count > 0) total / count else 0f

                binding.tvRating.text = String.format("%.1f", promedio)
            }
    }
}