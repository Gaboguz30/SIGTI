package com.auvenix.sigti.ui.support

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R

class AyudaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayuda)

        // 🔥 ACCEDER AL HEADER
        val header = findViewById<View>(R.id.header)

        val titulo = header.findViewById<TextView>(R.id.tvHeaderTitle)

        // 🔹 CAMBIAR TITULO
        titulo.text = "Ayuda"

        // 🔹 BOTON BACK

        val btnBack = findViewById<ImageView>(R.id.btnBackHeader)
        btnBack.setOnClickListener {
            finish()
        }

        // 🔹 CARDS
        findViewById<LinearLayout>(R.id.cardServicio).setOnClickListener {
            Toast.makeText(this, "Problema con servicio", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.cardCatalogo).setOnClickListener {
            Toast.makeText(this, "Mi catálogo", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.cardVerificacion).setOnClickListener {
            Toast.makeText(this, "Verificación", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.cardOtro).setOnClickListener {
            Toast.makeText(this, "Otro problema", Toast.LENGTH_SHORT).show()
        }
    }
}