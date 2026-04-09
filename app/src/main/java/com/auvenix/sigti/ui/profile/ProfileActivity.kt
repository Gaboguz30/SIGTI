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
import com.auvenix.sigti.ui.auth.LoginActivity
import com.auvenix.sigti.ui.provider.plans.ProviderPlansActivity
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import com.auvenix.sigti.ui.support.QueEsSigtiActivity

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

        val rolGuardado = sessionManager.getRole()

        if (rolGuardado != null) {
            configurarBottomMenu(rolGuardado)
        }

        loadUserData()   // 🔥 ESTA ES LA CLAVE QUE TE FALTA
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

                    // Si el rol en Firebase es diferente al que tenía el teléfono, lo actualiza.
                    if (sessionManager.getRole() != rol) {
                        sessionManager.saveRole(rol)
                        configurarBottomMenu(rol)
                    }

                    binding.tvProfileName.text = "$nombre $apPaterno $apMaterno".trim()

                    if (rol == "PRESTADOR") {
                        binding.tvProfilePlan.text = "Prestador | $plan"
                        configurarComoPrestador(isAvailable)
                    } else {
                        binding.tvProfilePlan.text = "Solicitante"
                        configurarComoSolicitante()
                    }
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
            val url = "sigti.com.mx/index#planes"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas salir?")
                .setPositiveButton("Sí, salir") { _, _ -> cerrarSesion() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        binding.llAyuda.setOnClickListener {
            startActivity(Intent(this, com.auvenix.sigti.ui.support.AyudaActivity::class.java))
        }

        binding.llTerminos.setOnClickListener {
            val url = "https://sigti.com.mx/terminos"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        binding.btnQueEs.setOnClickListener {
            startActivity(Intent(this, QueEsSigtiActivity::class.java))
        }
    }

    private fun cerrarSesion() {
        auth.signOut()
        sessionManager.clearToken()
        sessionManager.clearAll() // Limpiamos todo al salir
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // 🔥 AQUI ESTA LA MAGIA CORREGIDA PARA AMBOS MUNDOS
    private fun configurarBottomMenu(rol: String) {
        binding.bottomNavigation.menu.clear()

        if (rol == "PRESTADOR") {
            binding.bottomNavigation.inflateMenu(R.menu.provider_bottom_nav_menu)
            binding.bottomNavigation.selectedItemId = R.id.nav_profile // Apuntamos al ID correcto del perfil

            binding.bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home_provider -> { startActivity(Intent(this, ProviderHomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    // 🔥 Catálogo y Trabajos en su nuevo lugar
                    R.id.nav_catalog -> { startActivity(Intent(this, ProviderCatalogActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_chat -> { startActivity(Intent(this, ProviderChatActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_jobs -> { startActivity(Intent(this, ProviderJobsActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_profile -> true
                    else -> false
                }
            }
        } else {
            // 🔥 MENÚ DEL SOLICITANTE / CLIENTE
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
            binding.bottomNavigation.selectedItemId = R.id.nav_profile

            binding.bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_map -> { startActivity(Intent(this, UserMapActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_chat -> {
                        val intent = Intent(this, ProviderChatActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                        finish()
                        true
                    }
                    // 🔥 AQUÍ CONECTAMOS LA PANTALLA RECICLADA DE TRABAJOS AL USUARIO
                    R.id.nav_jobs -> {
                        startActivity(Intent(this, ProviderJobsActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                        true
                    }
                    R.id.nav_profile -> true
                    else -> false
                }
            }
        }
    }
}