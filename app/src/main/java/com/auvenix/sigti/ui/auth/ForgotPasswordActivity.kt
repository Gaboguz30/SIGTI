package com.auvenix.sigti.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Luego aquí ponemos: enviar correo con código / link
    }
}