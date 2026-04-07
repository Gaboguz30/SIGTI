package com.auvenix.sigti.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityUserMapBinding
import com.auvenix.sigti.ui.profile.ProfileActivity

class UserMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔥 1. PINTAMOS EL MAPA DE AZUL
        binding.bottomNavigation.selectedItemId = R.id.nav_map

        // 🔥 2. CONFIGURAMOS LOS CLICS (Sin parpadeos)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_map -> true // Ya estamos aquí, no hacemos nada

                R.id.nav_chat -> {
                    startActivity(Intent(this, com.auvenix.sigti.ui.provider.chat.ProviderChatActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                // 🔥 3. NUEVO: BOTÓN DE TRABAJOS (Reciclado del prestador)
                R.id.nav_jobs -> {
                    startActivity(Intent(this, com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}