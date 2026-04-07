package com.auvenix.sigti.ui.provider.catalog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderCatalogBinding
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Imports de navegación
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity

class ProviderCatalogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderCatalogBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val serviceList = mutableListOf<ServiceCatalog>()
    private lateinit var adapter: CatalogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderCatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBottomNavigation()
        escucharServiciosDeFirestore() // ✅ Carga en tiempo real

        // El FAB abre la nueva pantalla en blanco
        binding.fabAddService.setOnClickListener {
            startActivity(Intent(this, AddServiceActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = CatalogAdapter(
            serviceList = serviceList,
            onEdit = { service -> openEditService(service) },
            onDelete = { service -> confirmarEliminacion(service) },
            onToggleStatus = { service, nuevoEstado -> cambiarEstadoServicio(service, nuevoEstado) }
        )
        binding.rvProviderCatalog.layoutManager = LinearLayoutManager(this)
        binding.rvProviderCatalog.adapter = adapter
    }

    // 🔥 ACTUALIZA FIREBASE
    private fun cambiarEstadoServicio(service: ServiceCatalog, nuevoEstado: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("services").document(service.id)
            .update("active", nuevoEstado)
            .addOnSuccessListener {
                val msj = if (nuevoEstado) "Servicio Reactivado" else "Servicio Desactivado"
                Toast.makeText(this, msj, Toast.LENGTH_SHORT).show()
                // La lista se actualiza sola gracias al SnapshotListener 😎
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cambiar estado", Toast.LENGTH_SHORT).show()
            }
    }

    // ==========================================
    // MAGIA: Escuchar Firestore en tiempo real
    // ==========================================
    private fun escucharServiciosDeFirestore() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).collection("services")
            .orderBy("name", Query.Direction.ASCENDING) // Ordenados alfabéticamente
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ProviderCatalog", "Error al escuchar servicios", e)
                    return@addSnapshotListener
                }

                serviceList.clear()

                if (snapshots != null && !snapshots.isEmpty) {
                    binding.tvEmptyCatalog.visibility = View.GONE
                    for (doc in snapshots) {
                        val service = doc.toObject(ServiceCatalog::class.java).copy(id = doc.id)
                        serviceList.add(service)
                    }
                } else {
                    binding.tvEmptyCatalog.visibility = View.VISIBLE
                }

                adapter.notifyDataSetChanged()
            }
    }

    private fun openEditService(service: ServiceCatalog) {
        val intent = Intent(this, AddServiceActivity::class.java).apply {
            putExtra("EXTRA_SERVICE_ID", service.id)
            putExtra("EXTRA_SERVICE_NAME", service.name)
            putExtra("EXTRA_SERVICE_DESC", service.description)
        }
        startActivity(intent)
    }

    private fun confirmarEliminacion(service: ServiceCatalog) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Servicio")
            .setMessage("¿Estás seguro de que quieres eliminar '${service.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                val uid = auth.currentUser?.uid ?: return@setPositiveButton
                db.collection("users").document(uid).collection("services").document(service.id).delete()
                    .addOnSuccessListener { Toast.makeText(this, "Servicio eliminado", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🔥 AQUI ESTÁ EL CAMBIO DE LA NAVEGACIÓN
    private fun setupBottomNavigation() {
        // Le indicamos que el Catálogo (posición 2) es el que debe estar en color azul
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_catalog

        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home_provider -> { startActivity(Intent(this, ProviderHomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_catalog -> true // Ya estamos aquí
                R.id.nav_chat -> { startActivity(Intent(this, ProviderChatActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_jobs -> { startActivity(Intent(this, ProviderJobsActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                else -> false
            }
        }
    }
}