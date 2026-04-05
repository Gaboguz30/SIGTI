package com.auvenix.sigti.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityMyDataBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyDataBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.header.btnBackHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.header.tvHeaderTitle.text = "Información Personal"

        cargarDatosUsuario()

        binding.btnGuardarDatos.setOnClickListener {
            guardarNuevosDatos()
        }
    }

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val rol = doc.getString("role") ?: "SOLICITANTE"

                // 🔥 LLAVES CORREGIDAS SEGÚN TU BD
                binding.etNombre.setText(doc.getString("nombre") ?: "")
                binding.etApellidoPaterno.setText(doc.getString("apPaterno") ?: "")
                binding.etApellidoMaterno.setText(doc.getString("apMaterno") ?: "")
                binding.etCorreo.setText(doc.getString("email") ?: "")
                binding.etFechaNacimiento.setText(doc.getString("fechaNac") ?: "No registrada")

                if (rol == "PRESTADOR") {
                    configurarModoPrestador(uid, doc.getString("plan_actual") ?: "FREE")
                } else {
                    configurarModoSolicitante()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarModoPrestador(uid: String, plan: String) {
        binding.etNombre.isEnabled = false
        binding.etApellidoPaterno.isEnabled = false
        binding.etApellidoMaterno.isEnabled = false

        binding.btnGuardarDatos.visibility = View.GONE
        binding.llPrestadorData.visibility = View.VISIBLE
        binding.etPlan.setText(plan)

        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            val oficiosArr = doc.get("oficios") as? List<Map<String, Any>>
            val nombresOficios = oficiosArr?.map { it["nombre"].toString() }?.joinToString(", ") ?: "Sin oficios"
            binding.etOficios.setText(nombresOficios)
        }
    }

    private fun configurarModoSolicitante() {
        binding.etNombre.isEnabled = true
        binding.etApellidoPaterno.isEnabled = true
        binding.etApellidoMaterno.isEnabled = true
        binding.btnGuardarDatos.visibility = View.VISIBLE
        binding.llPrestadorData.visibility = View.GONE
    }

    private fun guardarNuevosDatos() {
        val uid = auth.currentUser?.uid ?: return

        val nombre = binding.etNombre.text.toString().trim()
        val apPaterno = binding.etApellidoPaterno.text.toString().trim()
        val apMaterno = binding.etApellidoMaterno.text.toString().trim()

        if (nombre.isEmpty() || apPaterno.isEmpty()) {
            Toast.makeText(this, "El nombre y apellido paterno son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnGuardarDatos.isEnabled = false
        binding.btnGuardarDatos.text = "Guardando..."

        val updates = mapOf(
            "nombre" to nombre,
            "apPaterno" to apPaterno,
            "apMaterno" to apMaterno
        )

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                binding.btnGuardarDatos.isEnabled = true
                binding.btnGuardarDatos.text = "Guardar Cambios"
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }
}