package com.auvenix.sigti.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProfileBinding
import com.auvenix.sigti.session.SessionManager
import com.auvenix.sigti.ui.auth.LoginActivity
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.support.MyReportsActivity // 🔥 Importamos la nueva pantalla
import com.auvenix.sigti.ui.support.QueEsSigtiActivity
import com.bumptech.glide.Glide
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

        val rolGuardado = sessionManager.getRole()

        if (rolGuardado != null) {
            configurarBottomMenu(rolGuardado)
        }

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

                    if (sessionManager.getRole() != rol) {
                        sessionManager.saveRole(rol)
                        configurarBottomMenu(rol)
                    }

                    binding.tvProfileName.text = "$nombre $apPaterno $apMaterno".trim()

                    var urlFoto = doc.getString("url_selfie")
                    if (urlFoto.isNullOrEmpty()) {
                        val documentacion = doc.get("documentacion") as? Map<*, *>
                        urlFoto = documentacion?.get("url_selfie")?.toString()
                    }

                    if (!urlFoto.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(urlFoto)
                            .circleCrop()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(binding.ivProfilePic)
                    } else {
                        Glide.with(this)
                            .load(android.R.drawable.ic_menu_gallery)
                            .circleCrop()
                            .into(binding.ivProfilePic)
                    }

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

        binding.switchStatus.setOnCheckedChangeListener(null)
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

        // 🔥 AQUÍ ESTÁ EL NUEVO BOTÓN DE MIS REPORTES 🔥
        binding.btnMyReports.setOnClickListener {
            startActivity(Intent(this, MyReportsActivity::class.java))
        }

        binding.llUpgradePlan.setOnClickListener {
            var url = "http://sigti.com.mx/index#planes"
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://$url"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            mostrarDialogConfirmacion(
                "Cerrar Sesión",
                "¿Estás seguro de que deseas salir?"
            ) {
                cerrarSesion()
            }
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
        sessionManager.clearAll()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun configurarBottomMenu(rol: String) {
        binding.bottomNavigation.menu.clear()

        if (rol == "PRESTADOR") {
            binding.bottomNavigation.inflateMenu(R.menu.provider_bottom_nav_menu)
            binding.bottomNavigation.selectedItemId = R.id.nav_profile

            binding.bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home_provider -> { irAPantalla(ProviderHomeActivity::class.java); true }
                    R.id.nav_catalog -> { irAPantalla(ProviderCatalogActivity::class.java); true }
                    R.id.nav_chat -> { irAPantalla(ProviderChatActivity::class.java); true }
                    R.id.nav_jobs -> { irAPantalla(ProviderJobsActivity::class.java); true }
                    R.id.nav_profile -> true
                    else -> false
                }
            }
        } else {
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
            binding.bottomNavigation.selectedItemId = R.id.nav_profile

            binding.bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { irAPantalla(HomeActivity::class.java); true }
                    R.id.nav_map -> { irAPantalla(UserMapActivity::class.java); true }
                    R.id.nav_chat -> { irAPantalla(ProviderChatActivity::class.java); true }
                    R.id.nav_jobs -> { irAPantalla(ProviderJobsActivity::class.java); true }
                    R.id.nav_profile -> true
                    else -> false
                }
            }
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

    private fun mostrarDialogConfirmacion(
        titulo: String,
        mensaje: String,
        onConfirm: () -> Unit
    ) {
        val view = layoutInflater.inflate(R.layout.dialog_confirmacion, null)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensaje)
        val btnSi = view.findViewById<TextView>(R.id.btnSi)
        val btnNo = view.findViewById<TextView>(R.id.btnNo)

        tvTitulo.text = titulo
        tvMensaje.text = mensaje

        val dialog = AlertDialog.Builder(this).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        btnSi.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }
    }
}