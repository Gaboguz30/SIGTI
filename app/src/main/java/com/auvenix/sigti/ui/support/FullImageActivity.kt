package com.auvenix.sigti.ui.support

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.bumptech.glide.Glide

class FullImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        val imageView = findViewById<ImageView>(R.id.fullImageView)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // 🔥 TRAER IMAGEN
        val imageUrl = intent.getStringExtra("imageUrl")

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(imageView)
        }

        // 🔥 FORZAR QUE EL BOTÓN ESTÉ ENCIMA
        btnBack.bringToFront()

        // 🔥 BOTÓN REGRESAR
        btnBack.setOnClickListener {
            finish()
        }

        // 🔥 CLICK EN IMAGEN (opcional)
        imageView.setOnClickListener {
            finish()
        }
    }
}