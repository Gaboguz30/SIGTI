package com.auvenix.sigti.ui.provider.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderProfileBinding
import com.auvenix.sigti.ui.auth.LoginActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProviderProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProviderProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // Bandera para no disparar updates mientras cargamos el estado inicial
    private var isLoadingState = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfileData()
        loadSwitchStates()
        setupSwitchListeners()
        setupLogoutButton()
        setupBottomNavigation()
    }
    private fun loadSwitchStates() {
        val uid = auth.currentUser?.uid ?: return
        isLoadingState = true

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Lee los valores; si no existen en Firestore, inicia en false (apagado)
                    val online        = doc.getBoolean("online")        ?: false
                    val notifEnabled  = doc.getBoolean("notificaciones") ?: false

                    binding.switchStatus.isChecked        = online
                    binding.switchNotifications.isChecked = notifEnabled
                }
                isLoadingState = false   // ya terminó de cargar, ahora sí escuchamos cambios
            }
            .addOnFailureListener {
                isLoadingState = false
            }
    }

    private fun setupSwitchListeners() {
        val uid = auth.currentUser?.uid ?: return

        binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            db.collection("users").document(uid)
                .update("online", isChecked)
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                }
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            db.collection("users").document(uid)
                .update("notificaciones", isChecked)
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar notificaciones", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre   = doc.getString("nombre")    ?: ""
                    val apellido = doc.getString("apPaterno") ?: ""
                    binding.tvProfileName.text = "$nombre $apellido"
                    val plan = doc.getString("plan_actual") ?: "FREE"
                    binding.tvProfilePlan.text = "Plan $plan"
                }
            }
            .addOnFailureListener { binding.tvProfileName.text = "Usuario" }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            getSharedPreferences("MisCredencialesSIGTI", Context.MODE_PRIVATE)
                .edit().clear().apply()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
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
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}