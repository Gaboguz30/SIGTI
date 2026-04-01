package com.auvenix.sigti.ui.register

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityPrestadorExtraBinding
import com.auvenix.sigti.ui.auth.PasswordActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale

class PrestadorExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrestadorExtraBinding

    private val oficios = listOf("Albañil", "Electricista", "Plomero", "Carpintero")

    private val codigosCabecera = listOf(
        "75700", "75710", "75717", "75718", "75719", "75720", "75725", "75726", "75730",
        "75731", "75732", "75740", "75741", "75742", "75743", "75750", "75758", "75760",
        "75763", "75764", "75765", "75766", "75768", "75769", "75770", "75780", "75784",
        "75786", "75790", "75793", "75794", "75795", "75796", "75797", "75799"
    )

    private val localidades = listOf(
        "Tehuacán",
        "Santa Cruz Acapa",
        "Santa Catarina Otzolotepec",
        "San Marcos Necoxtla",
        "Magdalena Cuayucatepec",
        "San Pedro Acoquiaco",
        "San Nicolás Tetitzintla",
        "Santa María Coapan",
        "San Lorenzo Teotipilco",
        "San Diego Chalma",
        "San Cristóbal Tepeteopan",
        "San Pablo Tepetzingo",
        "Santa Ana Teloxtoc"
    )

    private val codigosPorLocalidad = linkedMapOf(
        "Tehuacán" to codigosCabecera,
        "Santa Cruz Acapa" to listOf("75859"),
        "Santa Catarina Otzolotepec" to listOf("75855"),
        "San Marcos Necoxtla" to listOf("75859"),
        "Magdalena Cuayucatepec" to listOf("75853"),
        "San Pedro Acoquiaco" to codigosCabecera,
        "San Nicolás Tetitzintla" to codigosCabecera,
        "Santa María Coapan" to listOf("75857"),
        "San Lorenzo Teotipilco" to listOf("75855"),
        "San Diego Chalma" to listOf("75859"),
        "San Cristóbal Tepeteopan" to listOf("75853"),
        "San Pablo Tepetzingo" to listOf("75859"),
        "Santa Ana Teloxtoc" to listOf("75856")
    )

    private val localidadUnicaPorCodigo = linkedMapOf(
        "75853" to "Magdalena Cuayucatepec",
        "75855" to "Santa Catarina Otzolotepec",
        "75856" to "Santa Ana Teloxtoc",
        "75857" to "Santa María Coapan"
    )

    private val oficiosPorFila = LinkedHashMap<View, String?>()

    private var fotoFrontalUri: Uri? = null
    private var tempCameraUri: Uri? = null

    private var isUpdatingLocalidad = false
    private var isUpdatingCodigoPostal = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openFrontCamera()
        else Toast.makeText(this, "Debes conceder permiso de cámara para tomar tu foto frontal", Toast.LENGTH_LONG).show()
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && tempCameraUri != null) {
            fotoFrontalUri = tempCameraUri
            binding.ivFotoFrontalPreview.setImageURI(fotoFrontalUri)
            binding.ivFotoFrontalPreview.visibility = View.VISIBLE
            binding.tvFotoFrontalStatus.text = "✓ Foto frontal capturada correctamente"
            binding.tvFotoFrontalStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        } else {
            Toast.makeText(this, "No se pudo capturar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrestadorExtraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val role = intent.getStringExtra(RegisterGeneralActivity.EXTRA_ROLE)
        if (role != RegisterGeneralActivity.ROLE_PRESTADOR) {
            Toast.makeText(this, "Flujo inválido para prestador", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupLocationFields()
        agregarFilaOficio()

        binding.btnAddOficio.setOnClickListener {
            if (oficiosPorFila.size >= oficios.size) {
                Toast.makeText(this, "Ya agregaste todos los oficios disponibles (máx. ${oficios.size})", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            agregarFilaOficio()
        }

        binding.btnCapturarFotoFrontal.setOnClickListener { requestCameraAndOpen() }
        binding.btnContinuarExtra.setOnClickListener { procesarDatosYContinuar() }
    }

    private fun setupLocationFields() {
        binding.etCiudadOperacion.setText("Tehuacán")
        binding.etCiudadOperacion.isEnabled = false
        binding.etCiudadOperacion.isFocusable = false
        binding.etCiudadOperacion.isClickable = false

        setLocalidadAdapter(localidades)
        setCodigoPostalAdapter(codigosCabecera)

        binding.actvLocalidad.setOnClickListener { binding.actvLocalidad.showDropDown() }
        binding.actvCodigoPostal.setOnClickListener { binding.actvCodigoPostal.showDropDown() }

        binding.actvLocalidad.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actvLocalidad.showDropDown()
        }

        binding.actvCodigoPostal.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actvCodigoPostal.showDropDown()
        }

        binding.actvLocalidad.setOnItemClickListener { _, _, _, _ ->
            val localidad = binding.actvLocalidad.text.toString().trim()
            aplicarRelacionDesdeLocalidad(localidad, true)
        }

        binding.actvCodigoPostal.setOnItemClickListener { _, _, _, _ ->
            val codigo = binding.actvCodigoPostal.text.toString().trim()
            aplicarRelacionDesdeCodigo(codigo, true)
        }

        binding.actvLocalidad.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingLocalidad) return
                val texto = s?.toString()?.trim().orEmpty()
                binding.tilLocalidad.error = null
                filtrarSugerenciasLocalidad(texto)

                val matchExacto = localidades.firstOrNull { it.equals(texto, true) }
                if (matchExacto != null) {
                    aplicarRelacionDesdeLocalidad(matchExacto, true)
                } else {
                    isUpdatingCodigoPostal = true
                    binding.actvCodigoPostal.setText("", false)
                    setCodigoPostalAdapter(codigosCabecera)
                    isUpdatingCodigoPostal = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        binding.actvCodigoPostal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingCodigoPostal) return
                val texto = s?.toString()?.trim().orEmpty()
                binding.tilCodigoPostal.error = null
                filtrarSugerenciasCodigo(texto)
                if (texto.length == 5) aplicarRelacionDesdeCodigo(texto, true)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }

    private fun aplicarRelacionDesdeLocalidad(localidad: String, autocompletarCodigo: Boolean) {
        if (localidad !in localidades) return
        val codigos = codigosPorLocalidad[localidad] ?: codigosCabecera
        setCodigoPostalAdapter(codigos)

        if (!autocompletarCodigo) return

        isUpdatingCodigoPostal = true
        if (codigos.size == 1) binding.actvCodigoPostal.setText(codigos.first(), false)
        else binding.actvCodigoPostal.setText("", false)
        isUpdatingCodigoPostal = false
    }

    private fun aplicarRelacionDesdeCodigo(codigo: String, autocompletarLocalidad: Boolean) {
        if (codigo.length != 5) return

        val localidadDetectada = when {
            codigo in codigosCabecera -> "Tehuacán"
            codigo in localidadUnicaPorCodigo -> localidadUnicaPorCodigo[codigo]
            codigo == "75859" -> null
            else -> null
        }

        if (autocompletarLocalidad && localidadDetectada != null) {
            isUpdatingLocalidad = true
            binding.actvLocalidad.setText(localidadDetectada, false)
            isUpdatingLocalidad = false
            aplicarRelacionDesdeLocalidad(localidadDetectada, false)
            return
        }

        if (codigo == "75859") {
            val posibles = listOf(
                "Santa Cruz Acapa",
                "San Marcos Necoxtla",
                "San Diego Chalma",
                "San Pablo Tepetzingo"
            )
            setLocalidadAdapter(posibles)
            if (autocompletarLocalidad) {
                isUpdatingLocalidad = true
                binding.actvLocalidad.setText("", false)
                isUpdatingLocalidad = false
                binding.actvLocalidad.post { binding.actvLocalidad.showDropDown() }
            }
            return
        }

        if (codigo in codigosCabecera) setLocalidadAdapter(localidades)
    }

    private fun filtrarSugerenciasLocalidad(texto: String) {
        val sugerencias = if (texto.isBlank()) localidades else localidades.filter { it.contains(texto, true) }
        setLocalidadAdapter(if (sugerencias.isEmpty()) localidades else sugerencias)
    }

    private fun filtrarSugerenciasCodigo(texto: String) {
        val localidadActual = binding.actvLocalidad.text.toString().trim()
        val base = codigosPorLocalidad[localidadActual] ?: codigosCabecera
        val sugerencias = if (texto.isBlank()) base else base.filter { it.startsWith(texto) }
        setCodigoPostalAdapter(if (sugerencias.isEmpty()) base else sugerencias)
    }

    private fun setLocalidadAdapter(items: List<String>) {
        binding.actvLocalidad.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items.distinct()))
    }

    private fun setCodigoPostalAdapter(items: List<String>) {
        binding.actvCodigoPostal.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items.distinct()))
    }

    private fun requestCameraAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> openFrontCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "SIGTI necesita acceso a la cámara para tomar tu foto frontal", Toast.LENGTH_LONG).show()
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openFrontCamera() {
        val imageFile = createImageFile()
        tempCameraUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri)
            putExtra("android.intent.extras.CAMERA_FACING", 1)
            putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(packageManager) != null) cameraLauncher.launch(intent)
        else Toast.makeText(this, "No se encontró una aplicación de cámara disponible", Toast.LENGTH_SHORT).show()
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(cacheDir, "registro_fotos").apply { if (!exists()) mkdirs() }
        return File(storageDir, "FOTO_FRONTAL_${timeStamp}.jpg")
    }

    private fun oficiosYaUsados(excluyendoFila: View? = null): List<String> {
        return oficiosPorFila.filter { (fila, oficio) -> fila != excluyendoFila && oficio != null }
            .values.filterNotNull()
    }

    private fun actualizarDropdowns() {
        for ((fila, _) in oficiosPorFila) {
            val autoComplete = fila.findViewById<AutoCompleteTextView>(R.id.etOficioNombre) ?: continue
            val usadosEnOtrasFila = oficiosYaUsados(fila)
            val disponibles = oficios.filter { it !in usadosEnOtrasFila }
            autoComplete.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, disponibles))
        }
    }

    private fun agregarFilaOficio() {
        val vistaOficio = LayoutInflater.from(this).inflate(R.layout.row_oficio, binding.llOficiosContainer, false)
        oficiosPorFila[vistaOficio] = null

        val autoComplete = vistaOficio.findViewById<AutoCompleteTextView>(R.id.etOficioNombre)
        val tilOficio = vistaOficio.findViewById<TextInputLayout>(R.id.tilOficioNombre)

        autoComplete.threshold = 0
        autoComplete.setOnClickListener {
            actualizarDropdowns()
            autoComplete.showDropDown()
        }

        autoComplete.setOnItemClickListener { _, _, _, _ ->
            oficiosPorFila[vistaOficio] = autoComplete.text.toString().trim()
            tilOficio.error = null
            actualizarDropdowns()
            actualizarBotonAgregar()
        }

        vistaOficio.findViewById<MaterialButton>(R.id.btnRemoveOficio).setOnClickListener {
            if (oficiosPorFila.size > 1) {
                oficiosPorFila.remove(vistaOficio)
                binding.llOficiosContainer.removeView(vistaOficio)
                actualizarDropdowns()
                actualizarBotonAgregar()
            } else {
                Toast.makeText(this, "Debes tener al menos un oficio", Toast.LENGTH_SHORT).show()
            }
        }

        binding.llOficiosContainer.addView(vistaOficio)
        actualizarDropdowns()
        actualizarBotonAgregar()
    }

    private fun actualizarBotonAgregar() {
        binding.btnAddOficio.isEnabled = oficiosPorFila.size < oficios.size
        binding.btnAddOficio.alpha = if (binding.btnAddOficio.isEnabled) 1f else 0.4f
    }

    private fun procesarDatosYContinuar() {
        var hayError = false

        val ciudad = binding.etCiudadOperacion.text.toString().trim()
        val localidad = binding.actvLocalidad.text.toString().trim()
        val codigoPostal = binding.actvCodigoPostal.text.toString().trim()

        if (ciudad != "Tehuacán") {
            Toast.makeText(this, "La ciudad de operación debe ser Tehuacán", Toast.LENGTH_SHORT).show()
            return
        }

        if (localidad.isEmpty()) {
            binding.tilLocalidad.error = "Selecciona o escribe una localidad"
            hayError = true
        } else if (localidad !in localidades) {
            binding.tilLocalidad.error = "Ingresa una localidad válida de la lista"
            hayError = true
        } else {
            binding.tilLocalidad.error = null
        }

        val codigosValidos = codigosPorLocalidad[localidad] ?: codigosCabecera
        if (codigoPostal.isEmpty()) {
            binding.tilCodigoPostal.error = "Selecciona o escribe un código postal"
            hayError = true
        } else if (codigoPostal !in codigosValidos) {
            binding.tilCodigoPostal.error = "El código postal no corresponde a la localidad seleccionada"
            hayError = true
        } else {
            binding.tilCodigoPostal.error = null
        }

        if (hayError) return

        val listaOficios = arrayListOf<String>()
        for ((index, entry) in oficiosPorFila.entries.withIndex()) {
            val (fila, _) = entry
            val autoComplete = fila.findViewById<AutoCompleteTextView>(R.id.etOficioNombre)
            val tilOficio = fila.findViewById<TextInputLayout>(R.id.tilOficioNombre)
            val etAnios = fila.findViewById<TextInputEditText>(R.id.etOficioAnios)

            val nombre = autoComplete.text.toString().trim()
            val anios = etAnios.text.toString().trim()

            when {
                nombre.isEmpty() -> {
                    tilOficio.error = "Selecciona un oficio"
                    Toast.makeText(this, "Selecciona el oficio en la fila ${index + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                nombre !in oficios -> {
                    tilOficio.error = "Oficio no válido"
                    Toast.makeText(this, "Elige un oficio de la lista en la fila ${index + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                anios.isEmpty() -> {
                    Toast.makeText(this, "Ingresa los años de experiencia en la fila ${index + 1}", Toast.LENGTH_SHORT).show()
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

        if (fotoFrontalUri == null) {
            Toast.makeText(this, "Debes tomar una foto frontal antes de continuar", Toast.LENGTH_SHORT).show()
            return
        }

        startActivity(Intent(this, PasswordActivity::class.java).apply {
            putExtras(intent)
            putExtra("extra_ciudad", ciudad)
            putExtra("extra_localidad", localidad)
            putExtra("extra_codigo_postal", codigoPostal)
            putExtra("extra_foto_frontal_uri", fotoFrontalUri.toString())
            putStringArrayListExtra("extra_oficios", listaOficios)
        })
    }
}