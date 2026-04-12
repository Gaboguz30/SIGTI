package com.auvenix.sigti.ui.provider.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderHomeBinding
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity

class ProviderHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderHomeBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val requestList = mutableListOf<RequestModel>()
    private lateinit var adapter: RequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserData()
        setupRecyclerView()
        escucharNuevasSolicitudes()
        setupBottomNavigation()
        cargarCalificacion()
        cargarGananciasMensuales() // 🔥 AQUÍ LLAMAMOS A LA SUMA DE GANANCIAS

        val btnNotifProvider = findViewById<View>(R.id.btnNotificationsProvider)
        btnNotifProvider.setOnClickListener {
            val bottomSheet = com.auvenix.sigti.notifications.NotificationsBottomSheet()
            bottomSheet.show(supportFragmentManager, "NotificationsSheet")
        }
    }

    // 🔥 SOLUCIÓN 2: ESTADO CONECTADO/DESCONECTADO EN TIEMPO REAL
    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        // Usamos addSnapshotListener para que se actualice instantáneo si lo cambian en el perfil
        db.collection("users").document(uid).addSnapshotListener { doc, e ->
            if (e != null || doc == null || !doc.exists()) return@addSnapshotListener

            val nombre = doc.getString("nombre") ?: "Prestador"
            binding.tvProviderName.text = "$nombre!"

            // Leemos el estado online
            val isOnline = doc.getBoolean("online") ?: false
            if (isOnline) {
                binding.tvStatus.text = "Conectado"
                binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#FFFFFF")) // Verde opcional
            } else {
                binding.tvStatus.text = "Desconectado"
                binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#FFFFFF")) // Blanco
            }
        }
    }

    // 🔥 SOLUCIÓN 3: SUMA DE GANANCIAS COMPLETADAS
// 🔥 GANANCIAS EN TIEMPO REAL 🔥
// 🔥 GANANCIAS ACUMULADAS DEL MES EN TIEMPO REAL 🔥
    private fun cargarGananciasMensuales() {
        val myUid = auth.currentUser?.uid ?: return

        // 1. Obtenemos la fecha de inicio del mes actual
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        val inicioMes = calendar.time

        // 2. Consultamos con filtro de Status Y Fecha
        db.collection("requests")
            .whereEqualTo("providerId", myUid)
            .whereEqualTo("status", "completed")
            .whereGreaterThanOrEqualTo("timestamp", inicioMes) // 🔥 Solo este mes
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ProviderHome", "Error escuchando ganancias", e)
                    return@addSnapshotListener
                }

                var acumuladoMensual = 0.0

                if (snapshots != null) {
                    // Firebase nos regresa la lista de TODOS los trabajos del mes
                    for (doc in snapshots) {
                        // Sumamos el finalPrice de cada uno
                        val price = doc.getDouble("finalPrice") ?: doc.getLong("finalPrice")?.toDouble() ?: 0.0
                        acumuladoMensual += price
                    }
                }

                // 3. Actualizamos la UI con el total acumulado
                val formatoMoneda = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "MX"))
                binding.tvEarnings.text = formatoMoneda.format(acumuladoMensual)
            }
    }
    private fun setupRecyclerView() {
        adapter = RequestAdapter(requestList) { request ->
            val intent = Intent(this, RequestDetailActivity::class.java)
            intent.putExtra("EXTRA_REQUEST_ID", request.id)
            startActivity(intent)
        }
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter
    }

    private fun escucharNuevasSolicitudes() {
        val myUid = auth.currentUser?.uid ?: return

        binding.pbLoading.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.rvRequests.visibility = View.GONE

        db.collection("requests")
            .whereEqualTo("providerId", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->

                binding.pbLoading.visibility = View.GONE

                if (e != null) {
                    Log.w("ProviderHome", "Error escuchando solicitudes", e)
                    binding.tvEmptyState.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                requestList.clear()

                if (snapshots != null) {
                    for (doc in snapshots) {
                        val req = doc.toObject(RequestModel::class.java).copy(id = doc.id)
                        requestList.add(req)
                    }
                }

                if (requestList.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvRequests.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvRequests.visibility = View.VISIBLE
                }

                adapter.notifyDataSetChanged()
            }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationProvider)
        bottomNavigation.selectedItemId = R.id.nav_home_provider

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home_provider -> true
                R.id.nav_catalog -> { irAPantalla(ProviderCatalogActivity::class.java); true }
                R.id.nav_chat -> { irAPantalla(ProviderChatActivity::class.java); true }
                R.id.nav_jobs -> { irAPantalla(ProviderJobsActivity::class.java); true }
                R.id.nav_profile -> { irAPantalla(ProfileActivity::class.java); true }
                else -> false
            }
        }
    }

    // 🔥 CALIFICACIÓN (ESTRELLITAS) EN TIEMPO REAL
    private fun cargarCalificacion() {
        val myUid = auth.currentUser?.uid ?: return

        // Usamos addSnapshotListener para que sea en tiempo real
        db.collection("reviews")
            .whereEqualTo("technician_uid", myUid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ProviderHome", "Error al escuchar reseñas", e)
                    return@addSnapshotListener
                }

                var total = 0f
                var count = 0

                if (snapshots != null) {
                    for (doc in snapshots) {
                        // Sacamos el valor asegurándonos de que no truene si es Long o Double
                        val rating = doc.getDouble("rating")?.toFloat() ?: doc.getLong("rating")?.toFloat() ?: 0f
                        total += rating
                        count++
                    }
                }

                // Calculamos el promedio
                val promedio = if (count > 0) total / count else 0f

                // Lo pintamos en tu TextView (tvRating) con 1 decimal (ejemplo: 4.8)
                binding.tvRating.text = String.format("%.1f", promedio)
            }
    }
    private fun irAPantalla(activityClass: Class<*>) {
        if (this::class.java == activityClass) return
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}