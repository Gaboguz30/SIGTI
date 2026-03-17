package com.auvenix.sigti.ui.provider.jobs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderJobsBinding
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity
import com.google.android.material.tabs.TabLayout

class ProviderJobsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderJobsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationBar()
        setupTabs() // <--- Llamamos a la lógica de las pestañas
    }

    private fun setupTabs() {
        // Escuchamos cuando el usuario le pica a una pestaña (Tab)
        binding.tabLayoutJobs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                // El 'position' nos dice qué número de pestaña tocaron (0, 1 o 2)
                when (tab?.position) {
                    0 -> {
                        // Pestaña "Nuevas"
                        binding.tvTabContentStatus.text = "Mostrando solicitudes nuevas pendientes de aceptar"
                        // TODO: Aquí luego jalaremos de Firebase las solicitudes con estatus "PENDIENTE"
                    }
                    1 -> {
                        // Pestaña "En Progreso"
                        binding.tvTabContentStatus.text = "Mostrando trabajos que estás realizando actualmente"
                        // TODO: Aquí jalaremos de Firebase los trabajos con estatus "EN_PROGRESO"
                    }
                    2 -> {
                        // Pestaña "Historial"
                        binding.tvTabContentStatus.text = "Mostrando tu historial de trabajos terminados"
                        // TODO: Aquí jalaremos de Firebase los trabajos con estatus "COMPLETADO"
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // No necesitamos hacer nada aquí por ahora
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Si vuelve a picar la misma pestaña, tampoco hacemos nada
            }
        })
    }

    private fun setupNavigationBar() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_jobs

        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home -> {
                    startActivity(Intent(this, ProviderHomeActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_provider_jobs -> true
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