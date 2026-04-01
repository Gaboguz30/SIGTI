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
import com.auvenix.sigti.ui.register.RegisterGeneralActivity
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

    private val rcSignIn = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnAcceptContinue.setOnClickListener {
            startActivity(Intent(this, RoleActivity::class.java).apply {
                putExtra(RegisterGeneralActivity.EXTRA_IS_GOOGLE, false)
            })
        }

        binding.btnGoogle.setOnClickListener {
            @Suppress("DEPRECATION")
            startActivityForResult(googleSignInClient.signInIntent, rcSignIn)
        }

        binding.btnFacebook.setOnClickListener {
            Toast.makeText(
                this,
                "Inicio con Facebook próximamente disponible",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        auth.currentUser?.let { redireccionarSegunRol(it.uid) }
    }

    @Deprecated("Use Activity Result API in future versions")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != rcSignIn) return

        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java) ?: return

            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                Toast.makeText(this, "No se pudo obtener el token de Google", Toast.LENGTH_SHORT).show()
                return
            }

            firebaseAuthWithGoogle(idToken)
        } catch (e: ApiException) {
            Log.w("GOOGLE_AUTH", "Google sign-in falló: ${e.statusCode}", e)
            Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Autenticación fallida con Firebase", Toast.LENGTH_SHORT).show()
                return@addOnCompleteListener
            }

            val user = auth.currentUser ?: run {
                Toast.makeText(this, "No se pudo obtener el usuario autenticado", Toast.LENGTH_SHORT).show()
                return@addOnCompleteListener
            }

            val uid = user.uid

            FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        FcmTokenManager.saveCurrentToken()
                        redireccionarSegunRol(uid)
                    } else {
                        startActivity(Intent(this, RoleActivity::class.java).apply {
                            putExtra(RegisterGeneralActivity.EXTRA_IS_GOOGLE, true)
                            putExtra(RegisterGeneralActivity.EXTRA_GOOGLE_UID, uid)
                            putExtra(RegisterGeneralActivity.EXTRA_GOOGLE_NAME, user.displayName)
                            putExtra(RegisterGeneralActivity.EXTRA_GOOGLE_EMAIL, user.email)
                            putExtra(RegisterGeneralActivity.EXTRA_GOOGLE_PHOTO_URL, user.photoUrl?.toString())
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
        }
    }

    private fun redireccionarSegunRol(uid: String) {
        FirebaseFirestore.getInstance()
            .collection(Constants.COLLECTION_USERS)
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val rol = doc.getString(Constants.FIELD_ROL)
                    ?: doc.getString(Constants.FIELD_ROLE)

                val nextIntent = if (rol == Constants.ROLE_PROVIDER) {
                    Intent(this, ProviderHomeActivity::class.java)
                } else {
                    Intent(this, HomeActivity::class.java)
                }

                nextIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(nextIntent)
                finish()
            }
    }
}