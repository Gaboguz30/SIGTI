package com.auvenix.sigti.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.chat.ChatListActivity
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // ================================
        // LISTA DE TRABAJADORES
        // ================================

        val rvWorkers = findViewById<RecyclerView>(R.id.rvWorkers)

        val dummyWorkers = listOf(
            Worker("Vianca Ramírez", "Electricista", "4.8", "250", "2 km"),
            Worker("Amairany Solís", "Plomero", "4.9", "200", "5 km"),
            Worker("Edgar Ramírez", "Electricista", "3.9", "150", "<1 km"),
            Worker("Leonardo Gonzalo", "Herrero", "4.2", "180", "3 km")
        )

        rvWorkers.layoutManager = LinearLayoutManager(this)

        rvWorkers.adapter = WorkerAdapter(dummyWorkers) { selectedWorker ->

            Toast.makeText(
                this,
                "Abriendo perfil de ${selectedWorker.name}",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(
                this,
                com.auvenix.sigti.ui.profile.WorkerProfileActivity::class.java
            )

            startActivity(intent)
        }

        // ================================
        // BOTTOM NAVIGATION
        // ================================

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> true

                R.id.nav_chat -> {

                    val intent = Intent(this, ChatListActivity::class.java)

                    // evita abrir múltiples ventanas
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

                    startActivity(intent)
                    finish()

                    true
                }

                R.id.nav_profile -> {
                    // ¡CONECTAMOS EL CABLE! Vamos al Perfil
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)

                    // Quitamos la animación de salto para que se sienta fluido
                    overridePendingTransition(0, 0)

                    // Opcional: finish() si no quieres que el usuario regrese al Home con el botón "Atrás"
                    true
                }

                R.id.nav_notifications -> {
                    Toast.makeText(this, "Alertas", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }
}