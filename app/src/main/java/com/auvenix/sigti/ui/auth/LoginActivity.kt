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

        binding.btnLogin.setOnClickListener {
            if (!validate(showErrors = true)) return@setOnClickListener

            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString()

            // ✅ Por ahora: stub (hasta conectar backend)
            // Simulamos login exitoso si password no está vacía
            Toast.makeText(this, "Login OK (stub)", Toast.LENGTH_SHORT).show()

            // Cuando conectemos backend, aquí guardaremos token:
            // session.saveToken(token)

            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
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