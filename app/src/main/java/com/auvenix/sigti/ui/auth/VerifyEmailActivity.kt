package com.auvenix.sigti.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityVerifyEmailBinding
import com.google.firebase.auth.FirebaseAuth
// IMPORTANTE: Asegúrate de que las rutas coincidan con tus carpetas reales
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnCheckVerification.setOnClickListener {
            val user = auth.currentUser
            user?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (user.isEmailVerified) {
                        // ¡AQUÍ ESTÁ LA CLAVE! Ya no llamamos a Firestore para nada, solo redireccionamos.
                        finalizarRegistroYRedireccionar()
                    } else {
                        Toast.makeText(this, "Aún no verificas tu correo.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.btnResendEmail.setOnClickListener {
            auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                Toast.makeText(this, "Correo reenviado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun finalizarRegistroYRedireccionar() {
        val role = intent.getStringExtra(PasswordActivity.EXTRA_ROLE).orEmpty()
        val email = intent.getStringExtra(PasswordActivity.EXTRA_EMAIL).orEmpty()
        val password = intent.getStringExtra(PasswordActivity.EXTRA_PASSWORD).orEmpty()
        val recordar = intent.getBooleanExtra(PasswordActivity.EXTRA_RECORDAR, false)

        // Guardamos credenciales en el celular si el usuario lo pidió
        if (recordar && email.isNotEmpty() && password.isNotEmpty()) {
            val prefs = getSharedPreferences("MisCredencialesSIGTI", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("email", email)
                putString("password", password)
                putBoolean("recordarme", true)
                apply()
            }
        }

        Toast.makeText(this, "¡Bienvenido a SIGTI!", Toast.LENGTH_SHORT).show()

        // EL AGENTE DE TRÁNSITO
        val intentHome = if (role == "PRESTADOR") {
            Intent(this, ProviderHomeActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }

        intentHome.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intentHome)
        finish()
    }
}