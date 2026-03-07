package com.auvenix.sigti.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
// Importa tu Activity del Perfil cuando la creemos
// import com.auvenix.sigti.ui.profile.WorkerProfileActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1. Buscamos el RecyclerView en el diseño
        val rvWorkers = findViewById<RecyclerView>(R.id.rvWorkers)

        // 2. Creamos datos de prueba igualitos a tu diseño
        val dummyWorkers = listOf(
            Worker("Vianca Ramírez", "Electricista", "4.8", "250", "2 km"),
            Worker("Amairany Solís", "Plomero", "4.9", "200", "5 km"),
            Worker("Edgar Ramírez", "Electricista", "3.9", "150", "<1 km"),
            Worker("Leonardo Gonzalo", "Herrero", "4.2", "180", "3 km")
        )

        // 3. Configuramos la lista para que sea vertical
        rvWorkers.layoutManager = LinearLayoutManager(this)

        // 4. Le asignamos el adaptador con los datos
        rvWorkers.adapter = WorkerAdapter(dummyWorkers) { selectedWorker ->
            // ¿Qué pasa cuando le dan clic a "Ver Perfil"?
            Toast.makeText(this, "Abriendo perfil de ${selectedWorker.name}", Toast.LENGTH_SHORT).show()

            /// Creamos el "Intent" que es el puente para saltar a la otra pantalla
            val intent = Intent(this, com.auvenix.sigti.ui.profile.WorkerProfileActivity::class.java)

            // Iniciamos la pantalla
            startActivity(intent)
        }
    }
}