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
import android.widget.TextView

class ProviderJobsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProviderJobsBinding
    private val db               = FirebaseFirestore.getInstance()
    private val auth             = FirebaseAuth.getInstance()
    private val jobList          = mutableListOf<RequestModel>()

    private lateinit var session : SessionManager
    private lateinit var myRole  : String

    private lateinit var adapter : JobAdapter
    private var activeListener   : ListenerRegistration? = null
    private var currentTab       = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        myRole = session.getRole() ?: "SOLICITANTE"

        setupRecyclerView()

        if (binding.tabLayoutJobs.tabCount < 3) {
            binding.tabLayoutJobs.addTab(binding.tabLayoutJobs.newTab().setText("Rechazados"))
        }

        setupTabs()
        loadJobs(tab = 0)
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
        val campoFiltro = if (myRole == "PRESTADOR") "providerId" else "clientId"

        val query = when (tab) {
            0 -> db.collection("requests")
                .whereEqualTo(campoFiltro, uid)
                .whereIn("status", listOf("in_progress", "pending_client_confirmation", "pending"))
            1 -> db.collection("requests")
                .whereEqualTo(campoFiltro, uid)
                .whereEqualTo("status", "completed")
            2 -> db.collection("requests")
                .whereEqualTo(campoFiltro, uid)
                .whereEqualTo("status", "rejected")
            else -> return
        }

        activeListener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            jobList.clear()

            for (doc in snapshot.documents) {
                jobList.add(
                    RequestModel(
                        id           = doc.id,
                        clientId     = doc.getString("clientId") ?: "",
                        clientName   = doc.getString("clientName") ?: "Cliente",
                        providerId   = doc.getString("providerId") ?: "",
                        providerName = doc.getString("providerName") ?: "Técnico",
                        title        = doc.getString("title") ?: "Sin título",
                        priceOffer   = doc.getDouble("finalPrice") ?: doc.getDouble("priceOffer") ?: 0.0,
                        fecha        = doc.getString("fecha") ?: "",
                        status       = doc.getString("status") ?: ""
                    )
                )
            }
            if (jobList.isEmpty()) {
                binding.tvTabContentStatus.visibility = View.VISIBLE
                binding.tvTabContentStatus.text = when (tab) {
                    0    -> if (myRole == "PRESTADOR") "No tienes trabajos activos" else "No has solicitado ningún trabajo"
                    1    -> "Tu historial está vacío"
                    2    -> "No hay trabajos rechazados"
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
            userRole = myRole,
            onChatClick = { job ->
                val intent = Intent(this, ChatDetailActivity::class.java)
                intent.putExtra("serviceId", job.clientId)
                intent.putExtra("contactName", job.clientName)
                startActivity(intent)
            },
            onCompleteClick = { job ->
                if (myRole == "PRESTADOR" && job.status == "in_progress") {
                    mostrarDialogConfirmacion(
                        "Finalizar Trabajo",
                        "¿Estás seguro de marcar '${job.title}' como terminado?\n\nConfirma que ya recibiste el pago de $${job.priceOffer}."
                    ) {
                        db.collection("requests").document(job.id).update("status", "completed")
                            .addOnSuccessListener {
                                Toast.makeText(this, "Trabajo finalizado correctamente", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                else if (myRole == "SOLICITANTE" && job.status == "pending_client_confirmation") {
                    mostrarDialogConfirmacion(
                        "Aceptar Precio",
                        "El técnico cobrará $${job.priceOffer} por este servicio.\n\n¿Deseas comenzar el servicio?"
                    ) {
                        db.collection("requests").document(job.id).update("status", "in_progress")
                            .addOnSuccessListener {
                                Toast.makeText(this, "Trabajo en marcha", Toast.LENGTH_LONG).show()
                            }
                    }
                }
            }
        )
        binding.rvJobsList.layoutManager = LinearLayoutManager(this)
        binding.rvJobsList.adapter = adapter
    }

    // 🔥 NAVEGACIÓN SUAVE DINÁMICA
    private fun setupBottomNavigation() {
        val navView = binding.bottomNavigationProvider

        if (myRole == "PRESTADOR") {
            navView.menu.clear()
            navView.inflateMenu(R.menu.provider_bottom_nav_menu)
            navView.selectedItemId = R.id.nav_jobs

            navView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home_provider -> { irAPantalla(ProviderHomeActivity::class.java); true }
                    R.id.nav_catalog -> { irAPantalla(ProviderCatalogActivity::class.java); true }
                    R.id.nav_chat -> { irAPantalla(ProviderChatActivity::class.java); true }
                    R.id.nav_jobs -> true
                    R.id.nav_profile -> { irAPantalla(ProviderProfileActivity::class.java); true }
                    else -> false
                }
            }
        } else {
            navView.menu.clear()
            navView.inflateMenu(R.menu.bottom_nav_menu)
            navView.selectedItemId = R.id.nav_jobs

            navView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { irAPantalla(HomeActivity::class.java); true }
                    R.id.nav_map -> { irAPantalla(UserMapActivity::class.java); true }
                    R.id.nav_chat -> { irAPantalla(ProviderChatActivity::class.java); true }
                    R.id.nav_jobs -> true
                    R.id.nav_profile -> { irAPantalla(ProfileActivity::class.java); true }
                    else -> false
                }
            }
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