package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityAuthEntryBinding
import com.auvenix.sigti.notifications.FcmTokenManager
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.role.RoleActivity
import com.auvenix.sigti.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class AuthEntryActivity : AppCompatActivity() {

    private lateinit var binding          : ActivityAuthEntryBinding
    private lateinit var auth             : FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 🔥 CORRECCIÓN: Registro manual (Va directo a elegir rol, sin mochila falsa)
        binding.btnAcceptContinue.setOnClickListener {
            val intent = Intent(this, RoleActivity::class.java)
            startActivity(intent)
        }

        // ── Google Sign-In ─────────────────────────────────────
        binding.btnGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            }
        }

        // ── Facebook — próximamente ────────────────────────────
        binding.btnFacebook.setOnClickListener {
            Toast.makeText(this, "Inicio con Facebook próximamente disponible", Toast.LENGTH_SHORT).show()
        }

        // ── Link "¿Ya tienes cuenta? Iniciar sesión" ───────────
        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    // ── Auto-login si ya hay sesión activa ─────────────────────
    override fun onStart() {
        super.onStart()
        auth.currentUser?.let { redireccionarSegunRol(it.uid) }
    }

    // ── Resultado del flujo Google ─────────────────────────────
    @Deprecated("Use Activity Result API in future versions")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GOOGLE_AUTH", "Google sign-in falló: ${e.statusCode}")
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Autenticación Firebase + Google ───────────────────────
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser ?: return@addOnCompleteListener
                val uid  = user.uid
                FirebaseFirestore.getInstance()
                    .collection(Constants.COLLECTION_USERS).document(uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Usuario viejo, lo mandamos al Home
                            FcmTokenManager.saveCurrentToken()
                            redireccionarSegunRol(uid)
                        } else {
                            // Usuario nuevo por Google: lo mandamos a completar perfil
                            val intentGoogle = Intent(this, GoogleCompleteProfileActivity::class.java).apply {
                                putExtra("EXTRA_NOMBRE_COMPLETO", user.displayName)
                                putExtra("EXTRA_EMAIL", user.email)
                                putExtra("EXTRA_UID", uid)
                                putExtra("EXTRA_ROL", "SOLICITANTE")
                            }
                            startActivity(intentGoogle)
                            finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al verificar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Autenticación fallida con Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Redirección según rol en Firestore ────────────────────
    private fun redireccionarSegunRol(uid: String) {
        FirebaseFirestore.getInstance()
            .collection(Constants.COLLECTION_USERS).document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener
                val rol    = doc.getString(Constants.FIELD_ROL) ?: doc.getString(Constants.FIELD_ROLE)
                val intent = if (rol == Constants.ROLE_PROVIDER)
                    Intent(this, ProviderHomeActivity::class.java)
                else
                    Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
    }
}