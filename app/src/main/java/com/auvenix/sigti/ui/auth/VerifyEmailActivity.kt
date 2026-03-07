package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.auvenix.sigti.databinding.ActivityVerifyEmailBinding
import com.auvenix.sigti.session.SessionManager
import com.auvenix.sigti.ui.home.HomeActivity

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private lateinit var session: SessionManager
    private var verified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        val email = intent.getStringExtra(EXTRA_EMAIL).orEmpty()
        binding.tvEmail.text = email

        binding.etCode.doAfterTextChanged { binding.tilCode.error = null }

        binding.btnVerify.setOnClickListener {
            val code = binding.etCode.text?.toString()?.trim().orEmpty()

            if (code.length != 6 || !code.all { it.isDigit() }) {
                binding.tilCode.error = "Ingresa 6 dígitos"
                return@setOnClickListener
            }

            // ✅ Mock por ahora (luego API): 123456
            if (code != "123456") {
                binding.tilCode.error = "Código incorrecto"
                return@setOnClickListener
            }

            // ✅ Ya verificó
            verified = true

            // ✅ Si había "Recuérdame" pendiente, lo hacemos permanente
            session.promotePendingToRemembered()

            Toast.makeText(this, "Correo verificado ✅", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }

        binding.tvResend.setOnClickListener {
            Toast.makeText(this, "Reenvío (pendiente de API)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Si se sale sin verificar, se borra lo guardado "pendiente"
        if (!verified) {
            session.clearPendingRemember()
        }
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}