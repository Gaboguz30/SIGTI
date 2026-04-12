package com.auvenix.sigti.ui.support

import com.auvenix.sigti.R
import android.os.Bundle
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.net.Uri
import android.content.Intent
import android.app.Activity
import com.auvenix.sigti.ui.profile.WorkerProfileActivity
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ReviewActivity1 : AppCompatActivity() {

    lateinit var ratingBar: RatingBar
    lateinit var inputDescription: TextInputEditText
    lateinit var btnSubmitReview: MaterialButton

    lateinit var db: FirebaseFirestore
    lateinit var providerId: String
    lateinit var inputRequestId: AutoCompleteTextView
    var selectedRequestId: String = "" // 🔥 Cambiado a Request ID
    var selectedFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review1)

        val btnAddEvidence = findViewById<MaterialButton>(R.id.btnAddEvidence)

        btnAddEvidence.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        inputRequestId = findViewById(R.id.inputRequestId)

        // 🔹 HEADER
        val header = findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "Reseñar"

        val back = header.findViewById<ImageView>(R.id.btnBackHeader)
        back.setOnClickListener { finish() }

        // 🔹 FECHA AUTOMÁTICA
        val inputDate = findViewById<TextInputEditText>(R.id.inputDate)
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        inputDate.setText(formato.format(Date()))

        // 🔹 FIREBASE
        db = FirebaseFirestore.getInstance()

        // 🔹 RECIBIR ID DEL PRESTADOR
        providerId = intent.getStringExtra("providerId") ?: ""

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val clientId = currentUser.uid

        // 🔹 UI
        ratingBar = findViewById(R.id.ratingBarReview)
        inputDescription = findViewById(R.id.inputDescription)
        btnSubmitReview = findViewById(R.id.btnSubmitReport)

        // 🔥 CARGAR TRABAJOS CON CANDADOS (Completed y No Reseñados) 🔥
        db.collection("requests")
            .whereEqualTo("clientId", clientId)
            .whereEqualTo("providerId", providerId)
            .whereEqualTo("status", "completed")
            .get()
            .addOnSuccessListener { docs ->
                val requestMap = HashMap<String, String>()
                val requestNames = mutableListOf<String>()

                for (doc in docs) {
                    // Verificamos si ya fue reseñado (si el campo no existe, asume false)
                    val isReviewed = doc.getBoolean("isReviewed") ?: false

                    if (!isReviewed) {
                        val title = doc.getString("title") ?: "Trabajo"
                        val fecha = doc.getString("fecha") ?: ""
                        val displayName = "$title ($fecha)"

                        requestMap[displayName] = doc.id
                        requestNames.add(displayName)
                    }
                }

                if (requestNames.isEmpty()) {
                    Toast.makeText(this, "No tienes trabajos pendientes por reseñar con este prestador", Toast.LENGTH_LONG).show()
                    btnSubmitReview.isEnabled = false // Bloqueamos si no hay qué reseñar
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    requestNames
                )

                inputRequestId.setAdapter(adapter)

                inputRequestId.setOnClickListener { inputRequestId.showDropDown() }
                inputRequestId.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) inputRequestId.showDropDown() }

                inputRequestId.setOnItemClickListener { parent, _, position, _ ->
                    val selectedName = parent.getItemAtPosition(position).toString()
                    selectedRequestId = requestMap[selectedName] ?: ""
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar tu historial de trabajos", Toast.LENGTH_SHORT).show()
            }

        // 🔹 BOTÓN ENVIAR RESEÑA
        btnSubmitReview.setOnClickListener {

            val rating = ratingBar.rating
            val comment = inputDescription.text.toString()

            if (rating == 0f) {
                Toast.makeText(this, "Agrega una calificación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedRequestId.isEmpty()) {
                Toast.makeText(this, "Selecciona el trabajo que quieres reseñar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarReseña(clientId)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            Toast.makeText(this, "Archivo seleccionado", Toast.LENGTH_SHORT).show()
        }
    }

    fun guardarReseña(clientId: String) {
        btnSubmitReview.isEnabled = false
        btnSubmitReview.text = "Enviando..."

        db.collection("users").document(clientId)
            .get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: ""
                val apP = doc.getString("apPaterno") ?: ""
                val userName = "$nombre $apP".trim()

                if (selectedFileUri != null) {
                    subirImagenYGuardar(clientId, userName)
                } else {
                    enviarFirestore(clientId, userName, "")
                }
            }
            .addOnFailureListener {
                btnSubmitReview.isEnabled = true
                btnSubmitReview.text = "Enviar reseña"
                Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirImagenYGuardar(clientId: String, userName: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "reviews_photos/$providerId/${UUID.randomUUID()}"
        val fileRef = storageRef.child(fileName)

        fileRef.putFile(selectedFileUri!!)
            .continueWithTask { task ->
                if (!task.isSuccessful) { task.exception?.let { throw it } }
                fileRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result.toString()
                    enviarFirestore(clientId, userName, downloadUri)
                } else {
                    btnSubmitReview.isEnabled = true
                    btnSubmitReview.text = "Enviar reseña"
                    Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun enviarFirestore(clientId: String, userName: String, imageUrl: String) {
        val review = hashMapOf(
            "userName" to userName,
            "technician_uid" to providerId,
            "user_uid" to clientId,
            "rating" to ratingBar.rating.toInt(),
            "comment" to inputDescription.text.toString(),
            "created_at" to com.google.firebase.Timestamp.now(),
            "imageUrl" to imageUrl
        )

        db.collection("reviews")
            .add(review)
            .addOnSuccessListener {

                // 🔥 MARCAMOS EL TRABAJO COMO "YA RESEÑADO"
                db.collection("requests").document(selectedRequestId)
                    .update("isReviewed", true)
                    .addOnSuccessListener {
                        Toast.makeText(this, "¡Gracias por tu reseña!", Toast.LENGTH_SHORT).show()

                        // 🔥 MANDAMOS AL CLIENTE AL PERFIL DEL TRABAJADOR
                        val intent = Intent(this, WorkerProfileActivity::class.java)
                        intent.putExtra("EXTRA_WORKER_UID", providerId)
                        startActivity(intent)
                        finish() // Cerramos esta pantalla para que no pueda volver atrás
                    }
            }
            .addOnFailureListener {
                btnSubmitReview.isEnabled = true
                btnSubmitReview.text = "Enviar reseña"
                Toast.makeText(this, "Error al guardar la reseña", Toast.LENGTH_SHORT).show()
            }
    }
}