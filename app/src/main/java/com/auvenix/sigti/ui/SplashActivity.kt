package com.auvenix.sigti.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.session.SessionManager
import com.auvenix.sigti.ui.auth.LoginActivity
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.imgLogoSplash)
        val texto = findViewById<TextView>(R.id.tvSplashText)

        // 🔥 1. LA ANIMACIÓN: Empiezan invisibles (alpha = 0)
        logo.alpha = 0f
        texto.alpha = 0f

        // Aparecen suavemente por 1.5 segundos (1500 ms)
        logo.animate().setDuration(1500).alpha(1f)
        texto.animate().setDuration(1500).alpha(1f).withEndAction {
            // Cuando termina la animación, ejecutamos la revisión de sesión
            revisarSesionAbierta()
        }
    }

    // 🔥 2. EL GUARDIÁN DE LA SESIÓN
    private fun revisarSesionAbierta() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // ¡EL USUARIO YA HABÍA INICIADO SESIÓN ANTES!
            // Leemos su rol para saber a qué pantalla mandarlo
            val sessionManager = SessionManager(this)
            val rol = sessionManager.getRole()

            if (rol == "PRESTADOR") {
                startActivity(Intent(this, ProviderHomeActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
        } else {
            // ¡NO HAY SESIÓN! Lo mandamos a que se loguee
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // 🔥 TRUCO: Destruimos el Splash para que al darle "Atrás" el usuario salga de la app y no regrese a esta pantalla
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}