package com.auvenix.sigti.ui.support

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R

class QueEsSigtiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_que_es_sigti)

        // 🔥 HEADER (IMPORTANTE)
        val header = findViewById<View>(R.id.header)

        val titulo = header.findViewById<TextView>(R.id.tvHeaderTitle)
        titulo.text = "¿Qué es SIGTI?"

        val btnBack = header.findViewById<ImageView>(R.id.btnBackHeader)
        btnBack.setOnClickListener {
            finish()
        }

        val btn = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSiguiente)

        btn.setOnClickListener {
            startActivity(Intent(this, ComoFunciona1Activity::class.java))
        }
    }
}