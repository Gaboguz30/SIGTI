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

class PrestadorExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrestadorExtraBinding

    private val OFICIOS = listOf("Albañil", "Electricista", "Plomero", "Carpintero")

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
            agregarFilaOficio()
        }

        binding.btnUploadIne.setOnClickListener {
            Toast.makeText(this, "Abriendo galería...", Toast.LENGTH_SHORT).show()
            binding.tvIneStatus.text = "INE_Frontal.jpg (Cargada con éxito)"
            binding.tvIneStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        }

        binding.btnContinuarExtra.setOnClickListener {
            procesarDatosYContinuar()
        }
    }

    private fun agregarFilaOficio() {
        val vistaOficio: View = LayoutInflater.from(this).inflate(R.layout.row_oficio, null)

        // ✅ Configurar el dropdown con la lista de oficios
        val autoComplete = vistaOficio.findViewById<AutoCompleteTextView>(R.id.etOficioNombre)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, OFICIOS)
        autoComplete.setAdapter(adapter)
        autoComplete.threshold = 0
        autoComplete.setOnClickListener { autoComplete.showDropDown() }
        autoComplete.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) autoComplete.showDropDown()
        }

        // Botón eliminar fila
        vistaOficio.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRemoveOficio)
            .setOnClickListener { binding.llOficiosContainer.removeView(vistaOficio) }

        binding.llOficiosContainer.addView(vistaOficio)
    }

    private fun procesarDatosYContinuar() {
        val ciudad = binding.etCiudad.text.toString().trim()
        if (ciudad.isEmpty()) {
            binding.etCiudad.error = "Campo obligatorio"
            return
        }

        val listaOficios = ArrayList<String>()

        for (i in 0 until binding.llOficiosContainer.childCount) {
            val fila = binding.llOficiosContainer.getChildAt(i)

            val autoComplete = fila.findViewById<AutoCompleteTextView>(R.id.etOficioNombre)
            val etAnios = fila.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etOficioAnios)

            val nombreOficio = autoComplete?.text.toString().trim()
            val aniosTexto   = etAnios?.text.toString().trim()

            // ✅ Validar que el oficio sea de la lista y los años tengan máximo 2 dígitos
            when {
                nombreOficio.isEmpty() -> {
                    Toast.makeText(this, "Selecciona un oficio en la fila ${i + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                !OFICIOS.contains(nombreOficio) -> {
                    Toast.makeText(this, "Oficio inválido en fila ${i + 1}. Selecciona de la lista.", Toast.LENGTH_SHORT).show()
                    return
                }
                aniosTexto.isEmpty() -> {
                    Toast.makeText(this, "Ingresa los años de experiencia en fila ${i + 1}", Toast.LENGTH_SHORT).show()
                    return
                }
                aniosTexto.length > 2 -> {
                    // No debería pasar por el maxLength="2" del XML, pero como seguridad extra:
                    Toast.makeText(this, "Máximo 99 años de experiencia", Toast.LENGTH_SHORT).show()
                    return
                }
                else -> listaOficios.add("$nombreOficio|$aniosTexto")
            }
        }

        if (listaOficios.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un oficio", Toast.LENGTH_LONG).show()
            return
        }

        val vieneDeGoogle = intent.getBooleanExtra("extra_is_google", false)

        if (vieneDeGoogle) {
            Toast.makeText(this, "Usuario de Google: Guardando en BD...", Toast.LENGTH_SHORT).show()
        } else {
            startActivity(Intent(this, PasswordActivity::class.java).apply {
                putExtras(intent)
                putExtra("extra_ciudad", ciudad)
                putStringArrayListExtra("extra_oficios", listaOficios)
            })
        }
    }
}