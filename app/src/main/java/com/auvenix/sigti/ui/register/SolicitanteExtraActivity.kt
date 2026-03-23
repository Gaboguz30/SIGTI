package com.auvenix.sigti.ui.register

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.auvenix.sigti.databinding.ActivitySolicitanteExtraBinding
import com.auvenix.sigti.ui.auth.PasswordActivity
import com.auvenix.sigti.utils.Validators

class SolicitanteExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitanteExtraBinding

    /** URI de la imagen INE seleccionada (null = no seleccionada aún) */
    private var ineUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            // El usuario canceló el selector
            return@registerForActivityResult
        }
        val validationError = validateIneImage(uri)
        if (validationError != null) {
            binding.tvIneStatus.text = "✗  $validationError"
            binding.tvIneStatus.setTextColor(android.graphics.Color.parseColor("#C62828"))
            ineUri = null
        } else {
            ineUri = uri
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "INE_Frontal"
            binding.tvIneStatus.text = "✓  $fileName cargada con éxito"
            binding.tvIneStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySolicitanteExtraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // ── Zona de carga del INE ─────────────────────────────────────────────
        binding.btnUploadIne.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // ── Botón Continuar ───────────────────────────────────────────────────
        binding.btnContinuarSolicitante.setOnClickListener {
            procesarYContinuar()
        }
    }

    // ── Validación de imagen INE ──────────────────────────────────────────────
    private fun validateIneImage(uri: Uri): String? {
        return try {
            // 1. Tamaño del archivo (mínimo 20 KB)
            val fileSizeBytes = contentResolver.openFileDescriptor(uri, "r")?.use {
                it.statSize
            } ?: 0L
            if (fileSizeBytes < 20_000L) {
                return "La imagen es demasiado pequeña o puede estar vacía"
            }

            // 2. Decodificar solo dimensiones (sin cargar el bitmap completo)
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)
            }

            val w = opts.outWidth
            val h = opts.outHeight

            // 3. Verifica que el archivo sea una imagen reconocida
            if (w <= 0 || h <= 0) {
                return "No se pudo leer la imagen. Intenta con otro archivo."
            }

            // 4. Resolución mínima
            if (w < 300 || h < 190) {
                return "La imagen es demasiado pequeña. Sube una foto nítida del INE."
            }

            // 5. Relación de aspecto (horizontal o vertical)
            val ratio = w.toFloat() / h.toFloat()
            val isHorizontal = ratio in 1.3f..1.9f   // tarjeta apaisada
            val isVertical   = ratio in 0.52f..0.77f // tarjeta de pie
            if (!isHorizontal && !isVertical) {
                return "La foto no parece ser una identificación. " +
                        "Asegúrate de fotografiar el INE completo."
            }

            null  // ✅ Todo correcto
        } catch (e: Exception) {
            "No se pudo validar la imagen: ${e.localizedMessage}"
        }
    }

    // ── Procesado y navegación ────────────────────────────────────────────────
    private fun procesarYContinuar() {
        var hayError = false

        // Validar ciudad
        val ciudad = binding.etCiudad.text.toString().trim()
        if (ciudad.isEmpty()) {
            binding.etCiudad.error = "Campo obligatorio"
            hayError = true
        } else {
            binding.etCiudad.error = null
        }

        val direccion = binding.etDireccion.text.toString().trim()
        val addressResult = Validators.validateAddress(direccion)
        if (addressResult != Validators.AddressResult.Ok) {
            binding.etDireccion.error = addressResult.message()
            hayError = true
        } else {
            binding.etDireccion.error = null
        }

        if (hayError) return

        // Validar INE cargado
        if (ineUri == null) {
            Toast.makeText(this, "Por favor sube una foto de tu INE", Toast.LENGTH_SHORT).show()
            return
        }

        // Todo correcto → continuar a contraseña
        startActivity(Intent(this, PasswordActivity::class.java).apply {
            putExtras(intent)                              // datos de RegisterGeneralActivity
            putExtra("extra_ciudad",    ciudad)
            putExtra("extra_direccion", direccion)
            putExtra("extra_ine_uri",   ineUri.toString()) // URI de la imagen INE
        })
    }
}