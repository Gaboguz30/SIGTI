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
import com.auvenix.sigti.ui.profile.WorkerProfileActivity
import com.auvenix.sigti.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val workers = mutableListOf<Worker>()
    private lateinit var adapter: WorkerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val rvWorkers = findViewById<RecyclerView>(R.id.rvWorkers)
        rvWorkers.layoutManager = LinearLayoutManager(this)

        adapter = WorkerAdapter(workers) { worker ->
            startActivity(Intent(this, WorkerProfileActivity::class.java).apply {
                putExtra(Constants.EXTRA_WORKER_ID, worker.uid)
                putExtra(Constants.EXTRA_WORKER_NAME, worker.name)
                putExtra(Constants.EXTRA_WORKER_PROFESSION, worker.profession)
            })
        }
        rvWorkers.adapter = adapter

        loadWorkers()
        setupBottomNav()
    }

    private fun loadWorkers() {
        db.collection(Constants.COLLECTION_USERS)
            .whereEqualTo(Constants.FIELD_ROLE, Constants.ROLE_PROVIDER)
            .get()
            .addOnSuccessListener { snapshot ->
                workers.clear()
                snapshot.documents.forEach { doc ->
                    val oficios = doc.get("oficios") as? List<Map<String, Any>> ?: emptyList()
                    val oficioPrincipal = oficios.firstOrNull()?.get("nombre")?.toString()
                        ?: doc.getString("oficio")
                        ?: "Prestador de servicios"

                    val nombre = listOf(
                        doc.getString("nombre").orEmpty(),
                        doc.getString("apPaterno").orEmpty()
                    ).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Prestador" }

                    workers.add(
                        Worker(
                            uid = doc.id,
                            name = nombre,
                            profession = oficioPrincipal,
                            rating = String.format(Locale.US, "%.1f", doc.getDouble("rating") ?: 0.0),
                            price = doc.get("precioBase")?.toString() ?: doc.get("price")?.toString() ?: "0",
                            distance = doc.getString("distance") ?: "N/D"
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar prestadores: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupBottomNav() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_map -> {
                    startActivity(Intent(this, UserMapActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, UserNotificationsActivity::class.java))
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