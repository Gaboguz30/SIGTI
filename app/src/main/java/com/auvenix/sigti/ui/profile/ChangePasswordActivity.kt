package com.auvenix.sigti.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.databinding.ActivityChangePasswordBinding
import com.auvenix.sigti.utils.Validators // 🔥 IMPORTANTE: Este es el import que faltaba
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.header.tvHeaderTitle.text = "Cambiar Contraseña"

        binding.header.btnBackHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Botón de confirmar el cambio
        binding.btnConfirmChange.setOnClickListener {
            validarYCambiar()
        }
    }

    private fun validarYCambiar() {
        val currentPass = binding.etCurrentPassword.text.toString().trim()
        val newPass = binding.etNewPassword.text.toString().trim()
        val confirmPass = binding.etConfirmNewPassword.text.toString().trim()

        // 1. Validar Contraseña Actual
        if (currentPass.isEmpty()) {
            binding.etCurrentPassword.error = "Ingresa tu contraseña actual"
            return
        } else {
            binding.etCurrentPassword.error = null
        }

        // 2. Usar nuestro validador centralizado para la NUEVA contraseña
        val passValidation = Validators.validatePassword(newPass)
        if (passValidation != Validators.PasswordResult.Ok) {
            binding.etNewPassword.error = passValidation.message()
            return
        } else {
            binding.etNewPassword.error = null
        }

        // 3. Validar que las contraseñas nuevas coincidan
        if (newPass != confirmPass) {
            binding.etConfirmNewPassword.error = "Las contraseñas no coinciden"
            return
        } else {
            binding.etConfirmNewPassword.error = null
        }

        val user = auth.currentUser
        if (user != null && user.email != null) {

            binding.btnConfirmChange.isEnabled = false
            binding.btnConfirmChange.text = "Validando identidad..."

            val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.btnConfirmChange.text = "Actualizando..."

                    user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(this, "¡Contraseña actualizada con éxito!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Error al actualizar. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                            restaurarBoton()
                        }
                    }
                } else {
                    binding.etCurrentPassword.error = "Contraseña incorrecta"
                    Toast.makeText(this, "La contraseña actual no es correcta", Toast.LENGTH_SHORT).show()
                    restaurarBoton()
                }
            }
        } else {
            Toast.makeText(this, "Error de sesión. Vuelve a iniciar sesión.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restaurarBoton() {
        binding.btnConfirmChange.isEnabled = true
        binding.btnConfirmChange.text = "Actualizar Contraseña"
    }
}