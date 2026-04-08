package com.auvenix.sigti.ui.request

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import android.widget.ImageView
import android.widget.TextView

import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.NumberPicker
import java.util.Calendar

import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.profile.ProfileActivity

class NewRequestActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var providerId = ""
    private var providerName = ""
    private var myClientName = "Cliente"

    private val servicePricesMap = mutableMapOf<String, Double>()
    private var currentMinPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_request)
        val header = findViewById<View>(R.id.header)

        val title = header.findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "Nueva Solicitud"

        val back = header.findViewById<ImageView>(R.id.btnBackHeader)
        back.setOnClickListener {
            finish()
        }

        setupBottomNavigation()

        providerId = intent.getStringExtra("EXTRA_WORKER_UID") ?: ""
        providerName = intent.getStringExtra("EXTRA_WORKER_NAME") ?: "Prestador"

        if (providerId.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró al prestador", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val btnSubmit = findViewById<MaterialButton>(R.id.btnSubmitRequest)

        val tilServicio = findViewById<TextInputLayout>(R.id.tilServicio)
        val etServicio = findViewById<AutoCompleteTextView>(R.id.etServicio)

        val tilServicioOtro = findViewById<TextInputLayout>(R.id.tilServicioPersonalizado)
        val etServicioOtro = findViewById<TextInputEditText>(R.id.etServicioPersonalizado)

        val tilFecha = findViewById<TextInputLayout>(R.id.tilFecha)
        val etFecha = findViewById<TextInputEditText>(R.id.etFecha)
        val tilCalle = findViewById<TextInputLayout>(R.id.tilCalle)
        val etCalle = findViewById<TextInputEditText>(R.id.etCalle)
        val tilNumExt = findViewById<TextInputLayout>(R.id.tilNumExt)
        val etNumExt = findViewById<TextInputEditText>(R.id.etNumExt)
        val tilCp = findViewById<TextInputLayout>(R.id.tilCp)
        val etCp = findViewById<TextInputEditText>(R.id.etCp)
        val tilDesc = findViewById<TextInputLayout>(R.id.tilDescripcion)
        val etDesc = findViewById<TextInputEditText>(R.id.etDescripcion)


        // 🔥 Cargar servicios
        loadProviderCatalog(etServicio, tilServicioOtro)

        // 🔥 Date Picker
        etFecha.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_date_picker, null)
            val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()

            val npDay = dialogView.findViewById<NumberPicker>(R.id.npDay)
            val npMonth = dialogView.findViewById<NumberPicker>(R.id.npMonth)
            val npYear = dialogView.findViewById<NumberPicker>(R.id.npYear)

            val btnAceptar = dialogView.findViewById<MaterialButton>(R.id.btnAceptar)
            val btnCancelar = dialogView.findViewById<MaterialButton>(R.id.btnCancelar)

            val c = Calendar.getInstance()

            npDay.minValue = 1
            npDay.maxValue = 31
            npDay.value = c.get(Calendar.DAY_OF_MONTH)

            val months = arrayOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
            npMonth.minValue = 0
            npMonth.maxValue = 11
            npMonth.displayedValues = months
            npMonth.value = c.get(Calendar.MONTH)

            npYear.minValue = c.get(Calendar.YEAR)
            npYear.maxValue = c.get(Calendar.YEAR) + 5
            npYear.value = c.get(Calendar.YEAR)

            btnAceptar.setOnClickListener {

                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(npYear.value, npMonth.value, npDay.value, 0, 0, 0)

                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)

                if (selectedCalendar.before(today)) {
                    Toast.makeText(this, "No puedes seleccionar una fecha anterior a la actual", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val date = String.format(
                    "%02d/%02d/%04d",
                    npDay.value,
                    npMonth.value + 1,
                    npYear.value
                )

                etFecha.setText(date)
                alertDialog.dismiss()
            }

            btnCancelar.setOnClickListener { alertDialog.dismiss() }

            alertDialog.show()
        }


        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener {
                val nombre = it.getString("nombre") ?: ""
                val apellido = it.getString("apellidoPaterno") ?: ""
                myClientName = "$nombre $apellido".trim().ifEmpty { "Cliente" }
            }
        }

        btnSubmit.setOnClickListener {

            val selectedService = etServicio.text.toString().trim()
            val customService = etServicioOtro.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val calle = etCalle.text.toString().trim()
            val numExt = etNumExt.text.toString().trim()
            val cp = etCp.text.toString().trim()

            var hasError = false

            if (selectedService.isEmpty()) {
                tilServicio.error = "Selecciona un servicio"; hasError = true
            } else tilServicio.error = null

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

            if (fecha.isEmpty()) { tilFecha.error = "Obligatorio"; hasError = true } else tilFecha.error = null
            if (calle.isEmpty()) { tilCalle.error = "Obligatorio"; hasError = true } else tilCalle.error = null
            if (numExt.isEmpty()) { tilNumExt.error = "Obligatorio"; hasError = true } else tilNumExt.error = null
            if (cp.isEmpty()) { tilCp.error = "Obligatorio"; hasError = true } else tilCp.error = null
            if (desc.isEmpty()) { tilDesc.error = "Obligatorio"; hasError = true } else tilDesc.error = null

            if (hasError) {
                Toast.makeText(this, "Verifica los errores", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (uid == null) {
                Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false

            val solicitud = hashMapOf(
                "clientId" to uid,
                "clientName" to myClientName,
                "providerId" to providerId,
                "providerName" to providerName,
                "title" to finalTitle,
                "fecha" to fecha,
                "calle" to calle,
                "numExt" to numExt,
                "cp" to cp,
                "description" to desc,
                "status" to "pending",
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("requests").add(solicitud)
                .addOnSuccessListener {
                    Toast.makeText(this, "Oferta enviada", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    btnSubmit.isEnabled = true
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun loadProviderCatalog(
        etServicio: AutoCompleteTextView,
        tilServicioOtro: TextInputLayout
    ) {
        db.collection("users").document(providerId).collection("services").get()
            .addOnSuccessListener {
                val list = mutableListOf<String>()

                for (doc in it) {
                    val name = doc.getString("name") ?: ""
                    val price = doc.getDouble("price") ?: 0.0

                    if (name.isNotEmpty()) {
                        list.add(name)
                        servicePricesMap[name] = price
                    }
                }

                list.add("Otro")

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, list)
                etServicio.setAdapter(adapter)

                etServicio.setOnItemClickListener { _, _, pos, _ ->
                    val selected = adapter.getItem(pos) ?: ""
                    tilServicioOtro.visibility = if (selected == "Otro") View.VISIBLE else View.GONE
                }
            }
    }

    private fun setupBottomNavigation() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationRequest)
        nav?.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> start(HomeActivity::class.java)
                R.id.nav_map -> start(UserMapActivity::class.java)
                R.id.nav_chat -> start(com.auvenix.sigti.ui.provider.chat.ProviderChatActivity::class.java)
                R.id.nav_profile -> start(ProfileActivity::class.java)
                else -> false
            }
        }
    }

    private fun start(activity: Class<*>) : Boolean {
        startActivity(Intent(this, activity))
        overridePendingTransition(0,0)
        finish()
        return true
    }
}