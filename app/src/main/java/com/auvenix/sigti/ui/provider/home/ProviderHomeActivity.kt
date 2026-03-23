package com.auvenix.sigti.ui.provider.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderHomeBinding
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProviderHomeActivity : AppCompatActivity() {

    private lateinit var binding  : ActivityProviderHomeBinding
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val requestList = mutableListOf<ServiceRequest>()
    private lateinit var adapter: RequestAdapter

    // Guardamos el listener para cancelarlo en onStop
    private var requestsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchProviderData()
        setupStatusSwitch()
        listenToRequests()
        setupBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        requestsListener?.remove()  // Evita fugas de memoria
    }

    private fun setupStatusSwitch() {
        val uid = auth.currentUser?.uid ?: return

        // Leer estado guardado en Firestore al entrar
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val online = doc.getBoolean("online") ?: false
                binding.switchStatus.isChecked = online
                updateSwitchLabel(online)
            }

        // Escuchar cambios del switch
        binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchLabel(isChecked)
            // Guardar el estado en Firestore
            db.collection("users").document(uid)
                .update("online", isChecked)
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateSwitchLabel(active: Boolean) {
        binding.switchStatus.text = if (active) "Activo" else "Ausente"
        binding.switchStatus.setTextColor(
            if (active)
                getColor(R.color.sigti_blue)
            else
                getColor(android.R.color.darker_gray)
        )
    }

    /**
     * Escucha la colección "requests" donde status == "pending".
     * Cada solicitud tiene: title, description, clientName, clientId,
     * oficio, distance (opcional), status, timestamp.
     *
     * Estructura Firestore:
     *   requests / {requestId}
     *     - title        : "Fuga en tubería"
     *     - description  : "Hay agua saliendo del baño..."
     *     - clientName   : "Roberto Sánchez"
     *     - clientId     : "uid_del_cliente"
     *     - oficio       : "Plomero"
     *     - status       : "pending"
     *     - timestamp    : ServerTimestamp
     */
    private fun listenToRequests() {
        requestsListener = db.collection("requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                requestList.clear()

                for (doc in snapshot.documents) {
                    requestList.add(
                        ServiceRequest(
                            id          = doc.id,
                            title       = doc.getString("title")       ?: "Sin título",
                            description = doc.getString("description") ?: "",
                            clientName  = doc.getString("clientName")  ?: "Cliente",
                            clientId    = doc.getString("clientId")    ?: "",
                            oficio      = doc.getString("oficio")      ?: "",
                            distance    = doc.getString("distance")    ?: "",
                            status      = doc.getString("status")      ?: "pending"
                        )
                    )
                }

                adapter.notifyDataSetChanged()
            }
    }

    private fun onAcceptRequest(request: ServiceRequest) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("requests").document(request.id)
            .update(
                mapOf(
                    "status"     to "accepted",
                    "providerId" to uid
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Solicitud aceptada ✓", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al aceptar solicitud", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onRejectRequest(request: ServiceRequest) {
        db.collection("requests").document(request.id)
            .update("status", "rejected")
            .addOnSuccessListener {
                Toast.makeText(this, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al rechazar solicitud", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView() {
        adapter = RequestAdapter(
            requestList = requestList,
            onAccept    = { req -> onAcceptRequest(req) },
            onReject    = { req -> onRejectRequest(req) }
        )
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter
    }

    private fun fetchProviderData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre   = doc.getString("nombre")   ?: ""
                    val apellido = doc.getString("apPaterno") ?: ""
                    binding.tvProviderName.text = "$nombre $apellido"
                }
            }
            .addOnFailureListener {
                binding.tvProviderName.text = "Error al cargar"
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_home

        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home    -> true
                R.id.nav_provider_jobs    -> {
                    startActivity(Intent(this, ProviderJobsActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
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