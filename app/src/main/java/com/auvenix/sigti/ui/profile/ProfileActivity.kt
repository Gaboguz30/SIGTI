package com.auvenix.sigti.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProfileBinding
import com.auvenix.sigti.ui.auth.AuthEntryActivity
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserChatsActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.home.UserNotificationsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // Evita disparar Firestore mientras cargamos el estado inicial
    private var isLoadingState = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfileData()      // ✅ nombre real desde Firestore
        loadSwitchStates()     // ✅ FIX 3: estados de switches desde Firestore
        setupSwitchListeners() // ✅ FIX 3: guarda cambios en Firestore
        setupNavigationBar()
        setupButtons()
    }

    // ══════════════════════════════════════════════════
    //  CARGAR NOMBRE Y PLAN DESDE FIRESTORE
    // ══════════════════════════════════════════════════
    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre   = doc.getString("nombre")    ?: "Usuario"
                    val paterno  = doc.getString("apPaterno") ?: ""   // ✅ campo correcto
                    binding.tvProfileName.text = "$nombre $paterno".trim()

                    val plan = doc.getString("plan_actual") ?: "FREE"
                    binding.tvProfilePlan.text = "Plan $plan"
                }
            }
    }

    // ══════════════════════════════════════════════════
    //  CARGAR ESTADOS DE LOS SWITCHES
    // ══════════════════════════════════════════════════
    private fun loadSwitchStates() {
        val uid = auth.currentUser?.uid ?: return
        isLoadingState = true

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.switchStatus.isChecked        = doc.getBoolean("online")         ?: false
                    binding.switchNotifications.isChecked = doc.getBoolean("notificaciones") ?: false
                }
                isLoadingState = false
            }
            .addOnFailureListener { isLoadingState = false }
    }

    // ══════════════════════════════════════════════════
    //  GUARDAR CAMBIOS DE SWITCHES EN FIRESTORE
    // ══════════════════════════════════════════════════
    private fun setupSwitchListeners() {
        val uid = auth.currentUser?.uid ?: return

        binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            db.collection("users").document(uid).update("online", isChecked)
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                }
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            db.collection("users").document(uid).update("notificaciones", isChecked)
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar notificaciones", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupButtons() {
        binding.btnLogout.setOnClickListener { cerrarSesionTotal() }
        binding.btnMyData.setOnClickListener {
            Toast.makeText(this, "Mis Datos próximamente...", Toast.LENGTH_SHORT).show()
        }
    }

    // ══════════════════════════════════════════════════
    //  BOTTOM NAVIGATION — FIX 5: sin finish() para mantener back stack
    // ══════════════════════════════════════════════════
    private fun setupNavigationBar() {
        binding.bottomNavigation.selectedItemId = R.id.nav_profile

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
                    startActivity(Intent(this, UserChatsActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, UserNotificationsActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun cerrarSesionTotal() {
        auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AuthEntryActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}