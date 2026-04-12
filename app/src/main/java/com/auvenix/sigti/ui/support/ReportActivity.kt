package com.auvenix.sigti.ui.support

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import android.net.Uri
import android.content.Intent
import android.app.Activity
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.firebase.storage.FirebaseStorage

class ReportActivity : AppCompatActivity() {

    private lateinit var inputIncident: TextInputEditText
    private lateinit var inputRequest: AutoCompleteTextView // 🔥 AHORA ES DROPDOWN
    private lateinit var inputDateReport: TextInputEditText
    private lateinit var inputDateIncident: TextInputEditText
    private lateinit var inputDescription: TextInputEditText
    private lateinit var btnSubmit: MaterialButton

    private var reportedWorkerId: String = ""
    private var selectedServiceId: String = ""
    private var selectedFileUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        reportedWorkerId = intent.getStringExtra("workerId") ?: ""

        initViews()
        configurarFechas()
        cargarTrabajosEnProgreso()

        val btnAddEvidence = findViewById<MaterialButton>(R.id.btnAddEvidence)
        btnAddEvidence.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        btnSubmit.setOnClickListener {
            sendReport()
        }
    }

    private fun initViews() {
        inputIncident = findViewById(R.id.inputIncidentType)
        inputRequest = findViewById(R.id.inputRequestId)
        inputDateReport = findViewById(R.id.inputDateReport)
        inputDateIncident = findViewById(R.id.inputDateIncident)
        inputDescription = findViewById(R.id.inputDescription)
        btnSubmit = findViewById(R.id.btnSubmitReport)

        val header = findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "Reporte"

        val back = findViewById<ImageView>(R.id.btnBackHeader)
        back.setOnClickListener { finish() }
    }

    private fun configurarFechas() {
        // 🔹 FECHA DE REPORTE (HOY - NO MODIFICABLE)
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        inputDateReport.setText(formato.format(Date()))

        // 🔹 FECHA DE INCIDENTE (CALENDARIO)
        inputDateIncident.setOnClickListener {
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                val fechaElegida = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year)
                inputDateIncident.setText(fechaElegida)
            }, anio, mes, dia)

            dpd.datePicker.maxDate = Date().time // No dejar elegir fechas en el futuro
            dpd.show()
        }
    }

    // 🔥 FILTRAR SOLO TRABAJOS EN PROGRESO ("in_progress") 🔥
    private fun cargarTrabajosEnProgreso() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        db.collection("requests")
            .whereEqualTo("clientId", user.uid)
            .whereEqualTo("providerId", reportedWorkerId)
            .whereEqualTo("status", "in_progress") // EL CANDADO
            .get()
            .addOnSuccessListener { docs ->
                val requestMap = HashMap<String, String>()
                val requestNames = mutableListOf<String>()

                for (doc in docs) {
                    val title = doc.getString("title") ?: "Trabajo"
                    val fecha = doc.getString("fecha") ?: ""
                    val displayName = "$title ($fecha)"

                    requestMap[displayName] = doc.id
                    requestNames.add(displayName)
                }

                if (requestNames.isEmpty()) {
                    Toast.makeText(this, "No tienes trabajos en curso para reportar", Toast.LENGTH_LONG).show()
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    requestNames
                )

                inputRequest.setAdapter(adapter)

                inputRequest.setOnClickListener { inputRequest.showDropDown() }
                inputRequest.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) inputRequest.showDropDown() }

                inputRequest.setOnItemClickListener { parent, _, position, _ ->
                    val selectedName = parent.getItemAtPosition(position).toString()
                    selectedServiceId = requestMap[selectedName] ?: ""
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar trabajos", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            Toast.makeText(this, "Archivo seleccionado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendReport() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_LONG).show()
            return
        }

        val incident = inputIncident.text.toString().trim()
        val dateReport = inputDateReport.text.toString().trim()
        val dateIncident = inputDateIncident.text.toString().trim()
        val description = inputDescription.text.toString().trim()

        if (incident.isEmpty() || description.isEmpty() || selectedServiceId.isEmpty() || dateIncident.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_LONG).show()
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "Enviando..."

        if (selectedFileUri != null) {
            subirImagenYGuardar(user.uid, incident, dateReport, dateIncident, description)
        } else {
            guardarEnFirestore(user.uid, incident, dateReport, dateIncident, description, "")
        }
    }

    private fun subirImagenYGuardar(uid: String, incident: String, dateReport: String, dateIncident: String, desc: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "reports_evidence/${UUID.randomUUID()}"
        val fileRef = storageRef.child(fileName)

        fileRef.putFile(selectedFileUri!!)
            .continueWithTask { task ->
                if (!task.isSuccessful) { task.exception?.let { throw it } }
                fileRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result.toString()
                    guardarEnFirestore(uid, incident, dateReport, dateIncident, desc, downloadUri)
                } else {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Enviar reporte"
                    Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun guardarEnFirestore(uid: String, incident: String, dateReport: String, dateIncident: String, desc: String, imageUrl: String) {
        val report = hashMapOf(
            "reporterUid" to uid,
            "reportedWorkerId" to reportedWorkerId,
            "incidentType" to incident,
            "serviceId" to selectedServiceId, // 🔥 Guarda el ID real del trabajo
            "dateReport" to dateReport, // Fecha en la que se levantó el reporte
            "dateIncident" to dateIncident, // Fecha en la que sucedió el problema
            "description" to desc,
            "evidenceUrl" to imageUrl, // Guarda la URL de la imagen si hay
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("reports")
            .add(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Enviar reporte"
                Toast.makeText(this, "Error al enviar reporte", Toast.LENGTH_LONG).show()
            }
    }
}