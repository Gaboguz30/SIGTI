package com.auvenix.sigti.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.chat.ChatDetailActivity
import com.auvenix.sigti.ui.request.NewRequestActivity
import com.auvenix.sigti.ui.support.ReportActivity
import com.auvenix.sigti.ui.provider.catalog.ServiceCatalog
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView // 🔥 IMPORT NUEVO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// 🔥 IMPORTS DE NAVEGACIÓN
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.chat.ChatListActivity
import com.auvenix.sigti.ui.home.UserNotificationsActivity

class WorkerProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvProfession: TextView
    private lateinit var tvRating: TextView
    private lateinit var rvServices: RecyclerView
    private lateinit var tvEmptyServices: TextView

    private var workerId: String = ""
    private var workerName: String = "Prestador"

    private val db = FirebaseFirestore.getInstance()

    private val servicesList = mutableListOf<ServiceCatalog>()
    private lateinit var adapter: PublicServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_profile)

        // 1. Recibimos el UID del prestador desde el HomeActivity
        workerId = intent.getStringExtra("EXTRA_WORKER_UID") ?: ""

        if (workerId.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró el perfil", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupRecyclerView()

        // 2. Descargar datos y servicios
        descargarDatosPerfil()
        descargarCatalogoReal()

        setupButtons()

        // 3. Conectar la barra de navegación inferior
        setupBottomNavigation()
    }

    private fun initializeViews() {
        tvName = findViewById(R.id.tvProfileName)
        tvProfession = findViewById(R.id.tvProfileProfession)
        tvRating = findViewById(R.id.tvProfileRating)

        rvServices = findViewById(R.id.rvServices)
        tvEmptyServices = findViewById(R.id.tvEmptyServicesPerfil)
    }

    private fun setupRecyclerView() {
        rvServices.layoutManager = LinearLayoutManager(this)
        adapter = PublicServiceAdapter(servicesList)
        rvServices.adapter = adapter
    }

    private fun descargarDatosPerfil() {
        db.collection("users").document(workerId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = doc.getString("nombre") ?: "Prestador"
                    val apellido = doc.getString("apellidoPaterno") ?: ""
                    workerName = "$nombre $apellido".trim()

                    tvName.text = workerName
                    // Buscamos "profesion" si la tienes, o "oficios" si es un array
                    tvProfession.text = doc.getString("profesion") ?: "Profesional"
                    tvRating.text = doc.getString("rating") ?: "★ 5.0"
                }
            }
    }

    private fun descargarCatalogoReal() {
        db.collection("users").document(workerId).collection("services")
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                servicesList.clear()
                if (documents.isEmpty) {
                    tvEmptyServices.visibility = View.VISIBLE
                    rvServices.visibility = View.GONE
                } else {
                    tvEmptyServices.visibility = View.GONE
                    rvServices.visibility = View.VISIBLE
                    for (doc in documents) {
                        val service = doc.toObject(ServiceCatalog::class.java)
                        servicesList.add(service)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("WorkerProfile", "Error cargando catálogo real", e)
            }
    }

    private fun setupButtons() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.btnReport).setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("workerId", workerId)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btnRequest).setOnClickListener {
            val intent = Intent(this, NewRequestActivity::class.java)
            // 🔥 Le metemos el ID y Nombre del prestador a la mochila (Intent)
            intent.putExtra("EXTRA_WORKER_UID", workerId)
            intent.putExtra("EXTRA_WORKER_NAME", workerName)
            startActivity(intent)
        }

        // 🔥 CHAT REAL
        findViewById<MaterialButton>(R.id.btnChat).setOnClickListener {
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra("serviceId", workerId)
            intent.putExtra("contactName", workerName)
            startActivity(intent)
        }
    }

    // ==========================================
    // MAGIA NUEVA: Barra de Navegación del Solicitante
    // ==========================================
    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationProfile)

        // No marcamos ninguna como "seleccionada" porque estamos viendo un perfil ajeno
        // bottomNavigation.selectedItemId = ...

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
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
                    // Este botón lleva al MI perfil (del Solicitante), no al del trabajador actual
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                else -> false
            }
        }
    }
}