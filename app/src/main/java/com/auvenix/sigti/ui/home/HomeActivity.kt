package com.auvenix.sigti.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.chat.ChatListActivity
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore // 🔥 IMPORTANTE: Importamos Firestore

class HomeActivity : AppCompatActivity() {

    private lateinit var rvWorkers: RecyclerView
    private lateinit var workerAdapter: WorkerAdapter
    private val workerList = mutableListOf<Worker>()

    // 🔥 Instancia de Firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        rvWorkers = findViewById(R.id.rvWorkers)
        rvWorkers.layoutManager = LinearLayoutManager(this)

        // 1. Inicializamos el adaptador primero (empezará vacío)
        workerAdapter = WorkerAdapter(workerList) { worker ->
            // Cuando le pican a un perfil, pasamos el UID a la siguiente pantalla
            val intent = Intent(this, com.auvenix.sigti.ui.profile.WorkerProfileActivity::class.java)
            intent.putExtra("EXTRA_WORKER_UID", worker.uid)
            startActivity(intent)
        }
        rvWorkers.adapter = workerAdapter

        // 2. Disparamos la búsqueda en la base de datos
        descargarPrestadoresDeFirestore()

        // 3. Configuramos el menú de abajo
        setupBottomNavigation()
    }

    private fun descargarPrestadoresDeFirestore() {
        // Buscamos en la colección "users" donde "role" sea "PRESTADOR"
        db.collection("users")
            .whereEqualTo("role", "PRESTADOR")
            .get()
            .addOnSuccessListener { documents ->
                workerList.clear() // Limpiamos la lista por si acaso

                for (document in documents) {
                    val uid = document.id

                    // Extraemos los datos. Usamos "?:" (elvis operator) para evitar crasheos
                    // por si alguna cuenta vieja no tiene el campo "nombre" o "profesion".
                    val nombre = document.getString("nombre") ?: "Prestador"
                    val apellidoPaterno = document.getString("apellidoPaterno") ?: ""
                    val fullName = "$nombre $apellidoPaterno".trim()

                    val profesion = document.getString("profesion") ?: "Oficio por definir"

                    // Estos campos los pongo por defecto por ahora, ya que tal vez
                    // aún no los guardas al registrar al usuario, pero tu diseño los pide.
                    val rating = document.getString("rating") ?: "5.0"
                    val price = document.getString("price") ?: "250"
                    val distance = document.getString("distance") ?: "Cerca de ti"

                    val worker = Worker(
                        uid = uid,
                        name = fullName,
                        profession = profesion,
                        rating = rating,
                        price = price,
                        distance = distance
                    )
                    workerList.add(worker)
                }

                // Le avisamos al adaptador que ya llegaron los datos para que dibuje las tarjetas
                workerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("HomeActivity", "Error al descargar prestadores", exception)
                Toast.makeText(this, "Error de conexión al cargar perfiles", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_map -> {
                    startActivity(Intent(this, UserMapActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, UserNotificationsActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                else -> false
            }
        }
    }
}