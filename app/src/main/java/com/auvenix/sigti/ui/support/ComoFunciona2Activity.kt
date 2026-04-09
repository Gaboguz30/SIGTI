package com.auvenix.sigti.ui.support

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton

class ComoFunciona2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_como_funciona2)

        val header = findViewById<View>(R.id.header)

        val titulo = header.findViewById<TextView>(R.id.tvHeaderTitle)
        titulo.text = "Final"

        val btnBack = header.findViewById<ImageView>(R.id.btnBackHeader)
        btnBack.setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnSiguiente).setOnClickListener {
            finish()
        }
    }
}