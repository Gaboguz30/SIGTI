package com.auvenix.sigti.ui.support

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.profile.ProfileActivity
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

        // 🔥 AQUÍ ESTÁ EL CAMBIO MAGISTRAL
        findViewById<MaterialButton>(R.id.btnSiguiente).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            // Limpiamos la memoria para que no se acumulen pantallas
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            // Animación suavecita de desvanecimiento
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}