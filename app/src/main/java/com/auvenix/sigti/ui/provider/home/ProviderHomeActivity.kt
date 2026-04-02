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

// Imports de navegación
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity

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

        setupRecyclerView()
        escucharNuevasSolicitudes()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        adapter = RequestAdapter(requestList) { request ->
            // Cuando le pican a "Ver Oferta", abrimos la pantalla de detalle pasándole el ID
            val intent = Intent(this, RequestDetailActivity::class.java)
            intent.putExtra("EXTRA_REQUEST_ID", request.id)
            startActivity(intent)
        }
        // 🔥 CORREGIDO: Usamos rvRequests en lugar de rvNewRequests
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter
    }

    private fun escucharNuevasSolicitudes() {
        val myUid = auth.currentUser?.uid ?: return

        // 🚨 OJO: Solo buscamos las que sean para mí y estén en "pending"
        db.collection("requests")
            .whereEqualTo("providerId", myUid)
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ProviderHome", "Error escuchando solicitudes", e)
                    return@addSnapshotListener
                }

                requestList.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    // Hay solicitudes: Ocultar textos de "vacío" si tienes, y mostrar la lista
                    binding.rvRequests.visibility = View.VISIBLE
                    for (doc in snapshots) {
                        val req = doc.toObject(RequestModel::class.java).copy(id = doc.id)
                        requestList.add(req)
                    }
                } else {
                    // No hay solicitudes nuevas
                    binding.rvRequests.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_home
        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home -> true
                R.id.nav_provider_jobs -> { startActivity(Intent(this, ProviderJobsActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_provider_chat -> { startActivity(Intent(this, ProviderChatActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_provider_catalog -> { startActivity(Intent(this, ProviderCatalogActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_provider_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                else -> false
            }
        }
    }
}