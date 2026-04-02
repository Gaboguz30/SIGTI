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
import com.auvenix.sigti.ui.chat.ChatListActivity
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var rvWorkers: RecyclerView
    private lateinit var workerAdapter: WorkerAdapter

    private val workerList = mutableListOf<Worker>()
    private val filteredList = mutableListOf<Worker>()

    private lateinit var etSearch: EditText

    private var oficioSeleccionado: String = ""

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        rvWorkers = findViewById(R.id.rvWorkers)
        rvWorkers.layoutManager = LinearLayoutManager(this)

        etSearch = findViewById(R.id.etSearch)

        // 🔍 BUSCADOR EN TIEMPO REAL
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltro(s.toString())
            }
        })

        // 🔥 ADAPTER
        workerAdapter = WorkerAdapter(filteredList) { worker ->
            val intent = Intent(this, com.auvenix.sigti.ui.profile.WorkerProfileActivity::class.java)
            intent.putExtra("EXTRA_WORKER_UID", worker.uid)
            startActivity(intent)
        }

        rvWorkers.adapter = workerAdapter

        // 🔥 CHIPS
        configurarChips()

        // 🔥 FIREBASE
        descargarPrestadoresDeFirestore()

        setupBottomNavigation()
    }

    // 🔥 FIRESTORE
    private fun descargarPrestadoresDeFirestore() {
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

                    if (oficiosRaw is List<*>) {
                        if (oficiosRaw.isNotEmpty()) {

                            val primerOficio = oficiosRaw[0]

                            if (primerOficio is Map<*, *>) {
                                profesion = primerOficio["nombre"]?.toString() ?: ""
                            }
                        }
                    }

                    Log.d("FIREBASE", "Oficios: $oficiosRaw")
                    Log.d("FIREBASE", "Profesion: $profesion")

                    val rating = document.getString("rating")
                    val price = document.getString("price")
                    val distance = document.getString("distance")
                    val online = document.getBoolean("online") ?: false
                    val availability = if (online) "Disponible" else "No disponible"

                    val worker = Worker(
                        uid = uid,
                        name = nombreCompleto,
                        profession = profesion,
                        rating = rating,
                        price = price,
                        distance = distance,
                        availability = availability
                    )

                    workerList.add(worker)
                }

                aplicarFiltro("")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔥 FILTRO
    private fun aplicarFiltro(query: String) {
        filteredList.clear()

        for (worker in workerList) {

            val coincideBusqueda =
                worker.name.contains(query, true) ||
                        worker.profession.contains(query, true)

            val coincideOficio =
                oficioSeleccionado.isEmpty() ||
                        worker.profession.equals(oficioSeleccionado, true)

            if (coincideBusqueda && coincideOficio) {
                filteredList.add(worker)
            }
        }

        workerAdapter.notifyDataSetChanged()
    }

    // 🔥 CHIPS CONFIG
    private fun configurarChips() {

        val chipAlbanil = findViewById<View>(R.id.chipAlbanil)
        val chipElectricista = findViewById<View>(R.id.chipElectricista)
        val chipPlomero = findViewById<View>(R.id.chipPlomero)
        val chipCarpintero = findViewById<View>(R.id.chipCarpintero)
        val chipPintor = findViewById<View>(R.id.chipPintor)

        val chips = listOf(
            chipAlbanil, chipElectricista, chipPlomero,
            chipCarpintero, chipPintor,
        )

        fun resetChips() {
            chips.forEach {
                it.setBackgroundColor(Color.parseColor("#E5E7EB"))
                if (it is TextView) {
                    it.setTextColor(Color.parseColor("#111827"))
                }
            }
        }

        fun activarChip(view: View, oficio: String) {
            if (oficioSeleccionado == oficio) {
                oficioSeleccionado = ""
                resetChips()
            } else {
                oficioSeleccionado = oficio
                resetChips()

                view.setBackgroundColor(Color.parseColor("#2563EB"))

                if (view is TextView) {
                    view.setTextColor(Color.WHITE)
                }
            }

            aplicarFiltro(etSearch.text.toString())
        }

        chipAlbanil.setOnClickListener { activarChip(it, "Albañil") }
        chipElectricista.setOnClickListener { activarChip(it, "Electricista") }
        chipPlomero.setOnClickListener { activarChip(it, "Plomero") }
        chipCarpintero.setOnClickListener { activarChip(it, "Carpintero") }
        chipPintor.setOnClickListener { activarChip(it, "Pintor") }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_map -> {
                    startActivity(Intent(this, UserMapActivity::class.java))
                    finish(); true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    finish(); true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, UserNotificationsActivity::class.java))
                    finish(); true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish(); true
                }
                else -> false
            }
        }
    }
}