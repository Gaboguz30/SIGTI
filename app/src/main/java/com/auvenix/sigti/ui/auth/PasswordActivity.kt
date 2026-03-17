package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.auvenix.sigti.databinding.ActivityPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // <-- IMPORTANTE: Agregamos Firestore

class PasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore // <-- 1. Declaramos Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance() // <-- 2. Inicializamos Firestore

        // Recibir datos previos de la maleta
        val role = intent.getStringExtra(EXTRA_ROLE).orEmpty()
        val email = intent.getStringExtra(EXTRA_EMAIL).orEmpty()

        setupRealtimeValidation()

        binding.btnContinuar.setOnClickListener {
            if (!validatePasswords(showErrors = true)) return@setOnClickListener

            val pass = binding.etPassword.text.toString()
            binding.btnContinuar.isEnabled = false

            // 3. EL CERRAJERO: Creamos la cuenta en Firebase Auth
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        // Obtenemos el ID único que nos acaba de dar Firebase (Ej. abc123xyz)
                        val uid = auth.currentUser?.uid

                        if (uid != null) {
                            // 4. EL ARCHIVISTA: Preparamos y Guardamos en Firestore
                            guardarExpedienteEnBD(uid, role, email, pass)
                        } else {
                            binding.btnContinuar.isEnabled = true
                            Toast.makeText(this, "Error: No se pudo obtener el UID", Toast.LENGTH_LONG).show()
                        }

                    } else {
                        binding.btnContinuar.isEnabled = true
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun guardarExpedienteEnBD(uid: String, role: String, email: String, pass: String) {

        // A. Traducimos los oficios (Cortamos el "Plomero|5" por la mitad)
        val listaOficiosCruda = intent.getStringArrayListExtra("extra_oficios") ?: arrayListOf()
        val oficiosProcesados = listaOficiosCruda.map { stringCrudo ->
            val partes = stringCrudo.split("|")
            mapOf(
                "nombre" to (partes.getOrNull(0) ?: ""),
                "anios_experiencia" to (partes.getOrNull(1)?.toIntOrNull() ?: 0)
            )
        }

        // B. Armamos el Expediente completo
        val expedienteUsuario = hashMapOf(
            "uid" to uid,
            "rol" to role,
            "email" to email,
            "nombre" to intent.getStringExtra(EXTRA_NOMBRE).orEmpty(),
            "ap_paterno" to intent.getStringExtra(EXTRA_AP_PATERNO).orEmpty(),
            "ap_materno" to intent.getStringExtra(EXTRA_AP_MATERNO).orEmpty(),
            "fecha_nac" to intent.getStringExtra(EXTRA_FECHA_NAC).orEmpty(),
            "genero" to intent.getStringExtra(EXTRA_GENERO).orEmpty(),
            "ciudad" to intent.getStringExtra("extra_ciudad").orEmpty(), // Exclusivo del prestador
            "oficios" to oficiosProcesados, // Exclusivo del prestador

            // --- NUESTRO MODELO DE NEGOCIO ---
            "plan_actual" to "FREE",
            "trabajos_realizados_mes" to 0,

            // Fecha exacta en la que se registró en el servidor de Firebase
            "fecha_registro" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        // C. Lo guardamos en el Archivero "Usuarios"
        db.collection("Usuarios").document(uid).set(expedienteUsuario)
            .addOnSuccessListener {
                // Si el archivista terminó bien, llamamos al Cartero
                enviarCorreoDeVerificacion(role, email, pass)
            }
            .addOnFailureListener { e ->
                binding.btnContinuar.isEnabled = true
                Toast.makeText(this, "Error al guardar perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun enviarCorreoDeVerificacion(role: String, email: String, pass: String) {
        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
            if (emailTask.isSuccessful) {
                // 5. TODO SALIÓ PERFECTO: Nos vamos a VerifyEmailActivity
                val i = Intent(this, VerifyEmailActivity::class.java).apply {
                    putExtra(EXTRA_ROLE, role)
                    putExtra(EXTRA_EMAIL, email)
                    putExtra(EXTRA_PASSWORD, pass)
                    putExtra(EXTRA_RECORDAR, binding.cbRecuerdame.isChecked)

                    // Ya no pasamos el resto de datos porque YA ESTÁN A SALVO EN FIRESTORE
                }
                startActivity(i)
                finish()
            } else {
                binding.btnContinuar.isEnabled = true
                Toast.makeText(this, "Cuenta creada, pero falló el envío del correo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- LAS VALIDACIONES DE CONTRASEÑA SE QUEDAN EXACTAMENTE IGUAL QUE ANTES ---
    private fun setupRealtimeValidation() {
        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
            binding.tvPasswordHint.visibility = android.view.View.GONE
        }
        binding.etConfirmPassword.doAfterTextChanged {
            binding.tilConfirmPassword.error = null
            binding.tvPasswordHint.visibility = android.view.View.GONE
        }
    }

    private fun validatePasswords(showErrors: Boolean): Boolean {
        var ok = true
        val pass = binding.etPassword.text?.toString().orEmpty()
        val pass2 = binding.etConfirmPassword.text?.toString().orEmpty()

        if (pass.isBlank()) {
            ok = false
            if (showErrors) binding.tilPassword.error = "Obligatorio"
        }
        if (pass2.isBlank()) {
            ok = false
            if (showErrors) binding.tilConfirmPassword.error = "Obligatorio"
        }
        if (pass.isNotBlank()) {
            val errors = passwordRuleErrors(pass)
            if (errors.isNotEmpty()) {
                ok = false
                if (showErrors) {
                    binding.tilPassword.error = errors.first()
                    binding.tvPasswordHint.text = errors.joinToString(separator = "\n") { "• $it" }
                    binding.tvPasswordHint.visibility = android.view.View.VISIBLE
                }
            }
        }
        if (pass.isNotBlank() && pass2.isNotBlank() && pass != pass2) {
            ok = false
            if (showErrors) binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
        }
        return ok
    }

    private fun passwordRuleErrors(pass: String): List<String> {
        val errs = mutableListOf<String>()
        if (pass.length < 8) errs.add("Mínimo 8 caracteres")
        val first = pass.firstOrNull()
        if (first == null || !first.isUpperCase()) errs.add("La primera letra debe ser mayúscula")
        if (!pass.any { it.isDigit() }) errs.add("Debe contener al menos 1 número")
        val special = "!@#\$%^&*()_+-=[]{};':\"\\|,.<>/?`~"
        if (!pass.any { it in special }) errs.add("Debe contener al menos 1 carácter especial")
        return errs
    }

    companion object {
        const val EXTRA_ROLE = "extra_role"
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_PASSWORD = "extra_password"
        const val EXTRA_NOMBRE = "extra_nombre"
        const val EXTRA_AP_PATERNO = "extra_ap_paterno"
        const val EXTRA_AP_MATERNO = "extra_ap_materno"
        const val EXTRA_FECHA_NAC = "extra_fecha_nac"
        const val EXTRA_GENERO = "extra_genero"
        const val EXTRA_RECORDAR = "extra_recordar"
    }
}