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

// IMPORTS DE TUS OTRAS PANTALLAS
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity

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

        fetchProviderData()
        setupRecyclerView()
        fetchRequests()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_home

        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home -> true // Ya estamos aquí
                R.id.nav_provider_jobs -> {
                    startActivity(Intent(this, ProviderJobsActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_provider_chat -> {
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
            onAccept = { req -> Toast.makeText(this, "Aceptaste: ${req.title}", Toast.LENGTH_SHORT).show() },
            onReject = { req -> Toast.makeText(this, "Rechazaste: ${req.title}", Toast.LENGTH_SHORT).show() }
        )
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter
    }

    private fun fetchRequests() {
        requestList.clear()
        requestList.add(ServiceRequest("1", "Fuga de agua en tubería", "Roberto Sánchez", "A 2.5 km"))
        requestList.add(ServiceRequest("2", "Instalación de pastilla", "Vianca Ramírez", "A 1.1 km"))
        requestList.add(ServiceRequest("3", "Impermeabilización", "Edgar Ramírez", "A 4.0 km"))
        adapter.notifyDataSetChanged()
    }
}