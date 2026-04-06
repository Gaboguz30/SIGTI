package com.auvenix.sigti.ui.role

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityRoleBinding
import com.auvenix.sigti.ui.auth.AuthEntryActivity
import com.auvenix.sigti.ui.auth.ProviderRedirectActivity
import com.auvenix.sigti.ui.register.RegisterGeneralActivity

class RoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Clic en Prestador -> Lo mandamos directo a la pantalla que lo redirige a la web
        binding.btnPrestador.setOnClickListener {
            val intent = Intent(this, ProviderRedirectActivity::class.java)
            startActivity(intent)
           // finish() // Cerramos esta pantalla para que no pueda regresar con el botón atrás
        }

        // 2. Clic en Solicitante -> Lo mandamos a la entrada de Auth (AuthEntryActivity)
        binding.btnSolicitante.setOnClickListener {
            val intent = Intent(this, RegisterGeneralActivity::class.java)
            // Le pasamos el rol en la "mochila" (Intent) por si la siguiente pantalla lo necesita
            intent.putExtra("EXTRA_ROLE", "SOLICITANTE")
            startActivity(intent)
            //finish()
        }
    }
}