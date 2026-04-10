package com.auvenix.sigti.ui.register

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivitySolicitanteExtraBinding
import com.auvenix.sigti.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import android.widget.TextView
import android.widget.ImageView
import com.auvenix.sigti.R

class SolicitanteExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitanteExtraBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 📸 URIs para guardar temporalmente las fotos seleccionadas en el teléfono
    private var uriIneFrontal: Uri? = null
    private var uriIneReverso: Uri? = null
    private var uriSelfie: Uri? = null

    // 🎯 Bandera para saber qué botón apretó el usuario ("FRONTAL", "REVERSO" o "SELFIE")
    private var tipoFotoSeleccionada = ""

    // 🖼️ El lanzador de la galería
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedUri = result.data?.data
            if (selectedUri != null) {
                // Dependiendo de qué botón apretó, guardamos la URI y cambiamos el textito a verde
                when (tipoFotoSeleccionada) {
                    "FRONTAL" -> {
                        uriIneFrontal = selectedUri
                        binding.tvIneFrontalStatus.text = "¡INE Frontal cargado! ✅"
                        binding.tvIneFrontalStatus.setTextColor(android.graphics.Color.parseColor("#16A34A")) // Verde
                    }
                    "REVERSO" -> {
                        uriIneReverso = selectedUri
                        binding.tvIneReversoStatus.text = "¡INE Reverso cargado! ✅"
                        binding.tvIneReversoStatus.setTextColor(android.graphics.Color.parseColor("#16A34A"))
                    }
                    "SELFIE" -> {
                        uriSelfie = selectedUri
                        binding.tvSelfieStatus.text = "¡Selfie cargada! ✅"
                        binding.tvSelfieStatus.setTextColor(android.graphics.Color.parseColor("#16A34A"))
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitanteExtraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val header = findViewById<View>(R.id.headerMain)

        val tvTitle = header.findViewById<TextView>(R.id.tvHeaderTitle)
        tvTitle.text = "Tu Perfil"

        val btnBack = header.findViewById<ImageView>(R.id.btnBackHeader)
        btnBack.setOnClickListener {
            finish()
        }

        setupListeners()
    }

    private fun setupListeners() {
        // 📸 Botones para abrir galería
        binding.btnUploadIneFrontal.setOnClickListener { abrirGaleria("FRONTAL") }
        binding.btnUploadIneReverso.setOnClickListener { abrirGaleria("REVERSO") }
        binding.btnUploadSelfie.setOnClickListener { abrirGaleria("SELFIE") }

        // 🚀 Botón Continuar
        binding.btnContinuarSolicitante.setOnClickListener {
            validarYSubirDatos()
        }
    }

    private fun abrirGaleria(tipo: String) {
        tipoFotoSeleccionada = tipo
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun validarYSubirDatos() {
        val ciudad = binding.etCiudad.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()

        if (ciudad.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "Por favor, completa tu ciudad y dirección.", Toast.LENGTH_SHORT).show()
            return
        }

        if (uriIneFrontal == null || uriIneReverso == null || uriSelfie == null) {
            Toast.makeText(this, "Debes subir las 3 fotos para continuar.", Toast.LENGTH_SHORT).show()
            return
        }

        // Bloqueamos el botón para evitar dobles clics
        binding.btnContinuarSolicitante.isEnabled = false
        binding.btnContinuarSolicitante.text = "Subiendo archivos... (Puede tardar)"

        // Empezamos la subida en cadena
        subirFoto(uriIneFrontal!!, "ine_frontal") { urlFrontal ->
            subirFoto(uriIneReverso!!, "ine_reverso") { urlReverso ->
                subirFoto(uriSelfie!!, "selfies") { urlSelfie ->

                    // Cuando las 3 estén listas, guardamos en la base de datos
                    guardarEnFirestore(ciudad, direccion, urlFrontal, urlReverso, urlSelfie)
                }
            }
        }
    }

    // 🔥 Función genérica que sube una foto a Storage y te devuelve el Link de descarga
    private fun subirFoto(uri: Uri, carpeta: String, onSuccess: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: UUID.randomUUID().toString()
        val nombreArchivo = "${System.currentTimeMillis()}.jpg"

        // Ruta en Firebase: ej. "solicitantes/12345/ine_frontal/162345.jpg"
        val ref = storage.reference.child("solicitantes").child(uid).child(carpeta).child(nombreArchivo)

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error subiendo foto de $carpeta", Toast.LENGTH_SHORT).show()
                binding.btnContinuarSolicitante.isEnabled = true
                binding.btnContinuarSolicitante.text = "Continuar"
            }
    }

    private fun guardarEnFirestore(ciudad: String, direccion: String, urlFrontal: String, urlReverso: String, urlSelfie: String) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "Error: No hay sesión iniciada", Toast.LENGTH_SHORT).show()
            return
        }

        val actualizaciones = mapOf(
            "ciudad" to ciudad,
            "direccion" to direccion,
            "url_ine_frontal" to urlFrontal,
            "url_ine_reverso" to urlReverso,
            "url_selfie" to urlSelfie,
            "perfil_completado" to true // Bandera útil por si quieres saber si ya terminó todo el proceso
        )

        db.collection("users").document(uid)
            .update(actualizaciones)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Perfil configurado con éxito!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar los datos finales", Toast.LENGTH_SHORT).show()
                binding.btnContinuarSolicitante.isEnabled = true
                binding.btnContinuarSolicitante.text = "Continuar"
            }
    }
}