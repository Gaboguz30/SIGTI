package com.auvenix.sigti.ui.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import com.auvenix.sigti.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class AddReviewBottomSheet(
    private val workerId: String // 🔥 AQUÍ RECIBIMOS EL ID DEL PRESTADOR
) : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
        val btnSubmit = view.findViewById<MaterialButton>(R.id.btnSubmitReview)
        val btnAddPhoto = view.findViewById<MaterialButton>(R.id.btnAddPhoto)

        // TODO: Lógica para abrir la galería y seleccionar foto (Fase de Storage)
        btnAddPhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir galería próximamente...", Toast.LENGTH_SHORT).show()
        }

        btnSubmit.setOnClickListener {
            val rating = rbStars.rating
            val comment = etComment.text.toString().trim()
            val userId = auth.currentUser?.uid

            if (rating == 0f) {
                Toast.makeText(requireContext(), "Por favor, dale una calificación en estrellas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(requireContext(), "Debes iniciar sesión para calificar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Deshabilitamos el botón para que no mande 20 reseñas si le pica mucho
            btnSubmit.isEnabled = false
            btnSubmit.text = "Publicando..."

            // 🔥 CREAMOS EL REGISTRO ATADO AL PRESTADOR
            val reviewData = hashMapOf(
                "technician_uid" to workerId, // Se lo asignamos al registro de este prestador
                "user_uid" to userId,
                "rating" to rating,
                "comment" to comment,
                "imageUrl" to "", // Aquí irá la URL de la foto después
                "created_at" to FieldValue.serverTimestamp() // Fecha exacta de Firebase
            )

            db.collection("reviews").add(reviewData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "¡Reseña publicada con éxito!", Toast.LENGTH_SHORT).show()
                    dismiss() // Cerramos el BottomSheet
                }
                .addOnFailureListener {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Publicar Reseña"
                    Toast.makeText(requireContext(), "Error al publicar. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}