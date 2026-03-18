package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.auvenix.sigti.databinding.ActivityLoginBinding
import com.auvenix.sigti.session.SessionManager
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.role.RoleActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // <-- 1. NUEVO IMPORT PARA LA BASE DE DATOS

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        session.getRememberedEmail()?.let { binding.etEmail.setText(it) }
        session.getRememberedPassword()?.let { binding.etPassword.setText(it) }

        setupClearErrors()

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            if (!validate(showErrors = true)) return@setOnClickListener

            val correo = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            // 2. Bloqueamos el botón mientras piensa para evitar que el usuario le dé 20 clics
            binding.btnLogin.isEnabled = false
            Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 3. EN LUGAR DE MANDARLO AL HOME DIRECTO, REVISAMOS SU ROL
                        redireccionarSegunRol()
                    } else {
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(this, "Híjole carnal, los datos no coinciden.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RoleActivity::class.java))
        }
    }

    private fun redireccionarSegunRol() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // 4. BUSCAMOS AL USUARIO EN LA COLECCIÓN "Usuarios"
        // (OJO: Si en tu base de datos la carpeta se llama "users" en minúsculas, cámbialo aquí)
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Leemos el campo donde guardas el rol (intento "rol" o "role")
                    val rol = doc.getString("rol") ?: doc.getString("role")

                    if (rol == "PRESTADOR") {
                        // ===== CAMINO DEL PRESTADOR DE SERVICIOS =====
                        // TODO: CAMBIA "TU_ACTIVITY_DEL_PRESTADOR" POR EL NOMBRE DE TU PANTALLA "MIS TRABAJOS"

                        val intent = Intent(this, ProviderHomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()

                        Toast.makeText(this, "Bienvenido Prestador (Falta conectar pantalla)", Toast.LENGTH_LONG).show()

                    } else {
                        // ===== CAMINO DEL SOLICITANTE (CLIENTE NORMAL) =====
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "No se encontró tu perfil en la base de datos.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                binding.btnLogin.isEnabled = true
                Toast.makeText(this, "Error de red al leer datos.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupClearErrors() {
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null }
        binding.etPassword.doAfterTextChanged { binding.tilPassword.error = null }
    }

    private fun validate(showErrors: Boolean): Boolean {
        var ok = true
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.etPassword.text?.toString().orEmpty()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ok = false
            if (showErrors) binding.tilEmail.error = "Correo inválido"
        }

        if (pass.isEmpty()) {
            ok = false
            if (showErrors) binding.tilPassword.error = "Contraseña obligatoria"
        }

        return ok
    }
}