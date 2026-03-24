package com.auvenix.sigti.ui.provider.catalog

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityAddServiceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddServiceBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var isEditing = false
    private var serviceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificamos si venimos a editar
        checkIfEditing()

        // Botón de cancelar de la tarjeta
        binding.btnCancelService.setOnClickListener { finish() }

        // Botón de guardar de la tarjeta
        binding.btnSaveService.setOnClickListener { validateAndSave() }
    }

    private fun checkIfEditing() {
        serviceId = intent.getStringExtra("EXTRA_SERVICE_ID")
        val name = intent.getStringExtra("EXTRA_SERVICE_NAME")
        val desc = intent.getStringExtra("EXTRA_SERVICE_DESC")
        val price = intent.getDoubleExtra("EXTRA_SERVICE_PRICE", -1.0)

        if (serviceId != null && name != null) {
            isEditing = true
            binding.tvCardTitle.text = "Modificar Detalles"
            binding.etServiceName.setText(name)
            binding.etServiceDesc.setText(desc)
            if (price != -1.0) binding.etServicePrice.setText(price.toString())
        } else {
            binding.tvCardTitle.text = "Detalles del Servicio"
        }
    }

    private fun validateAndSave() {
        val name = binding.etServiceName.text.toString().trim()
        val desc = binding.etServiceDesc.text.toString().trim()
        val priceStr = binding.etServicePrice.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        var hasError = false

        if (name.isEmpty()) {
            binding.tilServiceName.error = "Campo obligatorio"
            hasError = true
        } else { binding.tilServiceName.error = null }

        if (desc.isEmpty()) {
            binding.tilServiceDesc.error = "Agrega una descripción"
            hasError = true
        } else { binding.tilServiceDesc.error = null }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.tilServicePrice.error = "Precio inválido"
            hasError = true
        } else { binding.tilServicePrice.error = null }

        if (hasError) return

        // Mostramos estado de carga
        binding.btnSaveService.isEnabled = false
        binding.btnSaveService.text = "Guardando..."

        val serviceData = hashMapOf(
            "name" to name,
            "description" to desc,
            "price" to price
        )

        // Referencia a la subcolección "services" en inglés
        val servicesRef = db.collection("users").document(uid).collection("services")

        if (isEditing) {
            // ACTUALIZAR DOCUMENTO EXISTENTE
            servicesRef.document(serviceId!!).update(serviceData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Servicio actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.btnSaveService.isEnabled = true
                    binding.btnSaveService.text = "Guardar"
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        } else {
            // CREAR NUEVO DOCUMENTO
            servicesRef.add(serviceData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Servicio creado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.btnSaveService.isEnabled = true
                    binding.btnSaveService.text = "Guardar"
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}