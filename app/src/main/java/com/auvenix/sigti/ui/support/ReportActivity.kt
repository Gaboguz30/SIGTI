package com.auvenix.sigti.ui.support

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var inputIncident: TextInputEditText
    private lateinit var inputRequest: TextInputEditText
    private lateinit var inputDate: TextInputEditText
    private lateinit var inputDescription: TextInputEditText

    private var reportedWorkerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        inputIncident = findViewById(R.id.inputIncidentType)
        inputRequest = findViewById(R.id.inputRequestId)
        inputDate = findViewById(R.id.inputDate)
        inputDescription = findViewById(R.id.inputDescription)

        reportedWorkerId = intent.getStringExtra("workerId")

        val btnBack = findViewById<ImageView>(R.id.btnBackReport)
        val btnSubmit = findViewById<MaterialButton>(R.id.btnSubmitReport)
        val btn911 = findViewById<MaterialButton>(R.id.btnCall911)

        btnBack.setOnClickListener { finish() }

        btn911.setOnClickListener {

            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
            intent.data = android.net.Uri.parse("tel:911")
            startActivity(intent)
        }

        inputDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            val dialog = DatePickerDialog(
                this,
                { _, year, month, day ->

                    val selected = Calendar.getInstance()
                    selected.set(year, month, day)

                    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    inputDate.setText(format.format(selected.time))

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            dialog.show()
        }

        btnSubmit.setOnClickListener {

            sendReport()
        }
    }

    private fun sendReport() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {

            Toast.makeText(this,"Debes iniciar sesión",Toast.LENGTH_LONG).show()
            return
        }

        val incident = inputIncident.text.toString().trim()
        val service = inputRequest.text.toString().trim()
        val date = inputDate.text.toString().trim()
        val description = inputDescription.text.toString().trim()

        if (incident.isEmpty() || description.isEmpty()) {

            Toast.makeText(this,"Completa los campos obligatorios",Toast.LENGTH_LONG).show()
            return
        }

        val report = hashMapOf(

            "reporterUid" to user.uid,
            "reportedWorkerId" to reportedWorkerId,
            "incidentType" to incident,
            "serviceId" to service,
            "date" to date,
            "description" to description,
            "timestamp" to System.currentTimeMillis()

        )

        FirebaseFirestore
            .getInstance()
            .collection("reports")
            .add(report)
            .addOnSuccessListener {

                Toast.makeText(this,"Reporte enviado",Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {

                Toast.makeText(this,"Error al enviar reporte",Toast.LENGTH_LONG).show()
            }
    }
}