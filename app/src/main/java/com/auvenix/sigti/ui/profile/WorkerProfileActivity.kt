package com.auvenix.sigti.ui.profile

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton

class WorkerProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_profile)

        // 1. Configurar el botón de regresar
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Esto cierra esta pantalla y te regresa a la anterior (Home)
        }

        // 2. Configurar la lista de servicios (RecyclerView)
        val rvServices = findViewById<RecyclerView>(R.id.rvServices)
        rvServices.layoutManager = LinearLayoutManager(this)

        // Datos de prueba basados en tu diseño
        val dummyServices = listOf(
            ServiceItem("Instalación Eléctrica", "Incluye cableado y tiempo estimado.", "300"),
            ServiceItem("Revisión de corto", "Incluye detección y tiempo estimado.", "200"),
            ServiceItem("Mantenimiento", "Incluye revisión general y ajustes.", "250")
        )

        // Asignamos el adaptador
        rvServices.adapter = ServiceAdapter(dummyServices)

        // 3. Configurar los botones principales
        val btnRequest = findViewById<MaterialButton>(R.id.btnRequest)
        val btnChat = findViewById<MaterialButton>(R.id.btnChat)

        btnRequest.setOnClickListener {
            Toast.makeText(this, "Abriendo formulario de solicitud...", Toast.LENGTH_SHORT).show()
            val intent = android.content.Intent(this, com.auvenix.sigti.ui.request.NewRequestActivity::class.java)
            startActivity(intent)
        }

        btnChat.setOnClickListener {
            Toast.makeText(this, "Abriendo chat con Vianca...", Toast.LENGTH_SHORT).show()
            val intent = android.content.Intent(this, com.auvenix.sigti.ui.chat.ChatActivity::class.java)
            startActivity(intent)
        }
    }
}