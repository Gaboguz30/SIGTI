package com.auvenix.sigti.ui.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.auvenix.sigti.databinding.ActivitySolicitanteExtraBinding
import com.auvenix.sigti.ui.auth.PasswordActivity
import com.auvenix.sigti.utils.Validators
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class SolicitanteExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitanteExtraBinding

    private var rostroUri: Uri? = null
    private var rostroValidado = false

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        validarFotoDeRostro(uri)
    }

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

        setupFixedCity()
        setupLocalidadDropdown()
        setupCodigoPostalDropdown()

        binding.btnUploadRostro.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnContinuarSolicitante.setOnClickListener {
            procesarYContinuar()
        }
    }

    private fun setupFixedCity() {
        binding.etCiudad.setText("Tehuacán")
        binding.etCiudad.isEnabled = false
        binding.etCiudad.isFocusable = false
        binding.etCiudad.isClickable = false
    }

    private fun setupLocalidadDropdown() {
        val localidadAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            Validators.localidadesTehuacan
        )
        binding.actvLocalidad.setAdapter(localidadAdapter)
        binding.actvLocalidad.setOnClickListener { binding.actvLocalidad.showDropDown() }
        binding.actvLocalidad.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actvLocalidad.showDropDown()
        }
        binding.actvLocalidad.setOnItemClickListener { _, _, _, _ ->
            binding.tilLocalidad.error = null
        }
    }

    private fun setupCodigoPostalDropdown() {
        val postalAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            Validators.postalCodeDropdownOptions
        )

        binding.actvCodigoPostal.setAdapter(postalAdapter)
        binding.actvCodigoPostal.filters = arrayOf(InputFilter.LengthFilter(5))
        binding.actvCodigoPostal.setOnClickListener { binding.actvCodigoPostal.showDropDown() }
        binding.actvCodigoPostal.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actvCodigoPostal.showDropDown()
        }
        binding.actvCodigoPostal.setOnItemClickListener { _, _, position, _ ->
            val option = Validators.postalCodeDropdownOptions[position]
            val cp = Validators.extractPostalCodeFromOption(option)
            if (cp.isBlank()) {
                binding.actvCodigoPostal.setText("")
            } else {
                binding.actvCodigoPostal.setText(cp, false)
                binding.actvCodigoPostal.setSelection(cp.length)
                binding.tilCodigoPostal.error = null
            }
        }
    }

    private fun validarFotoDeRostro(uri: Uri) {
        binding.tvRostroStatus.text = "Validando foto..."
        binding.tvRostroStatus.setTextColor(android.graphics.Color.parseColor("#757575"))
        rostroValidado = false
        rostroUri = null

        val image = try {
            InputImage.fromFilePath(this, uri)
        } catch (e: Exception) {
            binding.tvRostroStatus.text = "✗ No se pudo leer la imagen seleccionada"
            binding.tvRostroStatus.setTextColor(android.graphics.Color.parseColor("#C62828"))
            return
        }

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    rostroUri = uri
                    rostroValidado = true
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "foto_rostro"
                    binding.tvRostroStatus.text = "✓ $fileName validada correctamente"
                    binding.tvRostroStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                } else {
                    rostroUri = null
                    rostroValidado = false
                    binding.tvRostroStatus.text = "✗ No se detectó un rostro humano en la imagen"
                    binding.tvRostroStatus.setTextColor(android.graphics.Color.parseColor("#C62828"))
                }
                detector.close()
            }
            .addOnFailureListener { e ->
                rostroUri = null
                rostroValidado = false
                binding.tvRostroStatus.text = "✗ Error al validar la foto: ${e.localizedMessage}"
                binding.tvRostroStatus.setTextColor(android.graphics.Color.parseColor("#C62828"))
                detector.close()
            }
    }

    private fun procesarYContinuar() {
        var hayError = false

        val ciudad = "Tehuacán"
        val localidad = binding.actvLocalidad.text?.toString()?.trim().orEmpty()
        val codigoPostal = binding.actvCodigoPostal.text?.toString()?.trim().orEmpty()

        val localidadResult = Validators.validateLocality(localidad)
        if (localidadResult != Validators.LocalityResult.Ok) {
            binding.tilLocalidad.error = localidadResult.message()
            hayError = true
        } else {
            binding.tilLocalidad.error = null
        }

        val cpResult = Validators.validatePostalCode(codigoPostal)
        if (cpResult != Validators.PostalCodeResult.Ok) {
            binding.tilCodigoPostal.error = cpResult.message()
            hayError = true
        } else {
            binding.tilCodigoPostal.error = null
        }

        if (hayError) return

        if (rostroUri == null || !rostroValidado) {
            Toast.makeText(
                this,
                "Debes subir una foto de rostro válida para continuar",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        startActivity(Intent(this, PasswordActivity::class.java).apply {
            putExtras(intent)
            putExtra("extra_ciudad", ciudad)
            putExtra("extra_localidad", localidad)
            putExtra("extra_codigo_postal", codigoPostal)
            putExtra("extra_foto_rostro_uri", rostroUri.toString())
        })
    }
}