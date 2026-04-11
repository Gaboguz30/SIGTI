package com.auvenix.sigti.ui.support

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R

class AyudaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayuda)

        // 🔥 CONFIGURAR EL HEADER
        val header = findViewById<View>(R.id.header)
        val titulo = header.findViewById<TextView>(R.id.tvHeaderTitle)
        val btnBack = header.findViewById<ImageView>(R.id.btnBackHeader)

        titulo.text = "Ayuda"

        btnBack.setOnClickListener {
            finish()
        }

        // 🔹 CONFIGURAR CLICS DE LAS CARDS
        // Ahora cada card manda a la pantalla de detalle con su categoría correspondiente

        findViewById<LinearLayout>(R.id.cardServicio).setOnClickListener {
            irADetalle("Problema con un servicio")
        }

        findViewById<LinearLayout>(R.id.cardCatalogo).setOnClickListener {
            irADetalle("Mi catálogo")
        }

        findViewById<LinearLayout>(R.id.cardVerificacion).setOnClickListener {
            irADetalle("Verificación de mi perfil")
        }

        findViewById<LinearLayout>(R.id.cardOtro).setOnClickListener {
            irADetalle("Otro problema")
        }
    }

    /**
     * Función auxiliar para navegar a la pantalla de detalle
     * @param categoria El nombre de la sección seleccionada
     */
    private fun irADetalle(categoria: String) {
        val intent = Intent(this, AyudaDetalleActivity::class.java)
        intent.putExtra("EXTRA_CATEGORIA", categoria)
        startActivity(intent)
    }
}