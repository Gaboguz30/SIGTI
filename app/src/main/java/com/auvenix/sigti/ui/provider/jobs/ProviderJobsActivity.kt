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
import com.auvenix.sigti.session.SessionManager
import com.auvenix.sigti.ui.chat.ChatDetailActivity
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.profile.ProfileActivity
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

    // 🔥 INSTANCIAMOS EL SESSION MANAGER PARA SABER QUIÉN ENTRÓ
    private lateinit var session : SessionManager
    private lateinit var myRole  : String

    private lateinit var adapter : JobAdapter
    private var activeListener   : ListenerRegistration? = null
    private var currentTab       = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔥 LEEMOS EL ROL DEL USUARIO LOGUEADO
        session = SessionManager(this)
        myRole = session.getRole() ?: "SOLICITANTE"

        setupRecyclerView()
        setupTabs()
        loadJobs(tab = 0)
        setupBottomNavigation() // 🔥 Ahora este menú es inteligente
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

        // 🔥 LA MAGIA DEL FILTRO: Si es prestador busca por providerId, si es cliente busca por clientId
        val campoFiltro = if (myRole == "PRESTADOR") "providerId" else "clientId"

        val query = when (tab) {
            0 -> db.collection("requests")
                .whereEqualTo(campoFiltro, uid) // <-- Usa la variable inteligente
                .whereIn("status", listOf("in_progress", "pending_client_confirmation"))
            1 -> db.collection("requests")
                .whereEqualTo(campoFiltro, uid) // <-- Usa la variable inteligente
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
                    0    -> if (myRole == "PRESTADOR") "No tienes trabajos activos" else "No has solicitado ningún trabajo"
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
            userRole = myRole, // 🔥 ¡AQUÍ ESTÁ LA LÍNEA CORREGIDA!
            onChatClick = { job ->
                val intent = Intent(this, ChatDetailActivity::class.java)
                // Si es prestador manda el ID del cliente, si es cliente manda el ID del prestador
                intent.putExtra("serviceId", job.clientId)
                intent.putExtra("contactName", job.clientName)
                startActivity(intent)
            },
            onCompleteClick = { job ->
                // 🔥 SEGURIDAD: Solo el prestador puede marcar como completado
                if (myRole == "PRESTADOR") {
                    AlertDialog.Builder(this)
                        .setTitle("Finalizar Trabajo")
                        .setMessage("¿Estás seguro de marcar '${job.title}' como terminado?\n\nConfirma que ya recibiste el pago de $${job.priceOffer}.")
                        .setPositiveButton("Sí, ya cobré") { _, _ ->
                            marcarComoCompletado(job.id)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                } else {
                    Toast.makeText(this, "El técnico debe finalizar el trabajo", Toast.LENGTH_SHORT).show()
                }
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

    // 🔥 LA MAGIA DEL MENÚ DINÁMICO
    private fun setupBottomNavigation() {
        val navView = binding.bottomNavigationProvider

        if (myRole == "PRESTADOR") {
            // CARGAMOS MENÚ DE PRESTADOR
            navView.menu.clear()
            navView.inflateMenu(R.menu.provider_bottom_nav_menu)
            navView.selectedItemId = R.id.nav_jobs

            navView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home_provider -> { startActivity(Intent(this, ProviderHomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_catalog -> { startActivity(Intent(this, ProviderCatalogActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_chat -> { startActivity(Intent(this, ProviderChatActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_jobs -> true
                    R.id.nav_profile -> { startActivity(Intent(this, ProviderProfileActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    else -> false
                }
            }
        } else {
            // 🔥 CARGAMOS MENÚ DE CLIENTE / SOLICITANTE
            navView.menu.clear()
            navView.inflateMenu(R.menu.bottom_nav_menu)
            navView.selectedItemId = R.id.nav_jobs

            navView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_map -> { startActivity(Intent(this, UserMapActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_chat -> { startActivity(Intent(this, ProviderChatActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_jobs -> true
                    R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    else -> false
                }
            }
        }
    }
}