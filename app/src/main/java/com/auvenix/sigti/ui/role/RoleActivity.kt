package com.auvenix.sigti.ui.role

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityRoleBinding
import com.auvenix.sigti.ui.auth.GoogleCompleteProfileActivity // Tu nueva ventana
import com.auvenix.sigti.ui.register.RegisterGeneralActivity

class RoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPrestador.setOnClickListener {
            goToRegister("PRESTADOR")
        }

        binding.btnSolicitante.setOnClickListener {
            goToRegister("SOLICITANTE")
        }
    }

    private fun goToRegister(role: String) {
        // 1. Checamos si la bandera "IS_GOOGLE" existe y es true
        val isGoogle = intent.getBooleanExtra("IS_GOOGLE", false)

        if (isGoogle) {
            // CAMINO A: Viene de Google -> Va a completar datos
            val i = Intent(this, GoogleCompleteProfileActivity::class.java).apply {
                putExtra("EXTRA_ROLE", role)
                // Reenviamos lo que traíamos de Google para no perderlo
                putExtra("EXTRA_NOMBRE_COMPLETO", intent.getStringExtra("EXTRA_NOMBRE_COMPLETO"))
                putExtra("EXTRA_EMAIL", intent.getStringExtra("EXTRA_EMAIL"))
                putExtra("EXTRA_UID", intent.getStringExtra("EXTRA_UID"))
            }
            startActivity(i)
        } else {
            // CAMINO B: Flujo normal -> Va al registro general (tu código original)
            val i = Intent(this, RegisterGeneralActivity::class.java)
            i.putExtra(RegisterGeneralActivity.EXTRA_ROLE, role)
            startActivity(i)
        }
        finish() // Opcional: para que no regresen a elegir rol
    }
}