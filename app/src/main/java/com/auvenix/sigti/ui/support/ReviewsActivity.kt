package com.auvenix.sigti.ui.support

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

class ReviewsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        findViewById<ImageView>(R.id.btnBackReviews).setOnClickListener { finish() }

        val rvReviews = findViewById<RecyclerView>(R.id.rvReviews)
        rvReviews.layoutManager = LinearLayoutManager(this)

        val dummyReviews = listOf(
            Review("Ana Solís", "12 Mayo 2023", "Muy puntual y dejó todo funcionando. Recomendada."),
            Review("Luis Contreras", "10 Enero 2024", "Buen trabajo. Tardó un poco más de lo esperado."),
            Review("Iván Sánchez", "01 Marzo 2024", "Excelente servicio, es muy cuidadosa con su trabajo.")
        )

        rvReviews.adapter = ReviewAdapter(dummyReviews)
    }
}