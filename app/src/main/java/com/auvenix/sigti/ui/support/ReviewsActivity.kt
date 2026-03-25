package com.auvenix.sigti.ui.support

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val reviews = mutableListOf<Review>()
    private lateinit var adapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        findViewById<ImageView>(R.id.btnBackReviews).setOnClickListener { finish() }

        val rvReviews = findViewById<RecyclerView>(R.id.rvReviews)
        rvReviews.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(reviews)
        rvReviews.adapter = adapter

        loadReviews()
    }

    private fun loadReviews() {
        val workerId = intent.getStringExtra(Constants.EXTRA_WORKER_ID)
            ?: intent.getStringExtra("workerId")

        if (workerId.isNullOrBlank()) {
            Toast.makeText(this, "No se recibió el prestador a consultar", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection(Constants.COLLECTION_USERS)
            .document(workerId)
            .collection(Constants.COLLECTION_REVIEWS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                reviews.clear()

                var totalRating = 0.0
                snapshot.documents.forEach { doc ->
                    val rating = doc.getDouble("rating") ?: 0.0
                    totalRating += rating
                    reviews.add(
                        Review(
                            name = doc.getString("reviewerName") ?: "Usuario",
                            date = formatDate(doc.getLong("timestamp")),
                            text = doc.getString("comment") ?: "Sin comentario",
                            rating = rating
                        )
                    )
                }

                adapter.notifyDataSetChanged()
                updateHeader(snapshot.size(), totalRating)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar reseñas: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateHeader(totalReviews: Int, totalRating: Double) {
        val average = if (totalReviews > 0) totalRating / totalReviews else 0.0
        findViewById<TextView>(R.id.tvBigRating).text = if (totalReviews > 0) {
            String.format("%.1f", average)
        } else {
            "0.0"
        }
        findViewById<TextView>(R.id.tvTotalReviews).text = if (totalReviews > 0) {
            "$totalReviews reseñas\n${String.format(Locale.getDefault(), "%.0f", (average / 5.0) * 100)}% recomendado"
        } else {
            "0 reseñas\nSin calificaciones"
        }
    }

    private fun formatDate(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0L) return "Sin fecha"
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}