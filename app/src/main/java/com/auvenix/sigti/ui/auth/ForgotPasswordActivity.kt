package com.auvenix.sigti.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón principal de enviar correo
        binding.btnSendResetLink.setOnClickListener {
            val email = binding.etEmailReset.text.toString().trim()

            // Validación rápida
            if (email.isEmpty()) {
                binding.tilEmail.error = "Por favor ingresa tu correo"
                return@setOnClickListener
            }
            binding.tilEmail.error = null

            // Desactivamos el botón para que no le piquen 10 veces seguidas
            binding.btnSendResetLink.isEnabled = false
            binding.btnSendResetLink.text = "Enviando..."

            // El Cerrajero manda el correo
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "¡Listo! Revisa tu bandeja de entrada o SPAM.", Toast.LENGTH_LONG).show()
                        finish() // Cerramos esta pantalla y devolvemos al usuario al Login
                    } else {
                        binding.btnSendResetLink.isEnabled = true
                        binding.btnSendResetLink.text = "Enviar Enlace de Recuperación"
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}