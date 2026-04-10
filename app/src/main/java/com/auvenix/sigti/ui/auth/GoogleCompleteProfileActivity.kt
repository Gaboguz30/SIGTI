package com.auvenix.sigti.ui.auth

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityGoogleCompleteProfileBinding
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.register.PrestadorExtraActivity
import com.auvenix.sigti.ui.register.SolicitanteExtraActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class GoogleCompleteProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoogleCompleteProfileBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleCompleteProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Recibir datos (Google + Rol elegido en la pantalla anterior)
        val nombreGoogle = intent.getStringExtra("EXTRA_NOMBRE_COMPLETO") ?: ""
        // Lo pasamos a mayúsculas por seguridad (SOLICITANTE o PRESTADOR)
        val rolElegido = intent.getStringExtra("EXTRA_ROL")?.uppercase() ?: "SOLICITANTE"

        // 2. Separar nombre en los 3 campos
        separarNombrePro(nombreGoogle)

        // 3. Calendario
        binding.etFechaNac.setOnClickListener { mostrarCalendario() }

        // 4. Guardado final
        binding.btnFinalizar.setOnClickListener {
            guardarEnFirestore(rolElegido)
        }
    }

    private fun separarNombrePro(cadena: String) {
        val palabras = cadena.trim().split(" ").filter { it.isNotEmpty() }
        when {
            palabras.size == 1 -> binding.etNombre.setText(palabras[0])
            palabras.size == 2 -> {
                binding.etNombre.setText(palabras[0])
                binding.etApPaterno.setText(palabras[1])
            }
            palabras.size == 3 -> {
                binding.etNombre.setText(palabras[0])
                binding.etApPaterno.setText(palabras[1])
                binding.etApMaterno.setText(palabras[2])
            }
            palabras.size >= 4 -> {
                binding.etNombre.setText("${palabras[0]} ${palabras[1]}")
                binding.etApPaterno.setText(palabras[2])
                binding.etApMaterno.setText(palabras.subList(3, palabras.size).joinToString(" "))
            }
        }
    }

    private fun mostrarCalendario() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            binding.etFechaNac.setText("$d/${m + 1}/$y")
        }, c.get(Calendar.YEAR) - 20, c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun guardarEnFirestore(rol: String) {
        val uid = intent.getStringExtra("EXTRA_UID") ?: return
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        val nombre = binding.etNombre.text.toString().trim()
        val paterno = binding.etApPaterno.text.toString().trim()
        val materno = binding.etApMaterno.text.toString().trim()
        val fechaNac = binding.etFechaNac.text.toString()

        if (nombre.isEmpty() || paterno.isEmpty() || fechaNac.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar carga y bloquear botón
        binding.btnFinalizar.text = ""
        binding.pbLoading.visibility = View.VISIBLE
        binding.btnFinalizar.isEnabled = false

        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "nombre" to nombre,
            "apPaterno" to paterno,
            "apMaterno" to materno,
            "fechaNac" to fechaNac,
            "role" to rol, // El rol que venía de la pantalla anterior
            "metodoRegistro" to "Google",
            "perfil_completado" to false, // 🔥 Aún no termina, le faltan las fotos
            "fechaRegistro" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid).set(userMap)
            .addOnSuccessListener {

                // 🔥 AQUÍ CONECTAMOS EL PUENTE A LA PANTALLA DE FOTOS
                val intent = if (rol == "PRESTADOR") {
                    Intent(this, PrestadorExtraActivity::class.java)
                } else {
                    Intent(this, SolicitanteExtraActivity::class.java)
                }

                // Cerramos esta pantalla para que no pueda regresar con el botón de atrás
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                binding.btnFinalizar.text = "Finalizar Registro"
                binding.pbLoading.visibility = View.GONE
                binding.btnFinalizar.isEnabled = true
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}