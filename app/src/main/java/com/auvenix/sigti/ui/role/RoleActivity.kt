package com.auvenix.sigti.ui.role

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityRoleBinding
import com.auvenix.sigti.ui.auth.GoogleCompleteProfileActivity
import com.auvenix.sigti.ui.register.RegisterGeneralActivity
import com.auvenix.sigti.utils.Constants

class RoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPrestador.setOnClickListener  { goToRegister("PRESTADOR")  }
        binding.btnSolicitante.setOnClickListener { goToRegister("SOLICITANTE") }
    }

    private fun goToRegister(role: String) {
        val isGoogle = intent.getBooleanExtra(Constants.EXTRA_IS_GOOGLE, false)

        if (isGoogle) {
            startActivity(Intent(this, GoogleCompleteProfileActivity::class.java).apply {
                putExtra("EXTRA_ROLE",             role)
                putExtra("EXTRA_NOMBRE_COMPLETO",  intent.getStringExtra(Constants.EXTRA_NOMBRE))
                putExtra("EXTRA_EMAIL",            intent.getStringExtra(Constants.EXTRA_EMAIL_GOOGLE))
                putExtra("EXTRA_UID",              intent.getStringExtra(Constants.EXTRA_UID))
            })
        } else {
            startActivity(Intent(this, RegisterGeneralActivity::class.java).apply {
                putExtra(RegisterGeneralActivity.EXTRA_ROLE, role)
            })
        }
        finish()
    }
}