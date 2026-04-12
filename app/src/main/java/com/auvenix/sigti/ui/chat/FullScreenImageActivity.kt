package com.auvenix.sigti.ui.chat

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.bumptech.glide.Glide

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageView = findViewById<ImageView>(R.id.fullImageView)
        val btnClose = findViewById<ImageView>(R.id.btnClose)

        val imageUrl = intent.getStringExtra("imageUrl")

        Glide.with(this)
            .load(imageUrl)
            .into(imageView)

        btnClose.setOnClickListener {
            finish()
        }
    }
}