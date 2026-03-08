package com.auvenix.sigti.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityHomeBinding
import com.google.android.material.chip.Chip

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: WorkerAdapter
    private var fullWorkerList = mutableListOf<Worker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadMockData()
        setupRecyclerView()
        setupFilters()
        setupNavigation()
        setupOtherListeners()
    }

    private fun loadMockData() {
        fullWorkerList = mutableListOf(
            Worker(1, "Daniel Perez", "Electricista", "Hoy", 4.8, "2 km", "$350", true),
            Worker(2, "Alfonso Ramirez", "Electricista", "Mañana", 4.3, "5 km", "$350"),
            Worker(3, "Edgar Ramirez", "Plomero", "Hoy", 3.9, "1.5 km", "$200"),
            Worker(4, "Leonardo Gonzalo", "Albañil", "Hoy", 3.2, "12 km", "$450"),
            Worker(5, "Juan Perez", "Plomero", "Hoy", 4.5, "3 km", "$220"),
            Worker(6, "Maria Lopez", "Electricista", "Hoy", 4.9, "1 km", "$380"),
            Worker(7, "Pedro Picapiedra", "Albañil", "Mañana", 4.1, "10 km", "$500")
        )
    }

    private fun setupRecyclerView() {
        adapter = WorkerAdapter(fullWorkerList) { worker ->
            Toast.makeText(this, "Perfil de ${worker.name}", Toast.LENGTH_SHORT).show()
        }

        binding.rvWorkers.layoutManager = LinearLayoutManager(this)
        binding.rvWorkers.adapter = adapter
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val chip = group.findViewById<Chip>(checkedIds[0])
            val category = chip.text.toString()

            if (category == "Todos") {
                adapter.updateList(fullWorkerList)
            } else {
                val filtered = fullWorkerList.filter { it.job == category }
                adapter.updateList(filtered)
            }
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.nav_map -> {
                    Toast.makeText(this, "Navegando a Mapa...", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.nav_chat -> {
                    Toast.makeText(this, "Abriendo Mensajes...", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.nav_notifications -> {
                    Toast.makeText(this, "Abriendo Notificaciones...", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.nav_profile -> {
                    Toast.makeText(this, "Navegando a Perfil...", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }

    private fun setupOtherListeners() {
        binding.tvMapHeader.setOnClickListener {
            Toast.makeText(this, "Abriendo mapa general", Toast.LENGTH_SHORT).show()
        }

        binding.ivAdvancedFilter.setOnClickListener {
            Toast.makeText(this, "Abriendo filtros avanzados", Toast.LENGTH_SHORT).show()
        }
    }
}