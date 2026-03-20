package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityAuthEntryBinding
import notifications.FcmTokenManager
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

    private lateinit var binding: ActivityAuthEntryBinding
    private lateinit var auth: FirebaseAuth
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

        // Botón "Aceptar y continuar" — registro manual
        binding.btnAcceptContinue.setOnClickListener {
            startActivity(Intent(this, RoleActivity::class.java).apply {
                putExtra(Constants.EXTRA_IS_GOOGLE, false)
            })
        }

        // Botón Google
        binding.btnGoogle.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }

        // Link a Login
        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    // ======================================================
    //  AUTO-LOGIN: Si ya hay sesión activa, redirige al home
    // ======================================================
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            redireccionarSegunRol(currentUser.uid)
        }
    }

    // ======================================================
    //  RESULTADO DEL FLUJO GOOGLE
    // ======================================================
    @Deprecated("Use Activity Result API in future versions")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GOOGLE_AUTH", "Google sign-in falló: ${e.statusCode}")
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ======================================================
    //  AUTENTICACIÓN CON FIREBASE + GOOGLE
    // ======================================================
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser ?: return@addOnCompleteListener
                val uid  = user.uid

                FirebaseFirestore.getInstance()
                    .collection(Constants.COLLECTION_USERS)
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Usuario ya registrado — guardar token FCM y redirigir
                            FcmTokenManager.saveCurrentToken()
                            redireccionarSegunRol(uid)
                        } else {
                            // Usuario nuevo — completar perfil
                            startActivity(Intent(this, RoleActivity::class.java).apply {
                                putExtra(Constants.EXTRA_IS_GOOGLE,    true)
                                putExtra(Constants.EXTRA_NOMBRE,       user.displayName)
                                putExtra(Constants.EXTRA_EMAIL_GOOGLE, user.email)
                                putExtra(Constants.EXTRA_UID,          uid)
                            })
                            finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al verificar usuario: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "Autenticación fallida con Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ======================================================
    //  REDIRECCIÓN SEGÚN ROL (igual que LoginActivity)
    // ======================================================
    private fun redireccionarSegunRol(uid: String) {
        FirebaseFirestore.getInstance()
            .collection(Constants.COLLECTION_USERS)
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val rol = doc.getString(Constants.FIELD_ROL)
                    ?: doc.getString(Constants.FIELD_ROLE)

                val intent = if (rol == Constants.ROLE_PROVIDER) {
                    Intent(this, ProviderHomeActivity::class.java)
                } else {
                    Intent(this, HomeActivity::class.java)
                }

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
    }
}