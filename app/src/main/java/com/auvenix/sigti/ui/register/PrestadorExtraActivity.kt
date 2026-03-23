package com.auvenix.sigti.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityPrestadorExtraBinding
import com.auvenix.sigti.ui.auth.PasswordActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class PrestadorExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrestadorExtraBinding

    // Lista maestra — máximo 4 y sin repetir
    private val OFICIOS = listOf("Albañil", "Electricista", "Plomero", "Carpintero")

    // Rastrea qué oficios ya fueron seleccionados en cada fila
    // clave: View de la fila → oficio seleccionado (o null si no ha elegido)
    private val oficiosPorFila = LinkedHashMap<View, String?>()

    private var ineSubido = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrestadorExtraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        agregarFilaOficio()

        binding.btnAddOficio.setOnClickListener {
            if (oficiosPorFila.size >= OFICIOS.size) {
                Toast.makeText(
                    this,
                    "Ya agregaste todos los oficios disponibles (máx. ${OFICIOS.size})",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            agregarFilaOficio()
        }

        binding.btnUploadIne.setOnClickListener {
            Toast.makeText(this, "Abriendo galería...", Toast.LENGTH_SHORT).show()
            ineSubido = true
            binding.tvIneStatus.text = "✓  INE_Frontal.jpg cargada con éxito"
            binding.tvIneStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        }

        binding.btnContinuarExtra.setOnClickListener {
            procesarDatosYContinuar()
        }
    }

    private fun oficiosYaUsados(excluyendoFila: View? = null): List<String> {
        return oficiosPorFila
            .filter { (fila, oficio) -> fila != excluyendoFila && oficio != null }
            .values
            .filterNotNull()
    }

    private fun actualizarDropdowns() {
        for ((fila, oficioActual) in oficiosPorFila) {
            val autoComplete = fila.findViewById<AutoCompleteTextView>(R.id.etOficioNombre) ?: continue

            // Esta fila puede ver: los oficios no usados en otras filas + su propio oficio actual
            val usadosEnOtrasFila = oficiosYaUsados(excluyendoFila = fila)
            val disponibles = OFICIOS.filter { it !in usadosEnOtrasFila }

            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, disponibles)
            autoComplete.setAdapter(adapter)
        }
    }

    private fun agregarFilaOficio() {
        val vistaOficio: View = LayoutInflater.from(this)
            .inflate(R.layout.row_oficio, binding.llOficiosContainer, false)

        // Registrar la fila sin oficio seleccionado todavía
        oficiosPorFila[vistaOficio] = null

        val autoComplete = vistaOficio.findViewById<AutoCompleteTextView>(R.id.etOficioNombre)
        val tilOficio    = vistaOficio.findViewById<TextInputLayout>(R.id.tilOficioNombre)

        autoComplete.threshold = 0

        autoComplete.setOnClickListener {
            actualizarDropdowns()   // recalcula disponibles antes de abrir
            autoComplete.showDropDown()
        }

        autoComplete.setOnItemClickListener { _, _, _, _ ->
            val seleccionado = autoComplete.text.toString().trim()
            oficiosPorFila[vistaOficio] = seleccionado
            tilOficio?.error = null
            actualizarDropdowns()   // actualiza el resto de filas
            actualizarBotonAgregar()
        }

        // Botón eliminar
        vistaOficio.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRemoveOficio)
            .setOnClickListener {
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
        // Ocultar el botón si ya se usaron todos los oficios disponibles
        binding.btnAddOficio.isEnabled = oficiosPorFila.size < OFICIOS.size
        binding.btnAddOficio.alpha     = if (binding.btnAddOficio.isEnabled) 1f else 0.4f
    }
    
    private fun procesarDatosYContinuar() {
        var hayError = false

        // ── Ciudad ─────────────────────────────────────────────
        val ciudad = binding.etCiudad.text.toString().trim()
        if (ciudad.isEmpty()) { binding.etCiudad.error = "Campo obligatorio"; hayError = true }
        else binding.etCiudad.error = null

        // ── Dirección ──────────────────────────────────────────
        val direccion = binding.etDireccion.text.toString().trim()
        if (direccion.isEmpty()) { binding.etDireccion.error = "Campo obligatorio"; hayError = true }
        else binding.etDireccion.error = null

        if (hayError) return

        // ── Oficios ────────────────────────────────────────────
        val listaOficios = ArrayList<String>()

        for ((index, entry) in oficiosPorFila.entries.withIndex()) {
            val (fila, _) = entry
            val autoComplete = fila.findViewById<AutoCompleteTextView>(R.id.etOficioNombre)
            val tilOficio    = fila.findViewById<TextInputLayout>(R.id.tilOficioNombre)
            val etAnios      = fila.findViewById<TextInputEditText>(R.id.etOficioAnios)

            val nombre = autoComplete?.text.toString().trim()
            val anios  = etAnios?.text.toString().trim()

            when {
                nombre.isEmpty() -> {
                    tilOficio?.error = "Selecciona un oficio"
                    Toast.makeText(this, "Selecciona el oficio en la fila ${index + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                !OFICIOS.contains(nombre) -> {
                    tilOficio?.error = "Oficio no válido"
                    Toast.makeText(this, "Elige un oficio de la lista en fila ${index + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                anios.isEmpty() -> {
                    Toast.makeText(this, "Ingresa los años de experiencia en fila ${index + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                else -> {
                    tilOficio?.error = null
                    listaOficios.add("$nombre|$anios")
                }
            }
        }

        if (listaOficios.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un oficio", Toast.LENGTH_LONG).show()
            return
        }

        // ── INE ────────────────────────────────────────────────
        if (!ineSubido) {
            Toast.makeText(this, "Por favor sube una foto de tu INE", Toast.LENGTH_SHORT).show()
            return
        }

        // ── Continuar ──────────────────────────────────────────
        if (intent.getBooleanExtra("extra_is_google", false)) {
            Toast.makeText(this, "Usuario de Google: guardando perfil...", Toast.LENGTH_SHORT).show()
        } else {
            startActivity(Intent(this, PasswordActivity::class.java).apply {
                putExtras(intent)
                putExtra("extra_ciudad",    ciudad)
                putExtra("extra_direccion", direccion)
                putStringArrayListExtra("extra_oficios", listaOficios)
            })
        }
    }
}