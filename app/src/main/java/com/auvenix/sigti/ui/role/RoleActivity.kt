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
            goToRegister("PRESTADOR")
        }

        binding.btnSolicitante.setOnClickListener {
            goToRegister("SOLICITANTE")
        }
    }

    private fun goToRegister(role: String) {
        val i = Intent(this, RegisterGeneralActivity::class.java)
        i.putExtra(RegisterGeneralActivity.EXTRA_ROLE, role)
        startActivity(i)
    }
}