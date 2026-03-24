package com.auvenix.sigti.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityUserNotificationsBinding
import com.auvenix.sigti.ui.chat.ChatListActivity
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserNotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserNotificationsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var adapter: NotificationAdapter
    private val notifList = mutableListOf<NotifRequestModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        escucharNotificaciones()
        setupBottomNav()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(
            notifList = notifList,
            onConfirm = { requestId -> confirmarTrabajo(requestId) },
            onReject = { requestId, currentStatus -> rechazarODescartar(requestId, currentStatus) }
        )
        // 🔥 Asegúrate de que el ID en tu XML sea rvNotifications
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun escucharNotificaciones() {
        val uid = auth.currentUser?.uid ?: return

        // Escuchamos las solicitudes donde el cliente soy yo, y están en espera de mi respuesta o fueron rechazadas
        db.collection("requests")
            .whereEqualTo("clientId", uid)
            .whereIn("status", listOf("pending_client_confirmation", "rejected"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                notifList.clear()
                if (snapshot != null && !snapshot.isEmpty) {
                    binding.rvNotifications.visibility = View.VISIBLE
                    for (doc in snapshot.documents) {
                        notifList.add(
                            NotifRequestModel(
                                id = doc.id,
                                providerName = doc.getString("providerName") ?: "Prestador",
                                title = doc.getString("title") ?: "Servicio",
                                finalPrice = doc.getDouble("finalPrice") ?: 0.0,
                                status = doc.getString("status") ?: ""
                            )
                        )
                    }
                } else {
                    binding.rvNotifications.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun confirmarTrabajo(requestId: String) {
        // 🔥 ¡PUM! El trabajo pasa a estar EN PROGRESO
        db.collection("requests").document(requestId).update("status", "in_progress")
            .addOnSuccessListener {
                Toast.makeText(this, "¡Trabajo Confirmado! El prestador ya fue avisado.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al confirmar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rechazarODescartar(requestId: String, currentStatus: String) {
        // Si el prestador la había rechazado, simplemente borramos el documento para que ya no estorbe.
        // Si el cliente no quiso el precio final, también lo borramos o cancelamos.
        db.collection("requests").document(requestId).delete()
            .addOnSuccessListener {
                val msj = if (currentStatus == "rejected") "Aviso descartado" else "Trato rechazado y cancelado"
                Toast.makeText(this, msj, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNav() {
        // 🔥 Asegúrate de que tu BottomNavigationView se llame bottomNavigation o ajusta aquí el nombre
        binding.bottomNavigation.selectedItemId = R.id.nav_notifications

        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
                    // 🔥 Corrección: Te mandamos a ChatListActivity (la que tiene Firebase Realtime)
                    startActivity(Intent(this, ChatListActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_notifications -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                else -> false
            }
        }
    }
}