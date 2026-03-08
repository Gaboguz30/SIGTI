package com.auvenix.sigti.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityWorkerProfileBinding
import com.auvenix.sigti.ui.home.HomeActivity

class WorkerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        setupListeners()
        setupServicesRecyclerView()
    }

    private fun initUI() {
        // Datos mock del prestador (En el futuro esto vendrá del Intent)
        binding.tvWorkerName.text = "Vianca Ramirez"
        binding.tvWorkerJob.text = "Electricista"
        binding.tvExperience.text = "Experiencia: 6 Años"
    }

    private fun setupListeners() {
        // Acciones del Header
        binding.btnBack.setOnClickListener { finish() }
        binding.btnReport.setOnClickListener { Toast.makeText(this, "Reporte enviado", Toast.LENGTH_SHORT).show() }

        binding.btnShare.setOnClickListener { Toast.makeText(this, "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show() }

        // Botones de acción
        binding.btnRequest.setOnClickListener {
            val intent = Intent(this, JobRequest::class.java)

            // Suponiendo que tienes los datos en variables o los extraes del perfil
            val name = binding.tvWorkerName.text.toString()
            val category = binding.tvWorkerJob.text.toString()

            intent.putExtra("WORKER_NAME", name)
            intent.putExtra("WORKER_CATEGORY", category)

            startActivity(intent)
        }
        binding.btnChat.setOnClickListener { Toast.makeText(this, "Iniciando conversación...", Toast.LENGTH_SHORT).show() }

        // Tabs Custom
        binding.tabServices.setOnClickListener { selectTab(isServices = true) }
        binding.tabPortfolio.setOnClickListener { selectTab(isServices = false) }

        // Navegación Inferior
        binding.navHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun selectTab(isServices: Boolean) {
        if (isServices) {
            // Cambiar textos y colores
            binding.tvTabServices.setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
            binding.indicatorServices.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_primary))

            binding.tvTabPortfolio.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
            binding.indicatorPortfolio.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

            // Visibilidad de contenedores
            binding.rvServices.visibility = View.VISIBLE
            binding.tvPortfolioEmpty.visibility = View.GONE
        } else {
            binding.tvTabServices.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
            binding.indicatorServices.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

            binding.tvTabPortfolio.setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
            binding.indicatorPortfolio.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_primary))

            binding.rvServices.visibility = View.GONE
            binding.tvPortfolioEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupServicesRecyclerView() {
        val mockServices = listOf(
            ServiceItem(1, "Instalación Eléctrica", "Incluye cotización y tiempo estimado", "desde $350"),
            ServiceItem(2, "Revisión de corto", "Incluye cotización y tiempo estimado", "desde $300"),
            ServiceItem(3, "Mantenimiento Preventivo", "Incluye cotización y tiempo estimado", "desde $250")
        )

        binding.rvServices.layoutManager = LinearLayoutManager(this)
        binding.rvServices.adapter = ServiceAdapter(mockServices)
    }
}