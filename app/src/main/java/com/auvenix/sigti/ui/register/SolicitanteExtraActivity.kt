package com.auvenix.sigti.ui.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.auvenix.sigti.databinding.ActivitySolicitanteExtraBinding
import com.auvenix.sigti.ui.auth.PasswordActivity
import com.auvenix.sigti.utils.Validators

class SolicitanteExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitanteExtraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySolicitanteExtraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // ── Botón Continuar ───────────────────────────────────────────────────
        binding.btnContinuarSolicitante.setOnClickListener {
            procesarYContinuar()
        }
    }

    // ── Procesado y navegación ────────────────────────────────────────────────
    private fun procesarYContinuar() {
        var hayError = false

        // Validar ciudad
        val ciudad = binding.etCiudad.text.toString().trim()
        if (ciudad.isEmpty()) {
            binding.etCiudad.error = "Campo obligatorio"
            hayError = true
        } else {
            binding.etCiudad.error = null
        }

        // Validar dirección
        val direccion = binding.etDireccion.text.toString().trim()
        val addressResult = Validators.validateAddress(direccion)
        if (addressResult != Validators.AddressResult.Ok) {
            binding.etDireccion.error = addressResult.message()
            hayError = true
        } else {
            binding.etDireccion.error = null
        }

        // Si falta algún dato, detenemos el proceso
        if (hayError) return

        // 🚀 Todo correcto → avanzar a poner contraseña (ya no pide INE)
        startActivity(Intent(this, PasswordActivity::class.java).apply {
            putExtras(intent)                              // Pasamos los datos de RegisterGeneralActivity
            putExtra("extra_ciudad",    ciudad)
            putExtra("extra_direccion", direccion)
        })
    }
}