package com.auvenix.sigti.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityVerifyEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// IMPORTANTE: Cambia 'HomeActivity' por el nombre real de tu pantalla de inicio
import com.auvenix.sigti.ui.home.HomeActivity

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnCheckVerification.setOnClickListener {
            val user = auth.currentUser
            user?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (user.isEmailVerified) {
                        // AQUÍ LLAMAMOS A LA FUNCIÓN QUE GUARDA Y NOS MANDA AL HOME
                        guardarDatosFinales(user.uid)
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

    private fun guardarDatosFinales(uid: String) {
        // 1. Sacamos los datos que venían de la maleta (Intent)
        val role = intent.getStringExtra(PasswordActivity.EXTRA_ROLE).orEmpty()
        val email = intent.getStringExtra(PasswordActivity.EXTRA_EMAIL).orEmpty()
        val password = intent.getStringExtra(PasswordActivity.EXTRA_PASSWORD).orEmpty()
        val nombre = intent.getStringExtra(PasswordActivity.EXTRA_NOMBRE).orEmpty()
        val apPaterno = intent.getStringExtra(PasswordActivity.EXTRA_AP_PATERNO).orEmpty()
        val apMaterno = intent.getStringExtra(PasswordActivity.EXTRA_AP_MATERNO).orEmpty()
        val fechaNac = intent.getStringExtra(PasswordActivity.EXTRA_FECHA_NAC).orEmpty()
        val genero = intent.getStringExtra(PasswordActivity.EXTRA_GENERO).orEmpty()
        val recordar = intent.getBooleanExtra(PasswordActivity.EXTRA_RECORDAR, false)

        val userData = hashMapOf(
            "uid" to uid,
            "email" to email,
            "role" to role,
            "nombre" to nombre,
            "apPaterno" to apPaterno,
            "apMaterno" to apMaterno,
            "fechaNac" to fechaNac,
            "genero" to genero,
            "fechaRegistro" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        // 2. Subimos a Firestore
        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {

                // 3. Si pidió "Recuérdame", guardamos en el celular
                if (recordar && email.isNotEmpty() && password.isNotEmpty()) {
                    guardarCredencialesEnCelular(email, password)
                }

                Toast.makeText(this, "¡Bienvenido a SIGTI!", Toast.LENGTH_SHORT).show()

                // --- AQUÍ VA EL CÓDIGO DEL SALTO AL HOME ---
                val intentHome = Intent(this, HomeActivity::class.java)

                // Estas banderas borran el historial de pantallas de login
                // para que no pueda regresarse a verificar si ya entró.
                intentHome.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intentHome)
                finish() // Cerramos esta actividad
                // -------------------------------------------
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar en BD: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun guardarCredencialesEnCelular(email: String, pass: String) {
        val prefs = getSharedPreferences("MisCredencialesSIGTI", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("email", email)
            putString("password", pass)
            putBoolean("recordarme", true)
            apply()
        }
    }
}