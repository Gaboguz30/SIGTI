package com.auvenix.sigti.ui.provider.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderHomeBinding
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity

class ProviderHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationBar()
        setupStatusSwitch() // <--- Llamamos a la nueva función
    }

    private fun setupStatusSwitch() {
        // Escuchamos cuando el usuario mueve el switch
        binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Está encendido (En línea)
                binding.switchStatus.text = "En línea"
                binding.switchStatus.setTextColor(Color.parseColor("#4CAF50")) // Verde
                Toast.makeText(this, "Ahora estás disponible para recibir trabajos", Toast.LENGTH_SHORT).show()
                // TODO: Aquí luego mandaremos la señal a Firebase
            } else {
                // Está apagado (Desconectado)
                binding.switchStatus.text = "Desconectado"
                binding.switchStatus.setTextColor(Color.parseColor("#757575")) // Gris
                Toast.makeText(this, "Dejaste de recibir notificaciones", Toast.LENGTH_SHORT).show()
                // TODO: Aquí apagaremos la señal en Firebase
            }
        }
    }

    private fun setupNavigationBar() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_home

        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home -> true
                R.id.nav_provider_jobs -> {
                    startActivity(Intent(this, ProviderJobsActivity::class.java))
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