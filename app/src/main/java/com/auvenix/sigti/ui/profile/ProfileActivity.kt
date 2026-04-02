package com.auvenix.sigti.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProfileBinding
import com.auvenix.sigti.session.SessionManager
import com.auvenix.sigti.ui.role.RoleActivity
import com.auvenix.sigti.ui.provider.plans.ProviderPlansActivity
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.home.UserNotificationsActivity
import com.auvenix.sigti.ui.chat.ChatListActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // 🔥 LA MAGIA CONTRA EL PARPADEO:
        // Leemos el rol de la memoria local (1 milisegundo) y pintamos la barra AL INSTANTE.
        // Nota: Asumo que en tu SessionManager la función se llama getRole(). Si le pusiste otro nombre (como fetchRole), solo cámbialo aquí.
        val rolGuardado = sessionManager.getRole() ?: "SOLICITANTE"
        configurarBottomMenu(rolGuardado)

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            cerrarSesion()
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = doc.getString("nombre") ?: ""
                    val apPaterno = doc.getString("apPaterno") ?: ""
                    val apMaterno = doc.getString("apMaterno") ?: ""
                    val rol = doc.getString("role") ?: "SOLICITANTE"
                    val plan = doc.getString("plan_actual") ?: "FREE"
                    val isAvailable = doc.getBoolean("online") ?: false

                    binding.tvProfileName.text = "$nombre $apPaterno $apMaterno".trim()

                    if (rol == "PRESTADOR") {
                        binding.tvProfilePlan.text = "Prestador | $plan"
                        configurarComoPrestador(isAvailable)
                    } else {
                        binding.tvProfilePlan.text = "Solicitante"
                        configurarComoSolicitante()
                    }

                    // 🔥 Aquí borramos el configurarBottomMenu que causaba el parpadeo
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configurarComoPrestador(isAvailable: Boolean) {
        binding.llStatus.visibility = View.VISIBLE
        binding.vDividerPref1.visibility = View.VISIBLE
        binding.llUpgradePlan.visibility = View.VISIBLE
        binding.vDividerPref2.visibility = View.VISIBLE

        binding.switchStatus.isChecked = isAvailable

        binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            auth.currentUser?.uid?.let { uid ->
                db.collection("users").document(uid).update("online", isChecked)
            }
        }
    }

    private fun configurarComoSolicitante() {
        binding.llStatus.visibility = View.GONE
        binding.vDividerPref1.visibility = View.GONE
        binding.llUpgradePlan.visibility = View.GONE
        binding.vDividerPref2.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.btnMyData.setOnClickListener {
            startActivity(Intent(this, MyDataActivity::class.java))
        }

        binding.btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        binding.llUpgradePlan.setOnClickListener {
            startActivity(Intent(this, ProviderPlansActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas salir?")
                .setPositiveButton("Sí, salir") { _, _ -> cerrarSesion() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun cerrarSesion() {
        auth.signOut()
        sessionManager.clearToken() // Asegúrate de limpiar todo en tu SessionManager
        val intent = Intent(this, RoleActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun configurarBottomMenu(rol: String) {
        binding.bottomNavigation.menu.clear()

        if (rol == "PRESTADOR") {
            binding.bottomNavigation.inflateMenu(R.menu.provider_bottom_nav_menu)
            binding.bottomNavigation.selectedItemId = R.id.nav_provider_profile

            binding.bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_provider_home -> { startActivity(Intent(this, ProviderHomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_provider_jobs -> { startActivity(Intent(this, ProviderJobsActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_provider_chat -> { startActivity(Intent(this, ProviderChatActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_provider_catalog -> { startActivity(Intent(this, ProviderCatalogActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_provider_profile -> true
                    else -> false
                }
            }
        } else {
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
            binding.bottomNavigation.selectedItemId = R.id.nav_profile

            binding.bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_map -> { startActivity(Intent(this, UserMapActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_chat -> { startActivity(Intent(this, ChatListActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_notifications -> { startActivity(Intent(this, UserNotificationsActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_profile -> true
                    else -> false
                }
            }
        }
    }
}