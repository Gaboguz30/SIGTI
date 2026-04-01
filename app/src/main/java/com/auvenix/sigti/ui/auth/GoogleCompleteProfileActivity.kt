package com.auvenix.sigti.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.notifications.FcmTokenManager
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.register.RegisterGeneralActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GoogleCompleteProfileActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        completarRegistroGoogle()
    }

    private fun completarRegistroGoogle() {
        val currentUser = auth.currentUser
        val uid = intent.getStringExtra(RegisterGeneralActivity.EXTRA_GOOGLE_UID)
            ?: currentUser?.uid
            ?: ""

        val role = intent.getStringExtra(RegisterGeneralActivity.EXTRA_ROLE).orEmpty()
        val email = intent.getStringExtra(RegisterGeneralActivity.EXTRA_EMAIL)
            ?: intent.getStringExtra(RegisterGeneralActivity.EXTRA_GOOGLE_EMAIL)
            ?: currentUser?.email
            ?: ""

        if (uid.isBlank() || role.isBlank() || email.isBlank()) {
            Toast.makeText(this, "Faltan datos para completar el registro con Google", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val listaOficiosCruda = intent.getStringArrayListExtra("extra_oficios") ?: arrayListOf()
        val oficiosProcesados = listaOficiosCruda.map { item ->
            val parts = item.split("|")
            mapOf(
                "nombre" to (parts.getOrNull(0) ?: ""),
                "anios_experiencia" to (parts.getOrNull(1)?.toIntOrNull() ?: 0)
            )
        }

        val expediente = hashMapOf(
            "uid" to uid,
            "role" to role,
            "rol" to role,
            "email" to email,
            "nombre" to intent.getStringExtra(RegisterGeneralActivity.EXTRA_NOMBRE).orEmpty(),
            "apPaterno" to intent.getStringExtra(RegisterGeneralActivity.EXTRA_AP_PATERNO).orEmpty(),
            "apMaterno" to intent.getStringExtra(RegisterGeneralActivity.EXTRA_AP_MATERNO).orEmpty(),
            "fechaNac" to intent.getStringExtra(RegisterGeneralActivity.EXTRA_FECHA_NAC).orEmpty(),
            "genero" to intent.getStringExtra(RegisterGeneralActivity.EXTRA_GENERO).orEmpty(),
            "ciudad" to intent.getStringExtra("extra_ciudad").orEmpty(),
            "localidad" to intent.getStringExtra("extra_localidad").orEmpty(),
            "codigoPostal" to intent.getStringExtra("extra_codigo_postal").orEmpty(),
            "fotoFrontalUri" to intent.getStringExtra("extra_foto_frontal_uri").orEmpty(),
            "oficios" to oficiosProcesados,
            "plan_actual" to "FREE",
            "trabajos_realizados_mes" to 0,
            "online" to false,
            "notificaciones" to true,
            "metodoRegistro" to "GOOGLE",
            "fechaRegistro" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(uid)
            .set(expediente)
            .addOnSuccessListener {
                FcmTokenManager.saveCurrentToken()

                val nextIntent = if (role == RegisterGeneralActivity.ROLE_PRESTADOR) {
                    Intent(this, ProviderHomeActivity::class.java)
                } else {
                    Intent(this, HomeActivity::class.java)
                }

                nextIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(nextIntent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al completar perfil con Google: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
    }
}