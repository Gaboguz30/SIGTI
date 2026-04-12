package com.auvenix.sigti.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityLoginBinding
import com.auvenix.sigti.notifications.FcmTokenManager
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.session.SessionManager
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* silencioso */ }

    // 🔥 LANZADOR PARA EL RESULTADO DE GOOGLE
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error de Google: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnGoogleLogin.isEnabled = true
            }
        } else {
            binding.btnGoogleLogin.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        session.getRememberedEmail()?.let { binding.etEmail.setText(it) }

        // Configuración de Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de tener este ID en strings.xml (se baja del google-services.json)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
            val pass = binding.etPassword.text.toString()

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

                        // 🔥 LÓGICA INTELIGENTE: Verificamos si la cuenta es de Google
                        verificarSiEsCuentaDeGoogle(correo)
                    }
                }
        }

        // 🔥 CLIC EN BOTÓN DE GOOGLE
        binding.btnGoogleLogin.setOnClickListener {
            binding.btnGoogleLogin.isEnabled = false
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, AuthEntryActivity::class.java))
            finish()
        }
    }

    private fun verificarSiEsCuentaDeGoogle(correo: String) {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(correo)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods != null && signInMethods.contains(GoogleAuthProvider.PROVIDER_ID)) {
                        Toast.makeText(this, "Esta cuenta se registró con Google. Por favor, usa el botón de Google abajo.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Credenciales incorrectas. Verifica tu correo y contraseña.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Credenciales incorrectas.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    FcmTokenManager.saveCurrentToken()
                    redireccionarSegunRol()
                } else {
                    binding.btnGoogleLogin.isEnabled = true
                    Toast.makeText(this, "Falló la autenticación con Firebase.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun redireccionarSegunRol() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val rol = doc.getString("role") ?: "SOLICITANTE"
                    val intent = if (rol == "PRESTADOR")
                        Intent(this, ProviderHomeActivity::class.java)
                    else
                        Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent); finish()
                } else {
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogleLogin.isEnabled = true
                    // 🔥 SI ES GOOGLE PERO NO TIENE PERFIL, LO MANDAMOS AL FLUJO DE REGISTRO
                    Toast.makeText(this, "Cuenta no registrada en SIGTI. Por favor, regístrate.", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                }
            }
            .addOnFailureListener {
                binding.btnLogin.isEnabled = true
                binding.btnGoogleLogin.isEnabled = true
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
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null }
        binding.etPassword.doAfterTextChanged { binding.tilPassword.error = null }
    }

    private fun validate(showErrors: Boolean): Boolean {
        var ok = true
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.etPassword.text?.toString().orEmpty()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ok = false; if (showErrors) binding.tilEmail.error = "Correo inválido"
        }
        if (pass.isEmpty()) {
            ok = false; if (showErrors) binding.tilPassword.error = "Contraseña obligatoria"
        }
        return ok
    }
}