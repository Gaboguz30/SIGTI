package com.auvenix.sigti.ui.role

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityRoleBinding
import com.auvenix.sigti.ui.register.RegisterGeneralActivity

class RoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPrestador.setOnClickListener {
            goToRegister(RegisterGeneralActivity.ROLE_PRESTADOR)
        }

        binding.btnSolicitante.setOnClickListener {
            goToRegister(RegisterGeneralActivity.ROLE_SOLICITANTE)
        }
    }

    private fun goToRegister(role: String) {
        val next = Intent(this, RegisterGeneralActivity::class.java).apply {
            putExtra(RegisterGeneralActivity.EXTRA_ROLE, role)

            putExtra(
                RegisterGeneralActivity.EXTRA_IS_GOOGLE,
                intent.getBooleanExtra(RegisterGeneralActivity.EXTRA_IS_GOOGLE, false)
            )
            putExtra(
                RegisterGeneralActivity.EXTRA_GOOGLE_UID,
                intent.getStringExtra(RegisterGeneralActivity.EXTRA_GOOGLE_UID)
            )
            putExtra(
                RegisterGeneralActivity.EXTRA_GOOGLE_NAME,
                intent.getStringExtra(RegisterGeneralActivity.EXTRA_GOOGLE_NAME)
            )
            putExtra(
                RegisterGeneralActivity.EXTRA_GOOGLE_EMAIL,
                intent.getStringExtra(RegisterGeneralActivity.EXTRA_GOOGLE_EMAIL)
            )
            putExtra(
                RegisterGeneralActivity.EXTRA_GOOGLE_PHOTO_URL,
                intent.getStringExtra(RegisterGeneralActivity.EXTRA_GOOGLE_PHOTO_URL)
            )
        }

        startActivity(next)
        finish()
    }
}