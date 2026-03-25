package com.auvenix.sigti.ui.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityPrestadorExtraBinding
import com.auvenix.sigti.ui.auth.PasswordActivity
import com.auvenix.sigti.utils.Validators
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class PrestadorExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrestadorExtraBinding

    private val oficiosDisponibles = listOf(
        "Albañil",
        "Electricista",
        "Plomero",
        "Carpintero"
    )

    private val oficiosPorFila = LinkedHashMap<View, String?>()

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
        binding = ActivityPrestadorExtraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setupFixedCity()
        setupLocalidadDropdown()
        setupCodigoPostalDropdown()
        agregarFilaOficio()

        binding.btnAddOficio.setOnClickListener {
            if (oficiosPorFila.size >= oficiosDisponibles.size) {
                Toast.makeText(
                    this,
                    "Ya agregaste todos los oficios disponibles (máx. ${oficiosDisponibles.size})",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            agregarFilaOficio()
        }

        binding.btnUploadRostro.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnContinuarExtra.setOnClickListener {
            procesarDatosYContinuar()
        }
    }

    private fun setupFixedCity() {
        binding.etCiudad.setText("Tehuacán")
        binding.etCiudad.isEnabled = false
        binding.etCiudad.isFocusable = false
        binding.etCiudad.isClickable = false
    }

    private fun setupLocalidadDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            Validators.localidadesTehuacan
        )
        binding.actvLocalidad.setAdapter(adapter)
        binding.actvLocalidad.setOnClickListener { binding.actvLocalidad.showDropDown() }
        binding.actvLocalidad.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actvLocalidad.showDropDown()
        }
        binding.actvLocalidad.setOnItemClickListener { _, _, _, _ ->
            binding.tilLocalidad.error = null
        }
    }

    private fun setupCodigoPostalDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            Validators.postalCodeDropdownOptions
        )
        binding.actvCodigoPostal.setAdapter(adapter)
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

    private fun oficiosYaUsados(excluyendoFila: View? = null): List<String> {
        return oficiosPorFila
            .filter { (fila, oficio) -> fila != excluyendoFila && oficio != null }
            .values
            .filterNotNull()
    }

    private fun actualizarDropdownsOficios() {
        for ((fila, _) in oficiosPorFila) {
            val autoComplete = fila.findViewById<AutoCompleteTextView>(R.id.etOficioNombre) ?: continue
            val usadosEnOtrasFilas = oficiosYaUsados(excluyendoFila = fila)
            val disponibles = oficiosDisponibles.filter { it !in usadosEnOtrasFilas }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, disponibles)
            autoComplete.setAdapter(adapter)
        }
    }

    private fun agregarFilaOficio() {
        val vistaOficio = LayoutInflater.from(this)
            .inflate(R.layout.row_oficio, binding.llOficiosContainer, false)

        oficiosPorFila[vistaOficio] = null

        val autoComplete = vistaOficio.findViewById<AutoCompleteTextView>(R.id.etOficioNombre)
        val tilOficio = vistaOficio.findViewById<TextInputLayout>(R.id.tilOficioNombre)

        autoComplete.threshold = 0

        autoComplete.setOnClickListener {
            actualizarDropdownsOficios()
            autoComplete.showDropDown()
        }

        autoComplete.setOnItemClickListener { _, _, _, _ ->
            val seleccionado = autoComplete.text.toString().trim()
            oficiosPorFila[vistaOficio] = seleccionado
            tilOficio.error = null
            actualizarDropdownsOficios()
            actualizarBotonAgregar()
        }

        vistaOficio.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRemoveOficio)
            .setOnClickListener {
                if (oficiosPorFila.size > 1) {
                    oficiosPorFila.remove(vistaOficio)
                    binding.llOficiosContainer.removeView(vistaOficio)
                    actualizarDropdownsOficios()
                    actualizarBotonAgregar()
                } else {
                    Toast.makeText(this, "Debes tener al menos un oficio", Toast.LENGTH_SHORT).show()
                }
            }

        binding.llOficiosContainer.addView(vistaOficio)
        actualizarDropdownsOficios()
        actualizarBotonAgregar()
    }

    private fun actualizarBotonAgregar() {
        binding.btnAddOficio.isEnabled = oficiosPorFila.size < oficiosDisponibles.size
        binding.btnAddOficio.alpha = if (binding.btnAddOficio.isEnabled) 1f else 0.4f
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

    private fun procesarDatosYContinuar() {
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

        val listaOficios = ArrayList<String>()

        for ((index, entry) in oficiosPorFila.entries.withIndex()) {
            val (fila, _) = entry
            val autoComplete = fila.findViewById<AutoCompleteTextView>(R.id.etOficioNombre)
            val tilOficio = fila.findViewById<TextInputLayout>(R.id.tilOficioNombre)
            val etAnios = fila.findViewById<TextInputEditText>(R.id.etOficioAnios)

            val nombre = autoComplete.text?.toString()?.trim().orEmpty()
            val anios = etAnios.text?.toString()?.trim().orEmpty()

            when {
                nombre.isEmpty() -> {
                    tilOficio.error = "Selecciona un oficio"
                    Toast.makeText(this, "Selecciona el oficio en la fila ${index + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                nombre !in oficiosDisponibles -> {
                    tilOficio.error = "Oficio no válido"
                    Toast.makeText(this, "Elige un oficio de la lista en fila ${index + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                anios.isEmpty() -> {
                    Toast.makeText(this, "Ingresa los años de experiencia en fila ${index + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                anios.toIntOrNull() == null -> {
                    Toast.makeText(this, "Los años de experiencia deben ser numéricos en fila ${index + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                else -> {
                    tilOficio.error = null
                    listaOficios.add("$nombre|$anios")
                }
            }
        }

        if (listaOficios.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un oficio", Toast.LENGTH_LONG).show()
            return
        }

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
            putStringArrayListExtra("extra_oficios", listaOficios)
            putExtra("extra_ciudad", ciudad)
            putExtra("extra_localidad", localidad)
            putExtra("extra_codigo_postal", codigoPostal)
            putExtra("extra_foto_rostro_uri", rostroUri.toString())
        })
    }
}