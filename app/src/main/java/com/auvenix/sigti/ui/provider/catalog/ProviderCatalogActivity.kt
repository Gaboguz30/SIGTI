package com.auvenix.sigti.ui.provider.catalog

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderCatalogBinding
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity

class ProviderCatalogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderCatalogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderCatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationBar()
        setupFab() // <--- Llamamos a la lógica del botón flotante
    }

    private fun setupFab() {
        // Escuchamos el clic en el Botón Flotante (+)
        binding.fabAddService.setOnClickListener {
            // Aquí en el futuro abriremos otra pantalla o un cuadrito de diálogo
            // para que escriba el nombre de su servicio y cuánto cobra.
            Toast.makeText(this, "Abriendo formulario de Nuevo Servicio...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNavigationBar() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_catalog

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
                R.id.nav_provider_catalog -> true // Ya estamos aquí
                R.id.nav_provider_profile -> {
                    startActivity(Intent(this, ProviderProfileActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                else -> false
            }
        }
    }
}