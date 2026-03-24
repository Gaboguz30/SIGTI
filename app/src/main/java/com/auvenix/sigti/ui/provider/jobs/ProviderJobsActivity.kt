package com.auvenix.sigti.ui.provider.jobs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderJobsBinding
import com.auvenix.sigti.ui.chat.ChatDetailActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.home.RequestModel
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProviderJobsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProviderJobsBinding
    private val db               = FirebaseFirestore.getInstance()
    private val auth             = FirebaseAuth.getInstance()
    private val jobList          = mutableListOf<RequestModel>()

    // 🔥 USAMOS EL NUEVO ADAPTADOR MAGICO
    private lateinit var adapter : JobAdapter

    private var activeListener   : ListenerRegistration? = null
    private var currentTab       = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupTabs()
        loadJobs(tab = 0)           // arranca en "En Progreso"
        setupBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        activeListener?.remove()
    }

    private fun setupTabs() {
        binding.tabLayoutJobs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                activeListener?.remove()
                loadJobs(currentTab)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadJobs(tab: Int) {
        val uid = auth.currentUser?.uid ?: return

        // 🔥 AQUÍ ESTÁ EL CAMBIO DE LA BASE DE DATOS
        val query = when (tab) {
            0 -> db.collection("requests")
                .whereEqualTo("providerId", uid)
                .whereIn("status", listOf("in_progress", "pending_client_confirmation")) // Trae ambos estados
            1 -> db.collection("requests")
                .whereEqualTo("providerId", uid)
                .whereEqualTo("status", "completed")
            else -> return
        }

        activeListener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            jobList.clear()

            for (doc in snapshot.documents) {
                jobList.add(
                    RequestModel(
                        id         = doc.id,
                        clientId   = doc.getString("clientId") ?: "",
                        clientName = doc.getString("clientName") ?: "Cliente",
                        title      = doc.getString("title") ?: "Sin título",
                        priceOffer = doc.getDouble("finalPrice") ?: 0.0,
                        fecha      = doc.getString("fecha") ?: "",
                        status     = doc.getString("status") ?: ""
                    )
                )
            }

            if (jobList.isEmpty()) {
                binding.tvTabContentStatus.visibility = View.VISIBLE
                binding.tvTabContentStatus.text = when (tab) {
                    0    -> "No tienes trabajos activos ni en espera" // Texto actualizado
                    1    -> "Tu historial está vacío"
                    else -> ""
                }
            } else {
                binding.tvTabContentStatus.visibility = View.GONE
            }

            adapter.notifyDataSetChanged()
        }
    }

    private fun setupRecyclerView() {
        adapter = JobAdapter(
            jobList = jobList,
            onChatClick = { job ->
                // 🔥 MAGIA: Abre el chat directo con el cliente
                val intent = Intent(this, ChatDetailActivity::class.java)
                intent.putExtra("serviceId", job.clientId)
                intent.putExtra("contactName", job.clientName)
                startActivity(intent)
            },
            onCompleteClick = { job ->
                // 🔥 MAGIA: Popup de confirmación de pago
                AlertDialog.Builder(this)
                    .setTitle("Finalizar Trabajo")
                    .setMessage("¿Estás seguro de marcar '${job.title}' como terminado?\n\nConfirma que ya recibiste el pago de $${job.priceOffer}.")
                    .setPositiveButton("Sí, ya cobré") { _, _ ->
                        marcarComoCompletado(job.id)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.rvJobsList.layoutManager = LinearLayoutManager(this)
        binding.rvJobsList.adapter = adapter
    }

    private fun marcarComoCompletado(jobId: String) {
        db.collection("requests").document(jobId).update("status", "completed")
            .addOnSuccessListener {
                Toast.makeText(this, "¡Felicidades por cerrar el trato! 💸", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_jobs
        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home -> { startActivity(Intent(this, ProviderHomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_provider_jobs -> true
                R.id.nav_provider_chat -> { startActivity(Intent(this, ProviderChatActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_provider_catalog -> { startActivity(Intent(this, ProviderCatalogActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_provider_profile -> { startActivity(Intent(this, ProviderProfileActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                else -> false
            }
        }
    }
}