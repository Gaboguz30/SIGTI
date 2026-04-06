package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityProviderRedirectBinding

class ProviderRedirectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProviderRedirectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderRedirectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = "https://sigti.com.mx/registro"

        binding.btnGoToWeb.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // Redirección automática después de 4 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }, 1000)
    }
}