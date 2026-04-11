package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.auvenix.sigti.databinding.ActivityPasswordBinding
import com.auvenix.sigti.ui.register.RegisterGeneralActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class PasswordActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPasswordBinding
    private lateinit var auth    : FirebaseAuth
    private lateinit var db      : FirebaseFirestore
    private lateinit var storage : FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val role  = intent.getStringExtra(RegisterGeneralActivity.EXTRA_ROLE).orEmpty()
        val email = intent.getStringExtra(RegisterGeneralActivity.EXTRA_EMAIL).orEmpty()

        setupRealtimeValidation()

        binding.btnContinuar.setOnClickListener {
            if (!validatePasswords(showErrors = true)) return@setOnClickListener
            val pass = binding.etPassword.text.toString()

            binding.btnContinuar.isEnabled = false
            binding.btnContinuar.text = "Creando cuenta..."

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            subirFotoYGuardarExpediente(uid, role, email)
                        } else {
                            restaurarBoton()
                            Toast.makeText(this, "Error: No se pudo obtener el UID", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        restaurarBoton()
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun subirFotoYGuardarExpediente(uid: String, role: String, email: String) {
        binding.btnContinuar.text = "Subiendo foto..."

        val fotoUriString = intent.getStringExtra("extra_url_selfie")

        if (!fotoUriString.isNullOrEmpty()) {
            val uri = Uri.parse(fotoUriString)
            val ref = storage.reference.child("profile_pictures").child("$uid.jpg")

            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { urlDescarga ->
                    guardarExpedienteEnBD(uid, role, email, urlDescarga.toString())
                }
            }.addOnFailureListener {
                guardarExpedienteEnBD(uid, role, email, "")
            }
        } else {
            guardarExpedienteEnBD(uid, role, email, "")
        }
    }

    private fun guardarExpedienteEnBD(uid: String, role: String, email: String, fotoUrl: String) {
        binding.btnContinuar.text = "Configurando perfil..."

        val listaOficiosCruda = intent.getStringArrayListExtra("extra_oficios") ?: arrayListOf()
        val oficiosProcesados = listaOficiosCruda.map { s ->
            val p = s.split("|")
            mapOf("nombre" to (p.getOrNull(0) ?: ""), "anios_experiencia" to (p.getOrNull(1)?.toIntOrNull() ?: 0))
        }

        val expediente = hashMapOf(
            "uid"                     to uid,
            "role"                    to role,
            "email"                   to email,
            "nombre"                  to intent.getStringExtra(RegisterGeneralActivity.EXTRA_NOMBRE).orEmpty(),
            "apPaterno"               to intent.getStringExtra(RegisterGeneralActivity.EXTRA_AP_PATERNO).orEmpty(),
            "apMaterno"               to intent.getStringExtra(RegisterGeneralActivity.EXTRA_AP_MATERNO).orEmpty(),
            "fechaNac"                to intent.getStringExtra(RegisterGeneralActivity.EXTRA_FECHA_NAC).orEmpty(),
            "genero"                  to intent.getStringExtra(RegisterGeneralActivity.EXTRA_GENERO).orEmpty(),
            "url_selfie"              to fotoUrl,
            "ciudad"                  to intent.getStringExtra("extra_ciudad").orEmpty(),
            "direccion"               to intent.getStringExtra("extra_direccion").orEmpty(),
            "oficios"                 to oficiosProcesados,
            "plan_actual"             to "FREE",
            "trabajos_realizados_mes" to 0,
            "online"                  to false,
            "notificaciones"          to true,
            "metodoRegistro"          to "Correo",
            "perfil_completado"       to true,
            "fechaRegistro"           to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        // 🔥 COLECCIÓN LIMPIA "users"
        db.collection("users").document(uid).set(expediente)
            .addOnSuccessListener { enviarCorreoDeVerificacion(role, email) }
            .addOnFailureListener { e ->
                restaurarBoton()
                Toast.makeText(this, "Error al guardar perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun enviarCorreoDeVerificacion(role: String, email: String) {
        binding.btnContinuar.text = "Enviando verificación..."
        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
            if (emailTask.isSuccessful) {
                startActivity(Intent(this, VerifyEmailActivity::class.java).apply {
                    putExtra(EXTRA_ROLE,    role)
                    putExtra(EXTRA_EMAIL,   email)
                    putExtra(EXTRA_RECORDAR, binding.cbRecuerdame.isChecked)
                })
                finish()
            } else {
                restaurarBoton()
                Toast.makeText(this, "Cuenta creada, pero falló el envío del correo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun restaurarBoton() {
        binding.btnContinuar.isEnabled = true
        binding.btnContinuar.text = "Continuar"
    }

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
        val pass  = binding.etPassword.text?.toString().orEmpty()
        val pass2 = binding.etConfirmPassword.text?.toString().orEmpty()

        if (pass.isBlank())  { ok = false; if (showErrors) binding.tilPassword.error = "Obligatorio" }
        if (pass2.isBlank()) { ok = false; if (showErrors) binding.tilConfirmPassword.error = "Obligatorio" }

        if (pass.isNotBlank()) {
            val errors = passwordRuleErrors(pass)
            if (errors.isNotEmpty()) {
                ok = false
                if (showErrors) {
                    binding.tilPassword.error = errors.first()
                    binding.tvPasswordHint.text = errors.joinToString("\n") { "• $it" }
                    binding.tvPasswordHint.visibility = android.view.View.VISIBLE
                }
            }
        }
        if (pass.isNotBlank() && pass2.isNotBlank() && pass != pass2) {
            ok = false; if (showErrors) binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
        }
        return ok
    }

    private fun passwordRuleErrors(pass: String): List<String> {
        val errs = mutableListOf<String>()
        if (pass.length < 8) errs.add("Mínimo 8 caracteres")
        if (pass.firstOrNull()?.isUpperCase() != true) errs.add("La primera letra debe ser mayúscula")
        if (!pass.any { it.isDigit() }) errs.add("Debe contener al menos 1 número")
        if (!pass.any { it in "!@#\$%^&*()_+-=[]{};':\"\\|,.<>/?`~" }) errs.add("Debe contener al menos 1 carácter especial")
        return errs
    }

    companion object {
        const val EXTRA_ROLE       = "extra_role"
        const val EXTRA_EMAIL      = "extra_email"
        const val EXTRA_RECORDAR   = "extra_recordar"
    }
}