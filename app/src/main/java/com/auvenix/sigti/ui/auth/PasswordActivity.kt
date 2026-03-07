package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.auvenix.sigti.databinding.ActivityPasswordBinding

class PasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos previos (mínimo email y role)
        val role = intent.getStringExtra(EXTRA_ROLE).orEmpty()
        val email = intent.getStringExtra(EXTRA_EMAIL).orEmpty()

        setupRealtimeValidation()

        binding.btnContinuar.setOnClickListener {
            if (!validatePasswords(showErrors = true)) return@setOnClickListener

            val pass = binding.etPassword.text.toString()

            // Siguiente: verificar correo
            val i = Intent(this, VerifyEmailActivity::class.java).apply {
                putExtra(EXTRA_ROLE, role)
                putExtra(EXTRA_EMAIL, email)
                putExtra(EXTRA_PASSWORD, pass)

                // reenviar lo demás si lo traes
                forwardIfPresent(this@PasswordActivity.intent, this, EXTRA_NOMBRE)
                forwardIfPresent(this@PasswordActivity.intent, this, EXTRA_AP_PATERNO)
                forwardIfPresent(this@PasswordActivity.intent, this, EXTRA_AP_MATERNO)
                forwardIfPresent(this@PasswordActivity.intent, this, EXTRA_FECHA_NAC)
                forwardIfPresent(this@PasswordActivity.intent, this, EXTRA_GENERO)
            }
            startActivity(i)
        }
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

    /**
     * Reglas:
     * - 8+ chars
     * - primera letra mayúscula
     * - al menos 1 número
     * - al menos 1 especial
     */
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

    private fun forwardIfPresent(from: Intent, to: Intent, key: String) {
        if (from.hasExtra(key)) {
            to.putExtra(key, from.getStringExtra(key))
        }
    }

    companion object {
        const val EXTRA_ROLE = "extra_role"
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_PASSWORD = "extra_password"

        // extras opcionales (si ya los usas)
        const val EXTRA_NOMBRE = "extra_nombre"
        const val EXTRA_AP_PATERNO = "extra_ap_paterno"
        const val EXTRA_AP_MATERNO = "extra_ap_materno"
        const val EXTRA_FECHA_NAC = "extra_fecha_nac"
        const val EXTRA_GENERO = "extra_genero"
    }
}