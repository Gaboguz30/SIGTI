package com.auvenix.sigti.ui.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.auvenix.sigti.databinding.ActivitySolicitanteExtraBinding
import com.auvenix.sigti.ui.auth.PasswordActivity

class SolicitanteExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitanteExtraBinding
    private var ineUri: Uri? = null

    private val pickIneLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                ineUri = uri
                binding.tvIneStatus.text = "INE seleccionada correctamente"
            } else {
                binding.tvIneStatus.text = "Ningún archivo seleccionado"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySolicitanteExtraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val role = intent.getStringExtra(RegisterGeneralActivity.EXTRA_ROLE)
        if (role != RegisterGeneralActivity.ROLE_SOLICITANTE) {
            Toast.makeText(this, "Flujo inválido para solicitante", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        setupInitialData()
        setupActions()
    }

    private fun setupInitialData() {
        binding.etCiudad.setText("Tehuacán")
        binding.etCiudad.isEnabled = false
        binding.etCiudad.isFocusable = false
        binding.etCiudad.isClickable = false

        binding.tvIneStatus.text = "Ningún archivo seleccionado"
    }

    private fun setupActions() {
        binding.btnUploadIne.setOnClickListener {
            pickIneLauncher.launch("image/*")
        }

        binding.btnContinuarSolicitante.setOnClickListener {
            procesarDatosYContinuar()
        }
    }

    private fun procesarDatosYContinuar() {
        val ciudad = binding.etCiudad.text?.toString()?.trim().orEmpty()
        val direccion = binding.etDireccion.text?.toString()?.trim().orEmpty()

        var hayError = false

        if (ciudad.isBlank()) {
            Toast.makeText(this, "La ciudad es obligatoria", Toast.LENGTH_SHORT).show()
            hayError = true
        }

        if (ciudad != "Tehuacán") {
            Toast.makeText(this, "La ciudad debe ser Tehuacán", Toast.LENGTH_SHORT).show()
            hayError = true
        }

        if (direccion.isBlank()) {
            binding.etDireccion.error = "La dirección es obligatoria"
            hayError = true
        } else if (direccion.length < 10) {
            binding.etDireccion.error = "Ingresa una dirección más completa"
            hayError = true
        } else {
            binding.etDireccion.error = null
        }

        if (ineUri == null) {
            Toast.makeText(this, "Debes subir una foto de tu INE", Toast.LENGTH_SHORT).show()
            hayError = true
        }

        if (hayError) return

        val nextIntent = Intent(this, PasswordActivity::class.java).apply {
            putExtras(intent)
            putExtra("extra_ciudad", ciudad)
            putExtra("extra_direccion", direccion)
            putExtra("extra_foto_ine_uri", ineUri.toString())
        }

        startActivity(nextIntent)
    }
}