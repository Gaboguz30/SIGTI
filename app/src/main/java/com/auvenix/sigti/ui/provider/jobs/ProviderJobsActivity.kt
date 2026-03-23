package com.auvenix.sigti.ui.provider.jobs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderJobsBinding
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.home.RequestAdapter
import com.auvenix.sigti.ui.provider.home.ServiceRequest
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProviderJobsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProviderJobsBinding
    private val db               = FirebaseFirestore.getInstance()
    private val auth             = FirebaseAuth.getInstance()
    private val jobList          = mutableListOf<ServiceRequest>()
    private lateinit var adapter : RequestAdapter
    private var activeListener   : ListenerRegistration? = null
    private var currentTab       = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupTabs()
        loadJobs(tab = 0)           // arranca en "Nuevas"
        setupBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        activeListener?.remove()    // evita fugas de memoria
    }

    // ══════════════════════════════════════════════════════
    //  TABS
    // ══════════════════════════════════════════════════════
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

    // ══════════════════════════════════════════════════════
    //  DATOS REALES DESDE FIRESTORE
    //
    //  Tab 0 "Nuevas"      → status == "pending"
    //  Tab 1 "En Progreso" → status == "accepted"  + providerId == uid
    //  Tab 2 "Historial"   → status == "completed" + providerId == uid
    // ══════════════════════════════════════════════════════
    private fun loadJobs(tab: Int) {
        val uid = auth.currentUser?.uid ?: return

        val query = when (tab) {
            0 -> db.collection("requests")
                .whereEqualTo("status", "pending")
            1 -> db.collection("requests")
                .whereEqualTo("status", "accepted")
                .whereEqualTo("providerId", uid)
            2 -> db.collection("requests")
                .whereEqualTo("status", "completed")
                .whereEqualTo("providerId", uid)
            else -> return
        }

        activeListener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            jobList.clear()

            for (doc in snapshot.documents) {
                jobList.add(
                    ServiceRequest(
                        id          = doc.id,
                        title       = doc.getString("title")       ?: "Sin título",
                        description = doc.getString("description") ?: "",
                        clientName  = doc.getString("clientName")  ?: "Cliente",
                        clientId    = doc.getString("clientId")    ?: "",
                        oficio      = doc.getString("oficio")      ?: "",
                        distance    = doc.getString("distance")    ?: "",
                        status      = doc.getString("status")      ?: ""
                    )
                )
            }

            // Mensaje vacío según tab
            if (jobList.isEmpty()) {
                binding.tvTabContentStatus.visibility = View.VISIBLE
                binding.tvTabContentStatus.text = when (tab) {
                    0    -> "No hay solicitudes nuevas"
                    1    -> "No tienes trabajos en progreso"
                    2    -> "Tu historial está vacío"
                    else -> ""
                }
            } else {
                binding.tvTabContentStatus.visibility = View.GONE
            }

            adapter.notifyDataSetChanged()
        }
    }

    // ══════════════════════════════════════════════════════
    //  ACEPTAR / RECHAZAR / COMPLETAR
    // ══════════════════════════════════════════════════════
    private fun onAccept(request: ServiceRequest) {
        val uid = auth.currentUser?.uid ?: return
        when (currentTab) {
            0 -> db.collection("requests").document(request.id)
                .update(mapOf("status" to "accepted", "providerId" to uid))
                .addOnSuccessListener {
                    Toast.makeText(this, "Solicitud aceptada ✓", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al aceptar", Toast.LENGTH_SHORT).show()
                }
            1 -> db.collection("requests").document(request.id)
                .update("status", "completed")
                .addOnSuccessListener {
                    Toast.makeText(this, "Trabajo completado ✓", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al completar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun onReject(request: ServiceRequest) {
        if (currentTab == 0) {
            db.collection("requests").document(request.id)
                .update("status", "rejected")
                .addOnSuccessListener {
                    Toast.makeText(this, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al rechazar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupRecyclerView() {
        adapter = RequestAdapter(
            requestList = jobList,
            onAccept    = { req -> onAccept(req) },
            onReject    = { req -> onReject(req) }
        )
        binding.rvJobsList.layoutManager = LinearLayoutManager(this)
        binding.rvJobsList.adapter = adapter
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_jobs
        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home -> {
                    startActivity(Intent(this, ProviderHomeActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_provider_jobs    -> true
                R.id.nav_provider_chat    -> {
                    startActivity(Intent(this, ProviderChatActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_provider_catalog -> {
                    startActivity(Intent(this, ProviderCatalogActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_provider_profile -> {
                    startActivity(Intent(this, ProviderProfileActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                else -> false
            }
        }
    }
}