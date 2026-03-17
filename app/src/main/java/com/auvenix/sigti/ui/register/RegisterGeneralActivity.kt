package com.auvenix.sigti.ui.register

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.auvenix.sigti.databinding.ActivityRegisterGeneralBinding
import com.auvenix.sigti.ui.auth.PasswordActivity
import com.auvenix.sigti.ui.register.PrestadorExtraActivity
import java.util.Calendar

class RegisterGeneralActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterGeneralBinding

    // "M" o "F"
    private var selectedGender: String? = null

    private val selectedStrokeColor by lazy {
        ContextCompat.getColor(this, android.R.color.holo_blue_dark)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterGeneralBinding.inflate(layoutInflater)
        setContentView(binding.root)

        android.util.Log.d("SIGTI_DEBUG", "RegisterGeneralActivity onCreate OK")

        val role = intent.getStringExtra(EXTRA_ROLE) ?: ROLE_NONE

        setupGenderSelection()
        setupDOBPicker()
        setupRealtimeValidation()
        setupOnlyLettersFilters()

        binding.btnContinuar.isEnabled = true
        binding.btnContinuar.isClickable = true

        // ✅ ÚNICO listener del botón
        binding.btnContinuar.setOnClickListener {
            android.util.Log.d("SIGTI_DEBUG", "CLICK btnContinuar (final)")

            if (!validateAll(showErrors = true)) {
                Toast.makeText(this, "Revisa los campos marcados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nombre = binding.etNombre.text.toString().trim()
            val apPat = binding.etApellidoPaterno.text.toString().trim()
            val apMat = binding.etApellidoMaterno.text.toString().trim()
            val fechaNac = binding.etFechaNac.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val gender = selectedGender!! // ya validado

            if (role == ROLE_PRESTADOR) {
                val i = Intent(this, PrestadorExtraActivity::class.java).apply {
                    putExtra(EXTRA_ROLE, role)
                    putExtra(EXTRA_NOMBRE, nombre)
                    putExtra(EXTRA_AP_PATERNO, apPat)
                    putExtra(EXTRA_AP_MATERNO, apMat)
                    putExtra(EXTRA_FECHA_NAC, fechaNac)
                    putExtra(EXTRA_EMAIL, email)
                    putExtra(EXTRA_GENERO, gender)
                }
                startActivity(i)
            } else {
                val i = Intent(this, PasswordActivity::class.java).apply {
                    putExtra(EXTRA_ROLE, role)
                    putExtra(EXTRA_NOMBRE, nombre)
                    putExtra(EXTRA_AP_PATERNO, apPat)
                    putExtra(EXTRA_AP_MATERNO, apMat)
                    putExtra(EXTRA_FECHA_NAC, fechaNac)
                    putExtra(EXTRA_EMAIL, email)
                    putExtra(EXTRA_GENERO, gender)
                }
                startActivity(i)
            }
        }
    }

    private fun setupOnlyLettersFilters() {
        val lettersFilter = android.text.InputFilter { source, _, _, _, _, _ ->
            // Permite letras (incluye acentos), espacios y apóstrofe
            val regex = Regex("^[\\p{L} '\\u00C0-\\u017F]+$")
            if (source.isNullOrEmpty()) return@InputFilter null // borrar
            if (regex.matches(source)) null else ""
        }

        binding.etNombre.filters = arrayOf(lettersFilter)
        binding.etApellidoPaterno.filters = arrayOf(lettersFilter)
        binding.etApellidoMaterno.filters = arrayOf(lettersFilter)

        // Opcional: capitalizar palabras
        binding.etNombre.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
        binding.etApellidoPaterno.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
        binding.etApellidoMaterno.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
    }

    private fun setupGenderSelection() {
        fun selectGender(g: String) {
            selectedGender = g
            binding.tvGenderError.visibility = android.view.View.GONE

            if (g == "M") {
                binding.cardMale.strokeColor = selectedStrokeColor
                binding.cardFemale.strokeColor = ContextCompat.getColor(this, android.R.color.darker_gray)
            } else {
                binding.cardFemale.strokeColor = selectedStrokeColor
                binding.cardMale.strokeColor = ContextCompat.getColor(this, android.R.color.darker_gray)
            }

            refreshButton()
        }

        binding.cardMale.setOnClickListener { selectGender("M") }
        binding.cardFemale.setOnClickListener { selectGender("F") }
    }

    private fun setupDOBPicker() {
        binding.etFechaNac.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                val mm = (m + 1).toString().padStart(2, '0')
                val dd = d.toString().padStart(2, '0')
                binding.etFechaNac.setText("$dd/$mm/$y")
                binding.tilFechaNac.error = null
                refreshButton()
            }, year, month, day).show()
        }
    }

    private fun setupRealtimeValidation() {
        binding.etNombre.doAfterTextChanged { binding.tilNombre.error = null; refreshButton() }
        binding.etApellidoPaterno.doAfterTextChanged { binding.tilApellidoPaterno.error = null; refreshButton() }
        binding.etApellidoMaterno.doAfterTextChanged { binding.tilApellidoMaterno.error = null; refreshButton() }
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null; refreshButton() }

        refreshButton()
    }

    private fun refreshButton() {
        // Botón siempre activo; solo damos feedback visual si falta algo
        val canContinue = validateAll(showErrors = false)

        binding.btnContinuar.isEnabled = true
        binding.btnContinuar.alpha = if (canContinue) 1f else 0.75f
    }

    private fun validateAll(showErrors: Boolean): Boolean {
        var ok = true

        val nombre = binding.etNombre.text?.toString()?.trim().orEmpty()
        val apPat = binding.etApellidoPaterno.text?.toString()?.trim().orEmpty()
        val apMat = binding.etApellidoMaterno.text?.toString()?.trim().orEmpty()
        val fecha = binding.etFechaNac.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()

        if (nombre.isEmpty()) { ok = false; if (showErrors) binding.tilNombre.error = "Obligatorio" }
        if (apPat.isEmpty()) { ok = false; if (showErrors) binding.tilApellidoPaterno.error = "Obligatorio" }
        if (apMat.isEmpty()) { ok = false; if (showErrors) binding.tilApellidoMaterno.error = "Obligatorio" }
        if (fecha.isEmpty()) { ok = false; if (showErrors) binding.tilFechaNac.error = "Obligatorio" }

        val onlyLettersRegex = Regex("^[\\p{L} '\\u00C0-\\u017F]+$")

        fun validateNameField(value: String, til: com.google.android.material.textfield.TextInputLayout): Boolean {
            if (!onlyLettersRegex.matches(value)) {
                if (showErrors) til.error = "Solo letras"
                return false
            }
            return true
        }

        if (nombre.isNotEmpty() && !validateNameField(nombre, binding.tilNombre)) ok = false
        if (apPat.isNotEmpty() && !validateNameField(apPat, binding.tilApellidoPaterno)) ok = false
        if (apMat.isNotEmpty() && !validateNameField(apMat, binding.tilApellidoMaterno)) ok = false

        if (selectedGender == null) {
            ok = false
            if (showErrors) binding.tvGenderError.visibility = android.view.View.VISIBLE
        } else {
            if (showErrors) binding.tvGenderError.visibility = android.view.View.GONE
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ok = false
            if (showErrors) binding.tilEmail.error = "Correo inválido"
        }

        return ok
    }

    companion object {
        const val EXTRA_ROLE = "extra_role"
        const val ROLE_PRESTADOR = "PRESTADOR"
        const val ROLE_SOLICITANTE = "SOLICITANTE"
        private const val ROLE_NONE = "SIN_ROL"

        const val EXTRA_NOMBRE = "extra_nombre"
        const val EXTRA_AP_PATERNO = "extra_ap_paterno"
        const val EXTRA_AP_MATERNO = "extra_ap_materno"
        const val EXTRA_FECHA_NAC = "extra_fecha_nac"
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_GENERO = "extra_genero"
    }
}