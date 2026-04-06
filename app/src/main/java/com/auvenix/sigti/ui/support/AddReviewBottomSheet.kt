package com.auvenix.sigti.ui.support

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.auvenix.sigti.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage

class AddReviewBottomSheet(
    private val workerId: String
) : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var imageUri: Uri? = null
    private lateinit var btnAddPhoto: MaterialButton
    private lateinit var btnSubmit: MaterialButton

    // 🔥 SELECTOR DE FOTOS MODERNO (Photo Picker)
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri
            btnAddPhoto.text = "Foto seleccionada ✅"
            btnAddPhoto.setIconResource(android.R.drawable.checkbox_on_background)
        } else {
            imageUri = null
            btnAddPhoto.text = "Añadir Foto (Opcional)"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rbStars = view.findViewById<RatingBar>(R.id.rbNewReview)
        val etComment = view.findViewById<TextInputEditText>(R.id.etReviewComment)
        btnSubmit = view.findViewById(R.id.btnSubmitReview)
        btnAddPhoto = view.findViewById(R.id.btnAddPhoto)

        // Botón para abrir galería
        btnAddPhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Botón principal
        btnSubmit.setOnClickListener {
            val rating = rbStars.rating
            val comment = etComment.text.toString().trim()
            val userId = auth.currentUser?.uid

            if (rating == 0f) {
                Toast.makeText(requireContext(), "¡Dale unas estrellas, carnal!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(requireContext(), "Inicia sesión para comentar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Publicando..."

            // 🚀 DECISIÓN: ¿Hay foto o no?
            if (imageUri != null) {
                subirFotoYPublicar(rating, comment, userId)
            } else {
                guardarReseñaEnFirestore(rating, comment, userId, "")
            }
        }
    }

    // ☁️ SUBIDA A FIREBASE STORAGE
    private fun subirFotoYPublicar(rating: Float, comment: String, userId: String) {
        val fileName = "review_${System.currentTimeMillis()}.jpg"
        val fileRef = storage.reference.child("reviews_photos/$workerId/$fileName")

        fileRef.putFile(imageUri!!)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    guardarReseñaEnFirestore(rating, comment, userId, uri.toString())
                }
            }
            .addOnFailureListener {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Publicar Reseña"
                Toast.makeText(requireContext(), "Fallo al subir foto", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔥 GUARDADO EN FIRESTORE
    private fun guardarReseñaEnFirestore(rating: Float, comment: String, userId: String, photoUrl: String) {
        val reviewData = hashMapOf(
            "technician_uid" to workerId,
            "user_uid" to userId,
            "rating" to rating,
            "comment" to comment,
            "imageUrl" to photoUrl,
            "created_at" to FieldValue.serverTimestamp()
        )

        db.collection("reviews").add(reviewData)
            .addOnSuccessListener {
                actualizarPromedioPrestador(workerId)
                Toast.makeText(requireContext(), "¡Reseña lista! ⭐", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Publicar Reseña"
            }
    }

    // 🧠 CÁLCULO DEL PROMEDIO
    private fun actualizarPromedioPrestador(workerId: String) {
        db.collection("reviews")
            .whereEqualTo("technician_uid", workerId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    var sumaTotal = 0f
                    val conteo = documents.size().toFloat()

                    for (doc in documents) {
                        val r = doc.getDouble("rating")?.toFloat() ?: 0f
                        sumaTotal += r
                    }

                    val promedio = sumaTotal / conteo
                    val promedioFinal = String.format("%.1f", promedio)

                    db.collection("users").document(workerId)
                        .update(
                            "rating", promedioFinal,
                            "total_reviews", documents.size()
                        )
                }
            }
    }
}
