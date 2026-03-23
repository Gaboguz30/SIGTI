package com.auvenix.sigti.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.auvenix.sigti.databinding.ActivityLoginBinding
import com.auvenix.sigti.notifications.FcmTokenManager
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.session.SessionManager
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.role.RoleActivity
import com.auvenix.sigti.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var session : SessionManager

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* silencioso */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        session.getRememberedEmail()?.let { binding.etEmail.setText(it) }

        setupClearErrors()
        setupListeners()
        requestNotificationPermissionIfNeeded()
    }

    private fun setupListeners() {

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            if (!validate(showErrors = true)) return@setOnClickListener

            val correo = binding.etEmail.text.toString().trim()
            val pass   = binding.etPassword.text.toString()

            binding.btnLogin.isEnabled = false
            Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (binding.cbRememberMe.isChecked) {
                            session.saveRememberedEmail(correo)
                        } else {
                            session.clearRememberedEmail()
                        }
                        FcmTokenManager.saveCurrentToken()
                        redireccionarSegunRol()
                    } else {
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(this,
                            "Credenciales incorrectas. Verifica tu correo y contraseña.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RoleActivity::class.java))
        }
    }

    private fun redireccionarSegunRol() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection(Constants.COLLECTION_USERS).document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val rol = doc.getString(Constants.FIELD_ROL) ?: doc.getString(Constants.FIELD_ROLE)
                    val intent = if (rol == Constants.ROLE_PROVIDER)
                        Intent(this, ProviderHomeActivity::class.java)
                    else
                        Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent); finish()
                } else {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "No se encontró tu perfil. Contacta a soporte.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                binding.btnLogin.isEnabled = true
                Toast.makeText(this, "Error de conexión. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupClearErrors() {
        binding.etEmail.doAfterTextChanged    { binding.tilEmail.error    = null }
        binding.etPassword.doAfterTextChanged { binding.tilPassword.error = null }
    }

    private fun validate(showErrors: Boolean): Boolean {
        var ok = true
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass  = binding.etPassword.text?.toString().orEmpty()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ok = false; if (showErrors) binding.tilEmail.error = "Correo inválido"
        }
        if (pass.isEmpty()) {
            ok = false; if (showErrors) binding.tilPassword.error = "Contraseña obligatoria"
        }
        return ok
    }
}