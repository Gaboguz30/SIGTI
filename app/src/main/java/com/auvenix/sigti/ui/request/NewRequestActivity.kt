package com.auvenix.sigti.ui.request

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.NumberPicker // 🔥 IMPORTAMOS NUMBER PICKER
import java.util.Calendar

// Imports de navegación
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.chat.ChatListActivity
import com.auvenix.sigti.ui.home.UserNotificationsActivity
import com.auvenix.sigti.ui.profile.ProfileActivity

class NewRequestActivity : AppCompatActivity() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var providerId = ""
    private var providerName = ""
    private var myClientName = "Cliente"

    // Mapa para guardar los servicios del prestador y sus precios mínimos
    private val servicePricesMap = mutableMapOf<String, Double>()
    private var currentMinPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_request)

        providerId = intent.getStringExtra("EXTRA_WORKER_UID") ?: ""
        providerName = intent.getStringExtra("EXTRA_WORKER_NAME") ?: "Prestador"

        if (providerId.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró al prestador", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Referencias a las vistas
        val btnSubmit   = findViewById<MaterialButton>(R.id.btnSubmitRequest)

        val tilServicio     = findViewById<TextInputLayout>(R.id.tilServicio)
        val etServicio      = findViewById<AutoCompleteTextView>(R.id.etServicio)

        val tilServicioOtro = findViewById<TextInputLayout>(R.id.tilServicioPersonalizado)
        val etServicioOtro  = findViewById<TextInputEditText>(R.id.etServicioPersonalizado)

        val tilPrecio       = findViewById<TextInputLayout>(R.id.tilPrecioOferta)
        val etPrecio        = findViewById<TextInputEditText>(R.id.etPrecioOferta)

        val tilFecha    = findViewById<TextInputLayout>(R.id.tilFecha)
        val etFecha     = findViewById<TextInputEditText>(R.id.etFecha)
        val tilCalle    = findViewById<TextInputLayout>(R.id.tilCalle)
        val etCalle     = findViewById<TextInputEditText>(R.id.etCalle)
        val tilNumExt   = findViewById<TextInputLayout>(R.id.tilNumExt)
        val etNumExt    = findViewById<TextInputEditText>(R.id.etNumExt)
        val tilCp       = findViewById<TextInputLayout>(R.id.tilCp)
        val etCp        = findViewById<TextInputEditText>(R.id.etCp)
        val tilDesc     = findViewById<TextInputLayout>(R.id.tilDescripcion)
        val etDesc      = findViewById<TextInputEditText>(R.id.etDescripcion)


        // ========================================================
        // 🔥 MAGIA DE TUS RUEDITAS (NUMBER PICKERS)
        // ========================================================
        etFecha.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_date_picker, null)
            val builder = AlertDialog.Builder(this).setView(dialogView)
            val alertDialog = builder.create()

            val npDay = dialogView.findViewById<NumberPicker>(R.id.npDay)
            val npMonth = dialogView.findViewById<NumberPicker>(R.id.npMonth)
            val npYear = dialogView.findViewById<NumberPicker>(R.id.npYear)
            val btnAceptar = dialogView.findViewById<MaterialButton>(R.id.btnAceptar)
            val btnCancelar = dialogView.findViewById<MaterialButton>(R.id.btnCancelar)

            // Obtener fecha actual
            val c = Calendar.getInstance()
            val currentYear = c.get(Calendar.YEAR)
            val currentMonth = c.get(Calendar.MONTH) // 0 - 11
            val currentDay = c.get(Calendar.DAY_OF_MONTH)

            // Configurar Días (1 al 31)
            npDay?.minValue = 1
            npDay?.maxValue = 31
            npDay?.value = currentDay

            // Configurar Meses (Mostramos letras pero guardamos el número)
            val monthNames = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
            npMonth?.minValue = 0
            npMonth?.maxValue = 11
            npMonth?.displayedValues = monthNames
            npMonth?.value = currentMonth

            // Configurar Años (Desde este año hasta 5 años más)
            npYear?.minValue = currentYear
            npYear?.maxValue = currentYear + 5
            npYear?.value = currentYear

            btnAceptar?.setOnClickListener {
                if (npDay != null && npMonth != null && npYear != null) {
                    val day = npDay.value
                    val month = npMonth.value + 1 // Le sumamos 1 porque Enero es 0
                    val year = npYear.value

                    val formattedDate = String.format("%02d/%02d/%04d", day, month, year)
                    etFecha.setText(formattedDate)
                }
                alertDialog.dismiss()
            }

            btnCancelar?.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.show()
        }
        // ========================================================

        // 1. Obtener mi nombre de cliente
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellido = doc.getString("apellidoPaterno") ?: ""
                    myClientName = "$nombre $apellido".trim()
                    if (myClientName.isEmpty()) myClientName = "Cliente"
                }
            }
        }

        // 2. Cargar el catálogo del prestador
        loadProviderCatalog(etServicio, tilServicioOtro, tilPrecio)

        // 3. ENVIAR LA OFERTA
        btnSubmit.setOnClickListener {
            val selectedService = etServicio.text.toString().trim()
            val customService   = etServicioOtro.text.toString().trim()
            val ofertaStr       = etPrecio.text.toString().trim()
            val desc            = etDesc.text.toString().trim()
            val fecha           = etFecha.text.toString().trim()
            val calle           = etCalle.text.toString().trim()
            val numExt          = etNumExt.text.toString().trim()
            val cp              = etCp.text.toString().trim()

            var hasError = false

            if (selectedService.isEmpty()) { tilServicio.error = "Selecciona un servicio"; hasError = true } else { tilServicio.error = null }

            var finalTitle = selectedService
            if (selectedService == "Otro") {
                if (customService.isEmpty()) {
                    tilServicioOtro.error = "Especifica el servicio"
                    hasError = true
                } else {
                    tilServicioOtro.error = null
                    finalTitle = customService
                }
            }

            val ofertaMonto = ofertaStr.toDoubleOrNull() ?: 0.0
            if (ofertaStr.isEmpty()) {
                tilPrecio.error = "Ingresa tu oferta"
                hasError = true
            } else if (selectedService != "Otro" && ofertaMonto < currentMinPrice) {
                tilPrecio.error = "El precio base es $${currentMinPrice}"
                hasError = true
            } else {
                tilPrecio.error = null
            }

            if (fecha.isEmpty()) { tilFecha.error = "Obligatorio"; hasError = true } else { tilFecha.error = null }
            if (calle.isEmpty()) { tilCalle.error = "Obligatorio"; hasError = true } else { tilCalle.error = null }
            if (numExt.isEmpty()) { tilNumExt.error = "Obligatorio"; hasError = true } else { tilNumExt.error = null }
            if (cp.isEmpty()) { tilCp.error = "Obligatorio"; hasError = true } else { tilCp.error = null }
            if (desc.isEmpty()) { tilDesc.error = "Obligatorio"; hasError = true } else { tilDesc.error = null }

            if (hasError) {
                Toast.makeText(this, "Verifica los errores en rojo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (uid == null) {
                Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Enviando Oferta..."

            val solicitud = hashMapOf(
                "clientId"    to uid,
                "clientName"  to myClientName,
                "providerId"  to providerId,
                "providerName" to providerName,
                "title"       to finalTitle,
                "priceOffer"  to ofertaMonto,
                "fecha"       to fecha,
                "calle"       to calle,
                "numExt"      to numExt,
                "cp"          to cp,
                "description" to desc,
                "status"      to "pending",
                "timestamp"   to FieldValue.serverTimestamp()
            )

            db.collection("requests").add(solicitud)
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Oferta enviada a $providerName!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Enviar Oferta"
                    Toast.makeText(this, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        setupBottomNavigation()
    }

    private fun loadProviderCatalog(
        etServicio: AutoCompleteTextView,
        tilServicioOtro: TextInputLayout,
        tilPrecio: TextInputLayout
    ) {
        db.collection("users").document(providerId).collection("services").get()
            .addOnSuccessListener { documents ->
                val serviceNames = mutableListOf<String>()

                for (doc in documents) {
                    val name = doc.getString("name") ?: ""
                    val price = doc.getDouble("price") ?: 0.0
                    if (name.isNotEmpty()) {
                        serviceNames.add(name)
                        servicePricesMap[name] = price
                    }
                }

                serviceNames.add("Otro")

                val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, serviceNames)
                etServicio.setAdapter(arrayAdapter)

                etServicio.setOnItemClickListener { _, _, position, _ ->
                    val selected = arrayAdapter.getItem(position) ?: ""

                    if (selected == "Otro") {
                        tilServicioOtro.visibility = View.VISIBLE
                        currentMinPrice = 0.0
                        tilPrecio.helperText = "Pon el precio que ofreces pagar"
                    } else {
                        tilServicioOtro.visibility = View.GONE
                        currentMinPrice = servicePricesMap[selected] ?: 0.0
                        tilPrecio.helperText = "Mínimo sugerido: $${currentMinPrice}"
                    }
                }
            }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationRequest)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_map -> { startActivity(Intent(this, UserMapActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_chat -> { startActivity(Intent(this, ChatListActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_notifications -> { startActivity(Intent(this, UserNotificationsActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                else -> false
            }
        }
    }
}