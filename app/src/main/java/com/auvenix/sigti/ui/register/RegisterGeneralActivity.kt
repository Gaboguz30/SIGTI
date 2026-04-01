package com.auvenix.sigti.ui.register

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityRegisterGeneralBinding
import com.auvenix.sigti.utils.Validators
import java.util.Calendar

class RegisterGeneralActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterGeneralBinding
    private var selectedGender: String? = null

    private var isGoogleUser: Boolean = false
    private var googleUid: String? = null
    private var googleName: String? = null
    private var googleEmail: String? = null
    private var googlePhotoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterGeneralBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val role = intent.getStringExtra(EXTRA_ROLE)

        if (role != ROLE_PRESTADOR && role != ROLE_SOLICITANTE) {
            Toast.makeText(this, "No se recibió un rol válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        readGoogleExtras()
        setupGenderSelection()
        setupDOBPicker()
        setupRealtimeValidation()
        setupNameFilters()
        prefillIfGoogleUser()

        binding.btnContinuar.setOnClickListener {
            if (!validateAll(showErrors = true)) {
                Toast.makeText(this, "Revisa los campos marcados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nextIntent = if (role == ROLE_PRESTADOR) {
                Intent(this, PrestadorExtraActivity::class.java)
            } else {
                Intent(this, SolicitanteExtraActivity::class.java)
            }

            nextIntent.apply {
                putExtra(EXTRA_ROLE, role)
                putExtra(EXTRA_NOMBRE, binding.etNombre.text.toString().trim())
                putExtra(EXTRA_AP_PATERNO, binding.etApellidoPaterno.text.toString().trim())
                putExtra(EXTRA_AP_MATERNO, binding.etApellidoMaterno.text.toString().trim())
                putExtra(EXTRA_FECHA_NAC, binding.etFechaNac.text.toString().trim())
                putExtra(EXTRA_EMAIL, binding.etEmail.text.toString().trim().lowercase())
                putExtra(EXTRA_GENERO, selectedGender.orEmpty())

                putExtra(EXTRA_IS_GOOGLE, isGoogleUser)
                putExtra(EXTRA_GOOGLE_UID, googleUid)
                putExtra(EXTRA_GOOGLE_NAME, googleName)
                putExtra(EXTRA_GOOGLE_EMAIL, googleEmail)
                putExtra(EXTRA_GOOGLE_PHOTO_URL, googlePhotoUrl)
            }

            startActivity(nextIntent)
        }
    }

    private fun readGoogleExtras() {
        isGoogleUser = intent.getBooleanExtra(EXTRA_IS_GOOGLE, false)
        googleUid = intent.getStringExtra(EXTRA_GOOGLE_UID)
        googleName = intent.getStringExtra(EXTRA_GOOGLE_NAME)
        googleEmail = intent.getStringExtra(EXTRA_GOOGLE_EMAIL)
        googlePhotoUrl = intent.getStringExtra(EXTRA_GOOGLE_PHOTO_URL)
    }

    private fun prefillIfGoogleUser() {
        if (!isGoogleUser) return

        googleEmail?.trim()?.takeIf { it.isNotBlank() }?.let { email ->
            if (binding.etEmail.text.isNullOrBlank()) {
                binding.etEmail.setText(email)
            }
        }

        googleName?.trim()?.takeIf { it.isNotBlank() }?.let { fullName ->
            val parts = fullName.split(Regex("\\s+")).filter { it.isNotBlank() }

            when (parts.size) {
                1 -> {
                    if (binding.etNombre.text.isNullOrBlank()) binding.etNombre.setText(parts[0])
                }
                2 -> {
                    if (binding.etNombre.text.isNullOrBlank()) binding.etNombre.setText(parts[0])
                    if (binding.etApellidoPaterno.text.isNullOrBlank()) binding.etApellidoPaterno.setText(parts[1])
                }
                3 -> {
                    if (binding.etNombre.text.isNullOrBlank()) binding.etNombre.setText(parts[0])
                    if (binding.etApellidoPaterno.text.isNullOrBlank()) binding.etApellidoPaterno.setText(parts[1])
                    if (binding.etApellidoMaterno.text.isNullOrBlank()) binding.etApellidoMaterno.setText(parts[2])
                }
                else -> {
                    val nombre = parts.dropLast(2).joinToString(" ")
                    val apPaterno = parts[parts.size - 2]
                    val apMaterno = parts[parts.size - 1]

                    if (binding.etNombre.text.isNullOrBlank()) binding.etNombre.setText(nombre)
                    if (binding.etApellidoPaterno.text.isNullOrBlank()) binding.etApellidoPaterno.setText(apPaterno)
                    if (binding.etApellidoMaterno.text.isNullOrBlank()) binding.etApellidoMaterno.setText(apMaterno)
                }
            }
        }
    }

    private fun setupNameFilters() {
        val lettersAndSpacesFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.isNullOrEmpty()) return@InputFilter null
            if (source.all { it.isLetter() || it == ' ' }) null else ""
        }

        val capWords = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS

        binding.etNombre.filters = arrayOf(lettersAndSpacesFilter, InputFilter.LengthFilter(20))
        binding.etApellidoPaterno.filters = arrayOf(lettersAndSpacesFilter, InputFilter.LengthFilter(15))
        binding.etApellidoMaterno.filters = arrayOf(lettersAndSpacesFilter, InputFilter.LengthFilter(15))

        binding.etNombre.inputType = capWords
        binding.etApellidoPaterno.inputType = capWords
        binding.etApellidoMaterno.inputType = capWords
        binding.etEmail.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    }

    private fun setupDOBPicker() {
        binding.etFechaNac.setOnClickListener { showDrumDatePicker() }
        binding.tilFechaNac.setEndIconOnClickListener { showDrumDatePicker() }
    }

    private fun showDrumDatePicker() {
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance()
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_date_picker)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )

        val npDay = dialog.findViewById<NumberPicker>(R.id.npDay)
        val npMonth = dialog.findViewById<NumberPicker>(R.id.npMonth)
        val npYear = dialog.findViewById<NumberPicker>(R.id.npYear)

        npDay.minValue = 1
        npDay.maxValue = 31
        npDay.value = cal.get(Calendar.DAY_OF_MONTH)

        val meses = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        npMonth.minValue = 0
        npMonth.maxValue = 11
        npMonth.displayedValues = meses
        npMonth.value = cal.get(Calendar.MONTH)

        val yearMax = today.get(Calendar.YEAR) - 18
        npYear.minValue = 1920
        npYear.maxValue = yearMax
        npYear.value = yearMax - 7

        fun updateMaxDay() {
            cal.set(Calendar.MONTH, npMonth.value)
            cal.set(Calendar.YEAR, npYear.value)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            npDay.maxValue = maxDay
            if (npDay.value > maxDay) npDay.value = maxDay
        }

        npMonth.setOnValueChangedListener { _, _, _ -> updateMaxDay() }
        npYear.setOnValueChangedListener { _, _, _ -> updateMaxDay() }

        dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelar)
            .setOnClickListener { dialog.dismiss() }

        dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAceptar)
            .setOnClickListener {
                val dateStr = "${npDay.value.toString().padStart(2, '0')}/${(npMonth.value + 1).toString().padStart(2, '0')}/${npYear.value}"
                binding.etFechaNac.setText(dateStr)
                binding.tilFechaNac.error = null
                refreshButton()
                dialog.dismiss()
            }

        dialog.show()
    }

    private fun setupGenderSelection() {
        fun select(gender: String) {
            selectedGender = gender
            binding.llGenderError.visibility = android.view.View.GONE

            val selectedColor = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
            val defaultColor = ContextCompat.getColor(this, android.R.color.darker_gray)

            binding.cardMale.strokeColor = if (gender == "M") selectedColor else defaultColor
            binding.cardFemale.strokeColor = if (gender == "F") selectedColor else defaultColor

            refreshButton()
        }

        binding.cardMale.setOnClickListener { select("M") }
        binding.cardFemale.setOnClickListener { select("F") }
    }

    private fun setupRealtimeValidation() {
        binding.etNombre.doAfterTextChanged {
            binding.tilNombre.error = null
            refreshButton()
        }
        binding.etApellidoPaterno.doAfterTextChanged {
            binding.tilApellidoPaterno.error = null
            refreshButton()
        }
        binding.etApellidoMaterno.doAfterTextChanged {
            binding.tilApellidoMaterno.error = null
            refreshButton()
        }
        binding.etEmail.doAfterTextChanged {
            binding.tilEmail.error = null
            refreshButton()
        }
        refreshButton()
    }

    private fun refreshButton() {
        binding.btnContinuar.isEnabled = true
        binding.btnContinuar.alpha = if (validateAll(false)) 1f else 0.75f
    }

    private fun validateAll(showErrors: Boolean): Boolean {
        var ok = true

        val nombre = binding.etNombre.text?.toString()?.trim().orEmpty()
        val apPat = binding.etApellidoPaterno.text?.toString()?.trim().orEmpty()
        val apMat = binding.etApellidoMaterno.text?.toString()?.trim().orEmpty()
        val fecha = binding.etFechaNac.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()

        if (nombre.isEmpty()) {
            ok = false
            if (showErrors) binding.tilNombre.error = "El nombre es obligatorio"
        }

        if (apPat.isEmpty()) {
            ok = false
            if (showErrors) binding.tilApellidoPaterno.error = "El apellido paterno es obligatorio"
        }

        if (apMat.isEmpty()) {
            ok = false
            if (showErrors) binding.tilApellidoMaterno.error = "El apellido materno es obligatorio"
        }

        if (fecha.isEmpty()) {
            ok = false
            if (showErrors) binding.tilFechaNac.error = "La fecha de nacimiento es obligatoria"
        }

        fun validateNameField(
            value: String,
            maxLength: Int,
            til: com.google.android.material.textfield.TextInputLayout,
            isSurname: Boolean = false
        ) {
            if (value.isEmpty()) return

            val result = if (isSurname) {
                Validators.validateSurname(value, maxLength)
            } else {
                Validators.validateName(value, maxLength)
            }

            if (result != Validators.NameResult.Ok) {
                ok = false
                if (showErrors) til.error = result.message()
            }
        }

        validateNameField(nombre, 20, binding.tilNombre, false)
        validateNameField(apPat, 15, binding.tilApellidoPaterno, true)
        validateNameField(apMat, 15, binding.tilApellidoMaterno, true)

        val emailResult = Validators.validateEmail(email)
        if (emailResult != Validators.EmailResult.Ok) {
            ok = false
            if (showErrors) binding.tilEmail.error = emailResult.message()
        }

        if (selectedGender == null) {
            ok = false
            if (showErrors) binding.llGenderError.visibility = android.view.View.VISIBLE
        }

        if (fecha.isNotEmpty() && !Validators.isAtLeast18(fecha)) {
            ok = false
            if (showErrors) binding.tilFechaNac.error = "Debes tener al menos 18 años"
        }

        return ok
    }

    companion object {
        const val EXTRA_ROLE = "extra_role"
        const val ROLE_PRESTADOR = "PRESTADOR"
        const val ROLE_SOLICITANTE = "SOLICITANTE"

        const val EXTRA_NOMBRE = "extra_nombre"
        const val EXTRA_AP_PATERNO = "extra_ap_paterno"
        const val EXTRA_AP_MATERNO = "extra_ap_materno"
        const val EXTRA_FECHA_NAC = "extra_fecha_nac"
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_GENERO = "extra_genero"

        const val EXTRA_IS_GOOGLE = "extra_is_google"
        const val EXTRA_GOOGLE_UID = "extra_google_uid"
        const val EXTRA_GOOGLE_NAME = "extra_google_name"
        const val EXTRA_GOOGLE_EMAIL = "extra_google_email"
        const val EXTRA_GOOGLE_PHOTO_URL = "extra_google_photo_url"
    }
}