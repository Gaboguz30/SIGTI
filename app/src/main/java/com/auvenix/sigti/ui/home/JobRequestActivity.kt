package com.auvenix.sigti.ui.request

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityJobRequestBinding
import com.auvenix.sigti.ui.home.HomeActivity
import java.util.*

class JobRequest : AppCompatActivity() {

    private lateinit var binding: ActivityJobRequestBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupIntentData()
        setupListeners()
    }

    private fun setupIntentData() {
        val workerName = intent.getStringExtra("WORKER_NAME") ?: "Prestador"
        val workerCategory = intent.getStringExtra("WORKER_CATEGORY") ?: "Servicios"

        binding.tvWorkerInfo.text = "$workerName ($workerCategory)"
    }

    private fun setupListeners() {
        // Botón Regresar
        binding.btnBack.setOnClickListener { finish() }

        // Selección de Fecha
        binding.etDate.setOnClickListener { showDatePicker() }

        // Botón Enviar Solicitud
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                sendJobRequest()
            }
        }

        // Barra inferior (Navegación futura)
        binding.navHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        binding.navChat.setOnClickListener {
            Toast.makeText(this, "Navegando a Mensajes...", Toast.LENGTH_SHORT).show()
        }
        // ... Repetir para los demás iconos según necesites
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                binding.etDate.setText(selectedDate)
                binding.tilDate.error = null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val fields = listOf(
            binding.tilServiceType to binding.etServiceType,
            binding.tilDate to binding.etDate,
            binding.tilStreet to binding.etStreet,
            binding.tilExtNum to binding.etExtNum,
            binding.tilPostalCode to binding.etPostalCode,
            binding.tilColony to binding.etColony,
            binding.tilBudget to binding.etBudget,
            binding.tilDescription to binding.etDescription
        )

        for ((layout, editText) in fields) {
            if (editText?.text.isNullOrBlank()) {
                layout.error = "Campo obligatorio"
                isValid = false
            } else {
                layout.error = null
            }
        }

        return isValid
    }

    private fun sendJobRequest() {
        // Aquí iría la lógica de Firebase o Retrofit
        Toast.makeText(this, "¡Solicitud enviada con éxito!", Toast.LENGTH_LONG).show()
        finish()
    }
}