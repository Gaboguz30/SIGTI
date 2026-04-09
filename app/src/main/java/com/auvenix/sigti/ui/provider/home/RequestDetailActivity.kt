package com.auvenix.sigti.ui.provider.home

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.chat.ChatDetailActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import android.widget.TextView
import android.widget.ImageView

class RequestDetailActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var requestId = ""
    private var clientId = ""
    private var clientName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_detail)
        val headerTitle = findViewById<TextView>(R.id.tvHeaderTitle)
        val btnBack = findViewById<ImageView>(R.id.btnBackHeader)

// cambiar título
        headerTitle.text = "Detalles de Oferta"

// botón regresar
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        requestId = intent.getStringExtra("EXTRA_REQUEST_ID") ?: ""
        if (requestId.isEmpty()) { finish(); return }

        cargarDatosDeLaOferta()

        findViewById<MaterialButton>(R.id.btnRejectJob).setOnClickListener { rechazarOferta() }
        findViewById<MaterialButton>(R.id.btnNegotiateJob).setOnClickListener { negociarEnChat() }
        findViewById<MaterialButton>(R.id.btnAcceptJob).setOnClickListener { aceptarYFijarPrecio() }
    }

    private fun cargarDatosDeLaOferta() {
        db.collection("requests").document(requestId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    clientId = doc.getString("clientId") ?: ""
                    clientName = doc.getString("clientName") ?: "Cliente"

                    findViewById<TextView>(R.id.tvDetailTitle).text = doc.getString("title") ?: "Servicio"

                    val offer = doc.getDouble("priceOffer") ?: 0.0

                    val fecha = doc.getString("fecha") ?: ""

                    findViewById<TextView>(R.id.tvDetailClientInfo).text = clientName
                    findViewById<TextView>(R.id.tvDetailDate).text = "Se requiere el servicio el dia: $fecha"

                    val calle = doc.getString("calle") ?: ""
                    val num = doc.getString("numExt") ?: ""
                    val cp = doc.getString("cp") ?: ""
                    findViewById<TextView>(R.id.tvStreet).text = "$calle #$num"
                    findViewById<TextView>(R.id.tvCity).text = "Tehuacán, Puebla"
                    findViewById<TextView>(R.id.tvPostalCode).text = "CP: $cp"

                    findViewById<TextView>(R.id.tvDetailDesc).text = doc.getString("description")
                }
            }
    }

    private fun negociarEnChat() {
        val intent = Intent(this, ChatDetailActivity::class.java)
        intent.putExtra("serviceId", clientId)
        intent.putExtra("contactName", clientName)
        startActivity(intent)
    }

    private fun rechazarOferta() {
        AlertDialog.Builder(this)
            .setTitle("Rechazar Oferta")
            .setMessage("¿Estás seguro de que quieres rechazar este trabajo? El cliente será notificado.")
            .setPositiveButton("Sí, rechazar") { _, _ ->
                // 🔥 Cambiamos estado a RECHAZADO
                db.collection("requests").document(requestId).update("status", "rejected")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Oferta rechazada", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun aceptarYFijarPrecio() {
        // 🔥 El popup mágico donde el prestador pone su precio final
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Ej: 450.00"

        AlertDialog.Builder(this)
            .setTitle("Fijar Precio Final")
            .setMessage("Acuerda el precio final. El cliente tendrá que confirmarlo para que el trabajo sea tuyo oficialmente.")
            .setView(input)
            .setPositiveButton("Enviar a Cliente") { _, _ ->
                val finalPrice = input.text.toString().toDoubleOrNull()

                if (finalPrice != null && finalPrice > 0) {
                    val updates = mapOf(
                        "status" to "pending_client_confirmation",
                        "finalPrice" to finalPrice
                    )
                    db.collection("requests").document(requestId).update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "¡Precio enviado! Esperando confirmación del cliente.", Toast.LENGTH_LONG).show()
                            finish()
                        }
                } else {
                    Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}