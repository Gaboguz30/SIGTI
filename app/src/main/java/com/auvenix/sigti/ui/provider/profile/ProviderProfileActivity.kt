package com.auvenix.sigti.ui.provider.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// IMPORTS
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.auth.LoginActivity

class ProviderProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfileData()
        setupLogoutButton()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_profile

        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home -> {
                    startActivity(Intent(this, ProviderHomeActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
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
                R.id.nav_provider_profile -> true // Ya estamos aquí
                else -> false
            }
        }
    }

    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellido = doc.getString("apPaterno") ?: ""
                    binding.tvProfileName.text = "$nombre $apellido"
                    val plan = doc.getString("plan_actual") ?: "FREE"
                    binding.tvProfilePlan.text = "Plan $plan"
                }
            }
            .addOnFailureListener {
                binding.tvProfileName.text = "Usuario"
            }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val prefs = getSharedPreferences("MisCredencialesSIGTI", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}