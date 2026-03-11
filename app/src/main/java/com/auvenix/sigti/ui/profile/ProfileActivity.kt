package com.auvenix.sigti.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProfileBinding
import com.auvenix.sigti.ui.auth.AuthEntryActivity
import com.auvenix.sigti.ui.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. CARGAR DATOS REALES DE FIRESTORE
        cargarDatosDesdeFirebase()

        // 2. CONFIGURAR EL NAVEGADOR (Bottom Nav Bar)
        setupNavigationBar()

        // 3. BOTÓN CERRAR SESIÓN
        binding.btnLogout.setOnClickListener {
            cerrarSesionTotal()
        }

        // 4. CLICS EN LAS OTRAS OPCIONES (STUBS)
        binding.btnMyData.setOnClickListener {
            Toast.makeText(this, "Mis Datos próximamente...", Toast.LENGTH_SHORT).show()
        }

        binding.btnSupport.setOnClickListener {
            Toast.makeText(this, "Abriendo Soporte Técnico...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosDesdeFirebase() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = doc.getString("nombre") ?: "Usuario"
                    val paterno = doc.getString("apPaterno") ?: ""
                    binding.tvProfileName.text = "$nombre $paterno"
                }
            }
    }

    private fun setupNavigationBar() {
        // Marcamos que estamos en la pestaña de Perfil
        binding.bottomNavigation.selectedItemId = R.id.nav_profile

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Vamos al Home y quitamos animaciones feas para que se vea fluido
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish() // Cerramos esta para no amontonar pantallas
                    true
                }

                R.id.nav_profile -> true // Ya estamos aquí, no hace nada
                else -> false
            }
        }
    }

    private fun cerrarSesionTotal() {
        // A. Cerramos sesión en Firebase (La pulsera del club)
        auth.signOut()

        // B. Cerramos sesión en Google (Importante: si no haces esto,
        // la próxima vez entrará con la misma cuenta sin preguntar)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleClient = GoogleSignIn.getClient(this, gso)

        googleClient.signOut().addOnCompleteListener {
            // C. Mandamos al usuario hasta el principio (AuthEntry)
            val intent = Intent(this, AuthEntryActivity::class.java)

            // D. QUEMAMOS EL PUENTE: Limpiamos historial para que no pueda dar "atrás"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()

            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
        }
    }
}