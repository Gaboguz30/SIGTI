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
import com.auvenix.sigti.ui.role.RoleActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        // ✅ Autollenado si "Recuérdame" quedó guardado (ya verificado)
        session.getRememberedEmail()?.let { binding.etEmail.setText(it) }
        session.getRememberedPassword()?.let { binding.etPassword.setText(it) }

        setupClearErrors()

        // 1. Escuchamos el clic en el TEXTO de "Olvidé mi contraseña"
        binding.tvForgotPassword.setOnClickListener {
            // 2. Armamos la maleta y abrimos la pantalla de recuperación
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val correo = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            // 1. Le pedimos a Firebase que revise los datos
            FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener { task ->
                    // 2. ¡ESTA ES LA CLAVE! Preguntamos si la tarea fue exitosa
                    if (task.isSuccessful) {
                        // SÍ FUE EXITOSA: Contraseña correcta, pásale al Home
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // NO FUE EXITOSA: Contraseña mal o el usuario no existe
                        Toast.makeText(this, "Híjole carnal, los datos no coinciden.", Toast.LENGTH_SHORT).show()
                        // El botón se queda ahí para que lo intente de nuevo
                    }
                }
        }

        // Ir a registro (tu flujo arranca en RoleActivity)
        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RoleActivity::class.java))
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