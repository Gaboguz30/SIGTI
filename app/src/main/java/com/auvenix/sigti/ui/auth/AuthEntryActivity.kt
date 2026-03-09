package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityAuthEntryBinding
import com.auvenix.sigti.ui.role.RoleActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.auvenix.sigti.R // Asegúrate de tener tus strings

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

        // 1. Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // ID de Firebase
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Botón "Aceptar y continuar" (Registro manual)
        binding.btnAcceptContinue.setOnClickListener {
            val intent = Intent(this, RoleActivity::class.java)
            intent.putExtra("IS_GOOGLE", false) // Flujo normal
            startActivity(intent)
        }

        // 2. BOTÓN GOOGLE
        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    // 3. Recibir el resultado de Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                Log.w("GOOGLE", "Google sign in failed", e)
            }
        }
    }

    // 4. Autenticar en Firebase y mandar a RoleActivity
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser

                // MANDAMOS A ROLE ACTIVITY CON LA BANDERA "IS_GOOGLE"
                val intent = Intent(this, RoleActivity::class.java).apply {
                    putExtra("IS_GOOGLE", true)
                    putExtra("EXTRA_NOMBRE_COMPLETO", user?.displayName)
                    putExtra("EXTRA_EMAIL", user?.email)
                    putExtra("EXTRA_UID", user?.uid)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al autenticar con Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }
}