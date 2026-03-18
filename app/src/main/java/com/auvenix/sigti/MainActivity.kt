package com.auvenix.sigti

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityAuthEntryBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // 🔹 Declarar binding
    private lateinit var binding: ActivityAuthEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Inicializar ViewBinding
        binding = ActivityAuthEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Conexión a Firebase
        val db = FirebaseFirestore.getInstance()

        val prueba = hashMapOf(
            "nombre" to "Vianca",
            "correo" to "prueba@gmail.com"
        )

        db.collection("users")
            .add(prueba)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Datos guardados")
            }
            .addOnFailureListener {
                Log.d("FIREBASE", "Error al guardar")
            }
    }
}