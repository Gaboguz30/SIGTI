package com.auvenix.sigti.ui.request

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton

class NewRequestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_request)

        // 1. Botón para regresar al Perfil
        val btnBack = findViewById<ImageView>(R.id.btnBackRequest)
        btnBack.setOnClickListener {
            finish() // Cierra esta pantalla
        }

        // 2. Botón para Enviar Solicitud
        val btnSubmit = findViewById<MaterialButton>(R.id.btnSubmitRequest)
        btnSubmit.setOnClickListener {
            // Aquí en el futuro guardarás los datos en Firestore
            Toast.makeText(this, "¡Solicitud enviada a Vianca exitosamente!", Toast.LENGTH_LONG).show()

            // Regresamos a la pantalla anterior después de enviar
            finish()
        }
    }
}