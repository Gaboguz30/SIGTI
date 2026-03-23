package com.auvenix.sigti.ui.register

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityRegisterGeneralBinding
import java.util.Calendar

class RegisterGeneralActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterGeneralBinding
    private var selectedGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterGeneralBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val role = intent.getStringExtra(EXTRA_ROLE) ?: ROLE_NONE

        setupGenderSelection()
        setupDOBPicker()
        setupRealtimeValidation()
        setupNameFilters()

        binding.btnContinuar.isEnabled  = true
        binding.btnContinuar.isClickable = true

        binding.btnContinuar.setOnClickListener {
            if (!validateAll(showErrors = true)) {
                Toast.makeText(this, "Revisa los campos marcados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val baseExtras = Intent().apply {
                putExtra(EXTRA_ROLE,       role)
                putExtra(EXTRA_NOMBRE,     binding.etNombre.text.toString().trim())
                putExtra(EXTRA_AP_PATERNO, binding.etApellidoPaterno.text.toString().trim())
                putExtra(EXTRA_AP_MATERNO, binding.etApellidoMaterno.text.toString().trim())
                putExtra(EXTRA_FECHA_NAC,  binding.etFechaNac.text.toString().trim())
                putExtra(EXTRA_EMAIL,      binding.etEmail.text.toString().trim())
                putExtra(EXTRA_GENERO,     selectedGender!!)
            }

            val destino = if (role == ROLE_PRESTADOR)
                PrestadorExtraActivity::class.java
            else
                SolicitanteExtraActivity::class.java

            startActivity(Intent(this, destino).putExtras(baseExtras))
        }
    }

    private fun setupNameFilters() {
        val lettersFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.isNullOrEmpty()) return@InputFilter null   // permite borrar

            val valid = source.all { char ->
                char.isLetter()     // incluye á é í ó ú ü ñ Ñ y cualquier letra Unicode
                        || char == ' '
                        || char == '\''
                        || char == 'ñ'
                        || char == 'Ñ'
            }
            if (valid) null else ""
        }

        val capWords = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS

        binding.etNombre.filters          = arrayOf(lettersFilter)
        binding.etApellidoPaterno.filters = arrayOf(lettersFilter)
        binding.etApellidoMaterno.filters = arrayOf(lettersFilter)

        binding.etNombre.inputType          = capWords
        binding.etApellidoPaterno.inputType = capWords
        binding.etApellidoMaterno.inputType = capWords
    }

    private fun setupDOBPicker() {
        binding.etFechaNac.setOnClickListener { showDrumDatePicker() }
        binding.tilFechaNac.setEndIconOnClickListener { showDrumDatePicker() }
    }

    private fun showDrumDatePicker() {
        val cal   = Calendar.getInstance()
        val today = Calendar.getInstance()
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_date_picker)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        val npDay   = dialog.findViewById<NumberPicker>(R.id.npDay)
        val npMonth = dialog.findViewById<NumberPicker>(R.id.npMonth)
        val npYear  = dialog.findViewById<NumberPicker>(R.id.npYear)

        npDay.minValue = 1; npDay.maxValue = 31; npDay.value = cal.get(Calendar.DAY_OF_MONTH)
        val meses = arrayOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
        npMonth.minValue = 0; npMonth.maxValue = 11; npMonth.displayedValues = meses; npMonth.value = cal.get(Calendar.MONTH)
        val yearMax = today.get(Calendar.YEAR)
        npYear.minValue = 1920; npYear.maxValue = yearMax; npYear.value = yearMax - 20

        fun updateMaxDay() {
            cal.set(Calendar.MONTH, npMonth.value); cal.set(Calendar.YEAR, npYear.value)
            val m = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            npDay.maxValue = m; if (npDay.value > m) npDay.value = m
        }
        npMonth.setOnValueChangedListener { _, _, _ -> updateMaxDay() }
        npYear.setOnValueChangedListener  { _, _, _ -> updateMaxDay() }

        dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelar)
            .setOnClickListener { dialog.dismiss() }
        dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAceptar)
            .setOnClickListener {
                binding.etFechaNac.setText("${npDay.value.toString().padStart(2,'0')}/${(npMonth.value+1).toString().padStart(2,'0')}/${npYear.value}")
                binding.tilFechaNac.error = null; refreshButton(); dialog.dismiss()
            }
        dialog.show()
    }

    private fun setupGenderSelection() {
        fun sel(g: String) {
            selectedGender = g
            // Ocultar error de género
            binding.llGenderError.visibility = android.view.View.GONE
            val on  = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
            val off = ContextCompat.getColor(this, android.R.color.darker_gray)
            binding.cardMale.strokeColor   = if (g == "M") on else off
            binding.cardFemale.strokeColor = if (g == "F") on else off
            refreshButton()
        }
        binding.cardMale.setOnClickListener { sel("M") }
        binding.cardFemale.setOnClickListener { sel("F") }
    }

    private fun setupRealtimeValidation() {
        binding.etNombre.doAfterTextChanged          { binding.tilNombre.error = null; refreshButton() }
        binding.etApellidoPaterno.doAfterTextChanged { binding.tilApellidoPaterno.error = null; refreshButton() }
        binding.etApellidoMaterno.doAfterTextChanged { binding.tilApellidoMaterno.error = null; refreshButton() }
        binding.etEmail.doAfterTextChanged           { binding.tilEmail.error = null; refreshButton() }
        refreshButton()
    }

    private fun refreshButton() {
        binding.btnContinuar.isEnabled = true
        binding.btnContinuar.alpha = if (validateAll(false)) 1f else 0.75f
    }

    private fun validateAll(showErrors: Boolean): Boolean {
        var ok = true
        val nombre = binding.etNombre.text?.toString()?.trim().orEmpty()
        val apPat  = binding.etApellidoPaterno.text?.toString()?.trim().orEmpty()
        val apMat  = binding.etApellidoMaterno.text?.toString()?.trim().orEmpty()
        val fecha  = binding.etFechaNac.text?.toString()?.trim().orEmpty()
        val email  = binding.etEmail.text?.toString()?.trim().orEmpty()

        if (nombre.isEmpty()) { ok = false; if (showErrors) binding.tilNombre.error = "Obligatorio" }
        if (apPat.isEmpty())  { ok = false; if (showErrors) binding.tilApellidoPaterno.error = "Obligatorio" }
        if (apMat.isEmpty())  { ok = false; if (showErrors) binding.tilApellidoMaterno.error = "Obligatorio" }
        if (fecha.isEmpty())  { ok = false; if (showErrors) binding.tilFechaNac.error = "Obligatorio" }

        fun validateName(v: String, til: com.google.android.material.textfield.TextInputLayout): Boolean {
            val valid = v.all { it.isLetter() || it == ' ' || it == '\'' || it == 'ñ' || it == 'Ñ' }
            return if (!valid) { if (showErrors) til.error = "Solo letras"; false } else true
        }
        if (nombre.isNotEmpty() && !validateName(nombre, binding.tilNombre))          ok = false
        if (apPat.isNotEmpty()  && !validateName(apPat,  binding.tilApellidoPaterno)) ok = false
        if (apMat.isNotEmpty()  && !validateName(apMat,  binding.tilApellidoMaterno)) ok = false

        if (selectedGender == null) {
            ok = false
            if (showErrors) binding.llGenderError.visibility = android.view.View.VISIBLE
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ok = false; if (showErrors) binding.tilEmail.error = "Correo inválido"
        }
        return ok
    }

    companion object {
        const val EXTRA_ROLE       = "extra_role"
        const val ROLE_PRESTADOR   = "PRESTADOR"
        const val ROLE_SOLICITANTE = "SOLICITANTE"
        private const val ROLE_NONE = "SIN_ROL"
        const val EXTRA_NOMBRE     = "extra_nombre"
        const val EXTRA_AP_PATERNO = "extra_ap_paterno"
        const val EXTRA_AP_MATERNO = "extra_ap_materno"
        const val EXTRA_FECHA_NAC  = "extra_fecha_nac"
        const val EXTRA_EMAIL      = "extra_email"
        const val EXTRA_GENERO     = "extra_genero"
    }
}