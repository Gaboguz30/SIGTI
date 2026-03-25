package com.auvenix.sigti.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.chat.ChatDetailActivity
import com.auvenix.sigti.ui.request.NewRequestActivity
import com.auvenix.sigti.ui.support.ReportActivity
import com.auvenix.sigti.ui.support.ReviewsActivity
import com.auvenix.sigti.utils.Constants
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class WorkerProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rvServices: RecyclerView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileProfession: TextView
    private lateinit var tvProfileRating: TextView

    private var workerId: String = ""
    private var workerName: String = "Prestador"
    private var workerProfession: String = "Servicio"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_profile)

        workerId = intent.getStringExtra(Constants.EXTRA_WORKER_ID)
            ?: intent.getStringExtra("workerId")
                    ?: ""
        workerName = intent.getStringExtra(Constants.EXTRA_WORKER_NAME) ?: workerName
        workerProfession = intent.getStringExtra(Constants.EXTRA_WORKER_PROFESSION) ?: workerProfession

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnReport = findViewById<TextView>(R.id.btnReport)
        val btnShare = findViewById<TextView>(R.id.btnShare)
        val btnRequest = findViewById<MaterialButton>(R.id.btnRequest)
        val btnChat = findViewById<MaterialButton>(R.id.btnChat)

        rvServices = findViewById(R.id.rvServices)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileProfession = findViewById(R.id.tvProfileProfession)
        tvProfileRating = findViewById(R.id.tvProfileRating)

        rvServices.layoutManager = LinearLayoutManager(this)
        tvProfileName.text = workerName
        tvProfileProfession.text = workerProfession

        btnBack.setOnClickListener { finish() }
        btnShare.setOnClickListener {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Mira este perfil en SIGTI: $workerName - $workerProfession")
            }, "Compartir perfil"))
        }
        btnReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java).apply {
                putExtra(Constants.EXTRA_WORKER_ID, workerId)
                putExtra("workerId", workerId)
            })
        }
        btnRequest.setOnClickListener {
            startActivity(Intent(this, NewRequestActivity::class.java).apply {
                putExtra(Constants.EXTRA_WORKER_ID, workerId)
                putExtra(Constants.EXTRA_WORKER_NAME, workerName)
                putExtra(Constants.EXTRA_WORKER_PROFESSION, workerProfession)
            })
        }
        btnChat.setOnClickListener {
            startActivity(Intent(this, ChatDetailActivity::class.java).apply {
                putExtra(Constants.EXTRA_WORKER_ID, workerId)
                putExtra(Constants.EXTRA_WORKER_NAME, workerName)
            })
        }

        findViewById<TextView>(R.id.tvProfileRating).setOnClickListener {
            startActivity(Intent(this, ReviewsActivity::class.java).apply {
                putExtra(Constants.EXTRA_WORKER_ID, workerId)
            })
        }

        if (workerId.isBlank()) {
            Toast.makeText(this, "No se recibió el prestador seleccionado", Toast.LENGTH_SHORT).show()
            return
        }

        loadWorkerProfile()
    }

    private fun loadWorkerProfile() {
        db.collection(Constants.COLLECTION_USERS)
            .document(workerId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "El perfil no existe", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                workerName = listOf(
                    doc.getString("nombre").orEmpty(),
                    doc.getString("apPaterno").orEmpty()
                ).filter { it.isNotBlank() }.joinToString(" ").ifBlank { workerName }

                val oficios = doc.get("oficios") as? List<Map<String, Any>> ?: emptyList()
                workerProfession = oficios.firstOrNull()?.get("nombre")?.toString()
                    ?: doc.getString("oficio")
                            ?: workerProfession

                val rating = doc.getDouble("rating") ?: 0.0
                val totalReviews = doc.getLong("reviewsCount") ?: 0L

                tvProfileName.text = workerName
                tvProfileProfession.text = workerProfession
                tvProfileRating.text = "★ ${String.format(Locale.US, "%.1f", rating)} ($totalReviews)"

                val services = if (oficios.isNotEmpty()) {
                    oficios.map {
                        ServiceItem(
                            title = it["nombre"]?.toString() ?: "Servicio",
                            description = "${it["anios_experiencia"] ?: 0} años de experiencia",
                            price = doc.get("precioBase")?.toString() ?: "0"
                        )
                    }
                } else {
                    listOf(
                        ServiceItem(
                            title = workerProfession,
                            description = "Servicio registrado por el prestador",
                            price = doc.get("precioBase")?.toString() ?: "0"
                        )
                    )
                }

                rvServices.adapter = ServiceAdapter(services)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}