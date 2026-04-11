package com.auvenix.sigti.ui.register

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityConfigurarFotoBinding
import com.auvenix.sigti.ui.auth.PasswordActivity
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ConfigurarFotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigurarFotoBinding
    private var uriFotoSeleccionada: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            uriFotoSeleccionada = result.data?.data
            binding.imgFotoPerfil.setImageURI(uriFotoSeleccionada)
            binding.btnGuardarFoto.text = "Continuar"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurarFotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnElegirFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.btnGuardarFoto.setOnClickListener {
            if (uriFotoSeleccionada == null) {
                Toast.makeText(this, "Por favor elige una foto para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isGoogle = intent.getBooleanExtra("EXTRA_IS_GOOGLE", false)

            if (isGoogle) {
                subirFotoParaGoogle()
            } else {
                val intentPassword = Intent(this, PasswordActivity::class.java)
                val extrasQueLlegaron = intent.extras
                if (extrasQueLlegaron != null) {
                    intentPassword.putExtras(extrasQueLlegaron)
                }
                intentPassword.putExtra("extra_url_selfie", uriFotoSeleccionada.toString())
                startActivity(intentPassword)
            }
        }
    }

    private fun subirFotoParaGoogle() {
        val uid = auth.currentUser?.uid ?: return

        binding.btnGuardarFoto.isEnabled = false
        binding.btnGuardarFoto.text = "Configurando perfil..."

        val ref = storage.reference.child("profile_pictures").child("$uid.jpg")

        ref.putFile(uriFotoSeleccionada!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { urlDescarga ->
                    // 🔥 COLECCIÓN LIMPIA "users"
                    db.collection("users").document(uid)
                        .update(
                            mapOf(
                                "url_selfie" to urlDescarga.toString(),
                                "perfil_completado" to true
                            )
                        )
                        .addOnSuccessListener {
                            redirigirAlHomeGoogle()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir foto", Toast.LENGTH_SHORT).show()
                binding.btnGuardarFoto.isEnabled = true
                binding.btnGuardarFoto.text = "Continuar"
            }
    }

    private fun redirigirAlHomeGoogle() {
        val rol = intent.getStringExtra("EXTRA_ROLE") ?: "SOLICITANTE"
        Toast.makeText(this, "¡Bienvenido a SIGTI!", Toast.LENGTH_SHORT).show()

        val intentHome = if (rol == "PRESTADOR") {
            Intent(this, ProviderHomeActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }

        intentHome.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intentHome)
        finish()
    }
}