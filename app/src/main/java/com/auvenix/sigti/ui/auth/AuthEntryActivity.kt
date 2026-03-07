package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityAuthEntryBinding
import com.auvenix.sigti.ui.role.RoleActivity
import com.auvenix.sigti.ui.auth.LoginActivity
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore


class AuthEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = FirebaseFirestore.getInstance()

        val prueba = hashMapOf(
            "nombre" to "Vianca",
            "correo" to "prueba@gmail.com"
        )

        db.collection("usuarios")
            .add(prueba)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Datos guardados")
            }
            .addOnFailureListener {
                Log.d("FIREBASE", "Error")
            }

        // Botón principal (ej: "Aceptar y continuar")
        binding.btnAcceptContinue.setOnClickListener {
            startActivity(Intent(this, RoleActivity::class.java))
        }

        // Botones de social login (stubs por ahora)
        binding.btnGoogle.setOnClickListener {
            // TODO: Google Sign-In
        }

        binding.btnFacebook.setOnClickListener {
            // TODO: Facebook Login
        }

        // Link "Iniciar sesión"
        binding.tvLoginLink.setOnClickListener {
            // TODO: ir a LoginActivity si lo agregas
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}


