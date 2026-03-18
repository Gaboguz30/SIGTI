package com.auvenix.sigti.ui.provider.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderProfileBinding
import com.auvenix.sigti.ui.auth.AuthEntryActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProviderProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderProfileBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationBar()
        setupButtons()
    }

    private fun setupButtons() {

        // EL BOTÓN IMPORTANTE: Cerrar Sesión
        binding.btnLogout.setOnClickListener {
            cerrarSesionTotal()
        }
    }

    private fun cerrarSesionTotal() {
        // 1. Cerramos sesión en Firebase
        auth.signOut()

        // 2. Cerramos sesión en Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {

            // 3. Mandamos al inicio y BORRAMOS EL HISTORIAL
            val intent = Intent(this, AuthEntryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupNavigationBar() {
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
                R.id.nav_provider_catalog -> {
                    startActivity(Intent(this, ProviderCatalogActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_provider_profile -> true // Ya estamos aquí
                else -> false
            }
        }
    }
}