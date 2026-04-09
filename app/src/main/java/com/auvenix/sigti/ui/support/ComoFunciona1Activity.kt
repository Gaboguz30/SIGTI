package com.auvenix.sigti.ui.support

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton

class ComoFunciona1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_como_funciona1)

        val header = findViewById<View>(R.id.header)

        val titulo = header.findViewById<TextView>(R.id.tvHeaderTitle)
        titulo.text = "¿Cómo funciona SIGTI?"

        val btnBack = header.findViewById<ImageView>(R.id.btnBackHeader)
        btnBack.setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnSiguiente).setOnClickListener {
            startActivity(Intent(this, ComoFunciona2Activity::class.java))
        }
    }
}