package com.auvenix.sigti.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.auvenix.sigti.databinding.ActivitySolicitanteExtraBinding
import com.auvenix.sigti.ui.auth.PasswordActivity

class SolicitanteExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitanteExtraBinding

    // Controla si el INE fue cargado
    private var ineSubido = false

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

        // ── Zona de carga del INE ─────────────────────────────
        binding.btnUploadIne.setOnClickListener {
            // TODO: conectar a galería/cámara real
            Toast.makeText(this, "Abriendo galería...", Toast.LENGTH_SHORT).show()
            ineSubido = true
            binding.tvIneStatus.text = "✓  INE_Frontal.jpg cargada con éxito"
            binding.tvIneStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        }

        // ── Botón Continuar ───────────────────────────────────
        binding.btnContinuarSolicitante.setOnClickListener {
            procesarYContinuar()
        }
    }

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
        if (direccion.isEmpty()) {
            binding.etDireccion.error = "Campo obligatorio"
            hayError = true
        } else {
            binding.etDireccion.error = null
        }

        if (hayError) return

        // Validar INE
        if (!ineSubido) {
            Toast.makeText(this, "Por favor sube una foto de tu INE", Toast.LENGTH_SHORT).show()
            return
        }

        // Todo correcto → continuar a contraseña
        startActivity(Intent(this, PasswordActivity::class.java).apply {
            putExtras(intent)                        // datos de RegisterGeneralActivity
            putExtra("extra_ciudad",    ciudad)
            putExtra("extra_direccion", direccion)
        })
    }
}