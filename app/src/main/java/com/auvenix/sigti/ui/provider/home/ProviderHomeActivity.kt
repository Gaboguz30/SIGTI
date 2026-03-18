package com.auvenix.sigti.ui.provider.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity

class ProviderHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderHomeBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val requestList = mutableListOf<ServiceRequest>()
    private lateinit var adapter: RequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProviderHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Cargamos toda la información de la pantalla
        fetchProviderData()
        setupRecyclerView()
        fetchRequests()

        // 2. Activamos el menú inferior
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // Le indicamos que el botón de Inicio debe estar marcado visualmente
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_home

        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home -> {
                    // Ya estamos en Inicio, no hacemos nada
                    true
                }
                R.id.nav_provider_jobs -> {
                    // TODO: Cuando ya tengas tu archivo ProviderJobsActivity.kt, borra las diagonales (//) de abajo:

                    val intent = Intent(this, ProviderJobsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)

                    Toast.makeText(this, "Aquí iremos a Trabajos", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_provider_catalog -> {
                    // TODO: Cuando ya tengas tu archivo ProviderCatalogActivity.kt, borra las diagonales (//) de abajo:

                    val intent = Intent(this, ProviderCatalogActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)

                    Toast.makeText(this, "Aquí iremos a Catálogo", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_provider_profile -> {
                    // TODO: Cuando ya tengas tu archivo ProviderProfileActivity.kt, borra las diagonales (//) de abajo:

                    val intent = Intent(this, ProviderProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)

                    Toast.makeText(this, "Aquí iremos a Perfil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchProviderData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellido = doc.getString("apPaterno") ?: ""
                    binding.tvProviderName.text = "$nombre $apellido"
                }
            }
            .addOnFailureListener {
                binding.tvProviderName.text = "Error al cargar"
            }
    }

    private fun setupRecyclerView() {
        adapter = RequestAdapter(
            requestList = requestList,
            onAccept = { req ->
                Toast.makeText(this, "Aceptaste el trabajo: ${req.title}", Toast.LENGTH_SHORT).show()
            },
            onReject = { req ->
                Toast.makeText(this, "Rechazaste el trabajo: ${req.title}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter
    }

    private fun fetchRequests() {
        // Datos de prueba para que veas el diseño
        requestList.clear()
        requestList.add(ServiceRequest("1", "Fuga de agua en tubería principal", "Roberto Sánchez", "A 2.5 km"))
        requestList.add(ServiceRequest("2", "Instalación de pastilla termomagnética", "Vianca Ramírez", "A 1.1 km"))
        requestList.add(ServiceRequest("3", "Impermeabilización de azotea", "Edgar Ramírez", "A 4.0 km"))
        adapter.notifyDataSetChanged()
    }
}