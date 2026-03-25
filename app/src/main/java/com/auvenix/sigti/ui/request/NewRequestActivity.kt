package com.auvenix.sigti.ui.request

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.utils.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewRequestActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_request)

        val btnBack = findViewById<ImageView>(R.id.btnBackRequest)
        val btnSubmit = findViewById<MaterialButton>(R.id.btnSubmitRequest)
        val tvRequestFor = findViewById<TextView>(R.id.tvRequestFor)

        val tilServicio = findViewById<TextInputLayout>(R.id.tilServicio)
        val tilFecha = findViewById<TextInputLayout>(R.id.tilFecha)
        val tilCalle = findViewById<TextInputLayout>(R.id.tilCalle)
        val tilNumExt = findViewById<TextInputLayout>(R.id.tilNumExt)
        val tilCp = findViewById<TextInputLayout>(R.id.tilCp)
        val tilDescripcion = findViewById<TextInputLayout>(R.id.tilDescripcion)

        val etServicio = findViewById<TextInputEditText>(R.id.etServicio)
        val etFecha = findViewById<TextInputEditText>(R.id.etFecha)
        val etCalle = findViewById<TextInputEditText>(R.id.etCalle)
        val etNumExt = findViewById<TextInputEditText>(R.id.etNumExt)
        val etCp = findViewById<TextInputEditText>(R.id.etCp)
        val etDescripcion = findViewById<TextInputEditText>(R.id.etDescripcion)

        val providerId = intent.getStringExtra(Constants.EXTRA_WORKER_ID).orEmpty()
        val providerName = intent.getStringExtra(Constants.EXTRA_WORKER_NAME).orEmpty()
        val providerProfession = intent.getStringExtra(Constants.EXTRA_WORKER_PROFESSION).orEmpty()

        tvRequestFor.text = if (providerName.isNotBlank()) {
            "Para: $providerName${if (providerProfession.isNotBlank()) " ($providerProfession)" else ""}"
        } else {
            "Nueva solicitud"
        }

        etFecha.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val value = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                    etFecha.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(value.time))
                    tilFecha.error = null
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnBack.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            var ok = true

            fun requireField(til: TextInputLayout, et: TextInputEditText): String {
                val value = et.text?.toString()?.trim().orEmpty()
                if (value.isBlank()) {
                    til.error = "Campo obligatorio"
                    ok = false
                } else {
                    til.error = null
                }
                return value
            }

            val servicio = requireField(tilServicio, etServicio)
            val fecha = requireField(tilFecha, etFecha)
            val calle = requireField(tilCalle, etCalle)
            val numExt = requireField(tilNumExt, etNumExt)
            val cp = requireField(tilCp, etCp)
            val descripcion = requireField(tilDescripcion, etDescripcion)

            if (!ok) {
                Toast.makeText(this, "Completa los campos marcados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = auth.currentUser?.uid ?: run {
                Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false

            db.collection(Constants.COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener { clientDoc ->
                    val clientName = listOf(
                        clientDoc.getString("nombre").orEmpty(),
                        clientDoc.getString("apPaterno").orEmpty()
                    ).filter { it.isNotBlank() }.joinToString(" ").ifBlank {
                        auth.currentUser?.displayName ?: "Cliente"
                    }

                    val request = hashMapOf(
                        "clientId" to uid,
                        "clientName" to clientName,
                        "providerId" to providerId,
                        "providerName" to providerName,
                        "oficio" to providerProfession,
                        "title" to servicio,
                        "fecha" to fecha,
                        "calle" to calle,
                        "numExt" to numExt,
                        "cp" to cp,
                        "description" to descripcion,
                        "status" to "pending",
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    db.collection(Constants.COLLECTION_REQUESTS)
                        .add(request)
                        .addOnSuccessListener {
                            Toast.makeText(this, "¡Solicitud enviada con éxito!", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            btnSubmit.isEnabled = true
                            Toast.makeText(this, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    btnSubmit.isEnabled = true
                    Toast.makeText(this, "No se pudo obtener tu perfil: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}