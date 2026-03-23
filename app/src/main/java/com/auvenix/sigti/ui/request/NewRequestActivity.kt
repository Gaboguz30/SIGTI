package com.auvenix.sigti.ui.request

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NewRequestActivity : AppCompatActivity() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_request)

        // Vistas
        val btnBack     = findViewById<ImageView>(R.id.btnBackRequest)
        val btnSubmit   = findViewById<MaterialButton>(R.id.btnSubmitRequest)

        val tilServicio = findViewById<TextInputLayout>(R.id.tilServicio)
        val tilFecha    = findViewById<TextInputLayout>(R.id.tilFecha)
        val tilCalle    = findViewById<TextInputLayout>(R.id.tilCalle)
        val tilNumExt   = findViewById<TextInputLayout>(R.id.tilNumExt)
        val tilCp       = findViewById<TextInputLayout>(R.id.tilCp)
        val tilDesc     = findViewById<TextInputLayout>(R.id.tilDescripcion)

        val etServicio  = findViewById<TextInputEditText>(R.id.etServicio)
        val etFecha     = findViewById<TextInputEditText>(R.id.etFecha)
        val etCalle     = findViewById<TextInputEditText>(R.id.etCalle)
        val etNumExt    = findViewById<TextInputEditText>(R.id.etNumExt)
        val etCp        = findViewById<TextInputEditText>(R.id.etCp)
        val etDesc      = findViewById<TextInputEditText>(R.id.etDescripcion)

        btnBack.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            // ── Validar campos obligatorios ─────────────────────
            var ok = true

            fun require(til: TextInputLayout?, et: TextInputEditText?): String {
                val v = et?.text.toString().trim()
                if (v.isEmpty()) {
                    til?.error = "Campo obligatorio"
                    ok = false
                } else {
                    til?.error = null
                }
                return v
            }

            val servicio = require(tilServicio, etServicio)
            val fecha    = require(tilFecha,    etFecha)
            val calle    = require(tilCalle,    etCalle)
            val numExt   = require(tilNumExt,   etNumExt)
            val cp       = require(tilCp,       etCp)
            val desc     = require(tilDesc,     etDesc)

            if (!ok) {
                Toast.makeText(this, "Completa los campos marcados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ── Guardar en Firestore ────────────────────────────
            val uid = auth.currentUser?.uid ?: run {
                Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false

            val solicitud = hashMapOf(
                "clientId"    to uid,
                "clientName"  to (auth.currentUser?.displayName ?: "Cliente"),
                "title"       to servicio,
                "fecha"       to fecha,
                "calle"       to calle,
                "numExt"      to numExt,
                "cp"          to cp,
                "description" to desc,
                "status"      to "pending",
                "timestamp"   to FieldValue.serverTimestamp()
            )

            db.collection("requests").add(solicitud)
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Solicitud enviada con éxito!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnSubmit.isEnabled = true
                    Toast.makeText(this, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}