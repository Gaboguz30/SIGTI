package com.auvenix.sigti.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityPrestadorExtraBinding
import com.auvenix.sigti.ui.auth.PasswordActivity


class PrestadorExtraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrestadorExtraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Activamos ViewBinding
        binding = ActivityPrestadorExtraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Agregamos el primer oficio por defecto para que no salga vacía la pantalla
        agregarFilaOficio()

        // 2. Escuchamos el botón de "+ Agregar otro oficio"
        binding.btnAddOficio.setOnClickListener {
            agregarFilaOficio()
        }

        // 3. Botón para simular subir la INE (Esto lo conectaremos a la cámara después)
        binding.btnUploadIne.setOnClickListener {
            Toast.makeText(this, "Abriendo galería...", Toast.LENGTH_SHORT).show()
            binding.tvIneStatus.text = "INE_Frontal.jpg (Cargada con éxito)"
            binding.tvIneStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Verde
        }

        // 4. Botón Continuar: Mandamos todo a la pantalla de Password
        binding.btnContinuarExtra.setOnClickListener {
            procesarDatosYContinuar()
        }
    }

    private fun agregarFilaOficio() {
        // Inflamos (fabricamos) un molde nuevo usando el layout row_oficio.xml
        val vistaOficio: View = LayoutInflater.from(this).inflate(R.layout.row_oficio, null)

        // Buscamos el botoncito de eliminar DENTRO de este molde específico
        val btnEliminar = vistaOficio.findViewById<ImageButton>(R.id.btnRemoveOficio)

        // Si el usuario le da al botoncito de basurero, borramos esta fila entera de la caja
        btnEliminar.setOnClickListener {
            binding.llOficiosContainer.removeView(vistaOficio)
        }

        // Metemos el molde recién fabricado a nuestra "Caja de Cartón" (llOficiosContainer)
        binding.llOficiosContainer.addView(vistaOficio)
    }


    private fun procesarDatosYContinuar() {
        val ciudad = binding.etCiudad.text.toString().trim()
        if (ciudad.isEmpty()) {
            binding.etCiudad.error = "Campo obligatorio"
            return
        }

        // 1. Recolectamos los oficios de la Caja de Cartón (Igual que antes)
        val listaOficios = ArrayList<String>()
        val cantidadMoldes = binding.llOficiosContainer.childCount

        for (i in 0 until cantidadMoldes) {
            val molde = binding.llOficiosContainer.getChildAt(i)
            val etNombre = molde.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etOficioNombre)
            val etAnios = molde.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etOficioAnios)

            val nombreOficio = etNombre?.text.toString().trim()
            val aniosExp = etAnios?.text.toString().trim()

            if (nombreOficio.isNotEmpty() && aniosExp.isNotEmpty()) {
                listaOficios.add("$nombreOficio|$aniosExp")
            }
        }

        if (listaOficios.isEmpty()) {
            android.widget.Toast.makeText(this, "Agrega al menos un oficio con sus años de experiencia", android.widget.Toast.LENGTH_LONG).show()
            return
        }

        // ==========================================================
        // 2. EL SWITCH LÓGICO DEL AGENTE DE TRÁNSITO
        // ==========================================================

        // Buscamos en la maleta si la pantalla anterior nos avisó que es de Google
        // (Asegúrate de mandar este "extra_is_google" desde tu pantalla de Google SignIn)
        val vieneDeGoogle = intent.getBooleanExtra("extra_is_google", false)

        if (vieneDeGoogle) {
            // 🚪 CAMINO VIP (Google)
            // Aquí en el futuro guardaremos directo en Firestore
            android.widget.Toast.makeText(this, "Usuario de Google: Guardando en BD y yendo a Planes...", android.widget.Toast.LENGTH_SHORT).show()

            // Y lo mandamos a la caja registradora (ProviderPlansActivity)
            // (Aún te marcará error en ProviderPlansActivity porque no lo hemos creado)
            /* val i = Intent(this, com.auvenix.sigti.ui.provider.plans.ProviderPlansActivity::class.java)
            startActivity(i)
            finish()
            */

        } else {
            // 🚪 CAMINO LARGO (Correo)
            // Aún no tiene cuenta, lo mandamos a PasswordActivity con la maleta llena
            val i = Intent(this, PasswordActivity::class.java).apply {
                putExtras(intent) // Pasamos lo que venía de la pantalla anterior
                putExtra("extra_ciudad", ciudad)
                putStringArrayListExtra("extra_oficios", listaOficios)
            }
            startActivity(i)
        }
    }
}