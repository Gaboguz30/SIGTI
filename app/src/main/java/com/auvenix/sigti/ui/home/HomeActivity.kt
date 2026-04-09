package com.auvenix.sigti.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.LinearLayout
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var rvWorkers: RecyclerView
    private lateinit var workerAdapter: WorkerAdapter

    private val workerList = mutableListOf<Worker>()
    private val filteredList = mutableListOf<Worker>()

    private lateinit var etSearch: EditText
    private var oficioSeleccionado: String = ""

    private val db = FirebaseFirestore.getInstance()
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        rvWorkers = findViewById(R.id.rvWorkers)
        rvWorkers.layoutManager = LinearLayoutManager(this)
        etSearch = findViewById(R.id.etSearch)

        val soloLetrasFilter = android.text.InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                val char = source[i]
                if (!Character.isLetter(char) && !Character.isSpaceChar(char)) {
                    return@InputFilter ""
                }
            }
            null
        }
        etSearch.filters = arrayOf(soloLetrasFilter)

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltro(s.toString())
            }
        })

        workerAdapter = WorkerAdapter(filteredList) { worker ->
            val intent = Intent(this, com.auvenix.sigti.ui.profile.WorkerProfileActivity::class.java)
            intent.putExtra("EXTRA_WORKER_UID", worker.uid)
            startActivity(intent)
        }
        rvWorkers.adapter = workerAdapter

        configurarChips()
        descargarPrestadoresDeFirestore()
        setupBottomNavigation()

        val btnNotifications = findViewById<View>(R.id.btnNotifications)
        btnNotifications.setOnClickListener {
            val bottomSheet = com.auvenix.sigti.notifications.NotificationsBottomSheet()
            bottomSheet.show(supportFragmentManager, "NotificationsSheet")
        }
    }

    private fun descargarPrestadoresDeFirestore() {
        db.collection("favorites")
            .whereEqualTo("user_uid", myUid)
            .get()
            .addOnSuccessListener { favSnapshot ->

                val favoritosIds = favSnapshot.documents.mapNotNull { it.getString("technician_uid") }

                db.collection("users")
                    .whereEqualTo("role", "PRESTADOR")
                    .get()
                    .addOnSuccessListener { documents ->
                        workerList.clear()

                        for (document in documents) {
                            val uid = document.id
                            val nombre = document.getString("nombre") ?: ""
                            val apPaterno = document.getString("apPaterno") ?: ""
                            val apMaterno = document.getString("apMaterno") ?: ""

                            val nombreCompleto = listOf(nombre, apPaterno, apMaterno)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")

                            val oficiosRaw = document.get("oficios")
                            var profesion = ""

                            if (oficiosRaw is List<*> && oficiosRaw.isNotEmpty()) {
                                val primerOficio = oficiosRaw[0]
                                if (primerOficio is Map<*, *>) {
                                    profesion = primerOficio["nombre"]?.toString() ?: ""
                                }
                            }

                            val price = document.getString("price")
                            val distance = document.getString("distance")
                            val online = document.getBoolean("online") ?: false
                            val availability = if (online) "Disponible" else "No disponible"

                            obtenerRatingPromedio(uid) { ratingPromedio ->
                                val worker = Worker(
                                    uid = uid,
                                    name = nombreCompleto,
                                    profession = profesion,
                                    rating = ratingPromedio,
                                    price = price,
                                    distance = distance,
                                    availability = availability
                                )

                                workerList.add(worker)

                                workerList.sortByDescending { w ->
                                    if (favoritosIds.contains(w.uid)) 1 else 0
                                }

                                aplicarFiltro(etSearch.text.toString())
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al cargar prestadores", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar favoritos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun aplicarFiltro(query: String) {
        filteredList.clear()

        for (worker in workerList) {
            val coincideBusqueda = worker.name.contains(query, true) || worker.profession.contains(query, true)
            val coincideOficio = oficioSeleccionado.isEmpty() || worker.profession.equals(oficioSeleccionado, true)

            if (coincideBusqueda && coincideOficio) {
                filteredList.add(worker)
            }
        }
        workerAdapter.notifyDataSetChanged()
    }

    private fun configurarChips() {
        val chipAlbanil = findViewById<LinearLayout>(R.id.catAlbanil)
        val chipElectricista = findViewById<LinearLayout>(R.id.catElectricista)
        val chipPlomero = findViewById<LinearLayout>(R.id.catPlomero)
        val chipCarpintero = findViewById<LinearLayout>(R.id.catCarpintero)
        val chipPintor = findViewById<LinearLayout>(R.id.catPintor)

        val chips = listOf(chipAlbanil, chipElectricista, chipPlomero, chipCarpintero, chipPintor)

        fun resetChips() {
            chips.forEach {
                it.setBackgroundResource(R.drawable.bg_category)
                val icon = it.getChildAt(0) as ImageView
                val text = it.getChildAt(1) as TextView
                icon.setColorFilter(Color.parseColor("#2563EB"))
                text.setTextColor(Color.parseColor("#374151"))
            }
        }

        fun activarChip(view: LinearLayout, oficio: String) {
            if (oficioSeleccionado == oficio) {
                oficioSeleccionado = ""
                resetChips()
            } else {
                oficioSeleccionado = oficio
                resetChips()
                view.setBackgroundResource(R.drawable.bg_category_selected)
                val icon = view.getChildAt(0) as ImageView
                val text = view.getChildAt(1) as TextView
                icon.setColorFilter(Color.WHITE)
                text.setTextColor(Color.WHITE)
            }
            aplicarFiltro(etSearch.text.toString())
        }

        chipAlbanil.setOnClickListener { activarChip(it as LinearLayout, "Albañil") }
        chipElectricista.setOnClickListener { activarChip(it as LinearLayout, "Electricista") }
        chipPlomero.setOnClickListener { activarChip(it as LinearLayout, "Plomero") }
        chipCarpintero.setOnClickListener { activarChip(it as LinearLayout, "Carpintero") }
        chipPintor.setOnClickListener { activarChip(it as LinearLayout, "Pintor") }
    }

    // 🔥 NAVEGACIÓN SUAVE
    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_map -> { irAPantalla(UserMapActivity::class.java); true }
                R.id.nav_chat -> { irAPantalla(com.auvenix.sigti.ui.provider.chat.ProviderChatActivity::class.java); true }
                R.id.nav_jobs -> { irAPantalla(com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity::class.java); true }
                R.id.nav_profile -> { irAPantalla(ProfileActivity::class.java); true }
                else -> false
            }
        }
    }

    private fun obtenerRatingPromedio(workerId: String, callback: (String?) -> Unit) {
        db.collection("reviews")
            .whereEqualTo("technician_uid", workerId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(null)
                    return@addOnSuccessListener
                }
                var suma = 0.0
                var total = 0

                for (doc in documents) {
                    val rating = doc.getDouble("rating") ?: 0.0
                    suma += rating
                    total++
                }
                val promedio = suma / total
                callback(String.format("%.1f", promedio))
            }
            .addOnFailureListener { callback(null) }
    }

    // 🔥 LA FUNCIÓN MÁGICA
    private fun irAPantalla(activityClass: Class<*>) {
        if (this::class.java == activityClass) return
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}