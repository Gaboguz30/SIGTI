package com.auvenix.sigti.ui.support

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton

class SolucionRapidaActivity : AppCompatActivity() {

    // 🔴 PON TUS DATOS REALES DE CONTACTO AQUÍ 🔴
    private val telefonoSoporte = "2381234567" // Lada Tehuacán de ejemplo
    private val correoSoporte = "soporte@sigti.com.mx"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solucion_rapida)

        // Configurar Header
        val header = findViewById<View>(R.id.header)
        header.findViewById<TextView>(R.id.tvHeaderTitle).text = "Solución Rápida"
        header.findViewById<ImageView>(R.id.btnBackHeader).setOnClickListener { finish() }

        val tvTitulo = findViewById<TextView>(R.id.tvTituloProblema)
        val tvTexto = findViewById<TextView>(R.id.tvTextoSolucion)

        // 1. Recibir el problema exacto
        val problemaSeleccionado = intent.getStringExtra("EXTRA_PROBLEMA") ?: "Problema no especificado"
        tvTitulo.text = problemaSeleccionado

        // 2. Cargar el texto de ayuda según el problema
        tvTexto.text = obtenerSolucion(problemaSeleccionado)

        // 3. Configurar Botones de Contacto
        findViewById<MaterialButton>(R.id.btnLlamar).setOnClickListener {
            val intentLlamada = Intent(Intent.ACTION_DIAL)
            intentLlamada.data = Uri.parse("tel:$telefonoSoporte")
            startActivity(intentLlamada)
        }

        findViewById<MaterialButton>(R.id.btnWhatsApp).setOnClickListener {
            // Mandamos a la API de WhatsApp con el número a 10 dígitos + lada de país (52 México)
            val urlWa = "https://api.whatsapp.com/send?phone=52$telefonoSoporte&text=Hola,%20necesito%20ayuda%20con:%20$problemaSeleccionado"
            val intentWa = Intent(Intent.ACTION_VIEW, Uri.parse(urlWa))
            try {
                startActivity(intentWa)
            } catch (e: Exception) {
                Toast.makeText(this, "No tienes WhatsApp instalado", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnCorreo).setOnClickListener {
            val intentCorreo = Intent(Intent.ACTION_SENDTO)
            intentCorreo.data = Uri.parse("mailto:$correoSoporte")
            intentCorreo.putExtra(Intent.EXTRA_SUBJECT, "Soporte SIGTI: $problemaSeleccionado")
            try {
                startActivity(intentCorreo)
            } catch (e: Exception) {
                Toast.makeText(this, "No hay aplicación de correo instalada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerSolucion(problema: String): String {
        return when (problema) {
            "El prestador no llegó / El cliente no estaba" ->
                "Intenta contactar a la otra parte mediante el chat interno de la aplicación. Es posible que esté retrasado por el tráfico. Si han pasado más de 20 minutos de tolerancia, puedes cancelar el servicio desde tu historial sin recibir ninguna penalización."
            "El trabajo quedó mal o incompleto" ->
                "Por favor, toma fotografías claras del trabajo realizado. Tienes un periodo de 48 horas para hacer el reporte. Conservaremos el pago en garantía hasta que un agente de SIGTI evalúe la situación y nos comuniquemos con ambas partes."
            "Problema con el cobro / Pago pendiente" ->
                "Asegúrate de tener conexión a internet. A veces los pagos con tarjeta tardan unos minutos en reflejarse. Si pagaste en efectivo y la aplicación sigue marcando deuda, por favor envíanos un mensaje con tu recibo."
            "Quiero cancelar un servicio activo" ->
                "Ve a tu sección de 'Trabajos', selecciona el servicio activo y presiona 'Cancelar'. Toma en cuenta que si el prestador ya está en camino, podría aplicarse un cargo mínimo por cancelación según nuestros términos y condiciones."
            "Error al subir fotos de mis trabajos" ->
                "Verifica que la aplicación de SIGTI tenga permisos de almacenamiento en tu teléfono (Ajustes > Aplicaciones > SIGTI > Permisos). Además, asegúrate de que tus fotos no pesen más de 5MB y tengan un formato estándar (JPG o PNG)."
            "¿Por qué rechazaron mi identificación?" ->
                "Las causas más comunes de rechazo son: la foto está borrosa, hay reflejos del flash que tapan tus datos, el documento está recortado o está vencido. Intenta tomar la foto nuevamente en un lugar con buena iluminación natural."
            else ->
                "Lamentamos el inconveniente. Te recomendamos revisar tu conexión a internet o reiniciar la aplicación. Si el problema persiste, por favor contáctanos directamente a través de los canales de abajo para que un asesor revise tu cuenta."
        }
    }
}