package com.auvenix.sigti.ui.support

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R

class AyudaDetalleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayuda_detalle)

        val header = findViewById<View>(R.id.header)
        val tituloHeader = header.findViewById<TextView>(R.id.tvHeaderTitle)
        val btnBack = header.findViewById<ImageView>(R.id.btnBackHeader)
        val tvCategoria = findViewById<TextView>(R.id.tvCategoriaSeleccionada)
        val listView = findViewById<ListView>(R.id.lvSubOpciones)

        // 🔹 Configurar Header
        tituloHeader.text = "Soporte"
        btnBack.setOnClickListener { finish() }

        // 🔹 Recibir Categoría
        val categoria = intent.getStringExtra("EXTRA_CATEGORIA") ?: "Ayuda"
        tvCategoria.text = "Problemas comunes: $categoria"

        // 🔹 Cargar Opciones
        val opciones = when (categoria) {
            "Problema con un servicio" -> listOf(
                "El prestador no llegó / El cliente no estaba",
                "El trabajo quedó mal o incompleto",
                "Problema con el cobro / Pago pendiente",
                "Quiero cancelar un servicio activo"
            )
            "Mi catálogo" -> listOf(
                "Error al subir fotos de mis trabajos",
                "No puedo editar mis precios",
                "¿Cómo cambio mi categoría principal?",
                "Mi perfil no se ve públicamente"
            )
            "Verificación de mi perfil" -> listOf(
                "¿Por qué rechazaron mi identificación?",
                "Tiempo de espera para la validación",
                "Error al cargar documentos",
                "Dudas sobre la seguridad de mis datos"
            )
            "Otro problema" -> listOf(
                "Reportar a un usuario (Acoso/Fraude)",
                "La aplicación se cierra sola",
                "Sugerencia para una nueva función",
                "Problemas con las notificaciones"
            )
            else -> listOf("Contactar a un asesor")
        }

        // 🔹 Adaptador para la lista
        val adapter = ArrayAdapter(this, R.layout.item_opcion, R.id.tvItemOpcion, opciones)
        listView.adapter = adapter

        // 🔥 AQUÍ ESTÁ EL PUENTE CORREGIDO 🔥
        // 🔹 Clic en la sub-opción
        listView.setOnItemClickListener { _, _, position, _ ->
            val seleccion = opciones[position]

            // Pasamos a la pantalla de Solución Rápida
            val intent = Intent(this, SolucionRapidaActivity::class.java)
            intent.putExtra("EXTRA_PROBLEMA", seleccion)
            startActivity(intent)
        }
    }
}