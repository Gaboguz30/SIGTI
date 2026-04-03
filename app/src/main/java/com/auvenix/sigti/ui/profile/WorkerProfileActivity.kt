package com.auvenix.sigti.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.chat.ChatDetailActivity
import com.auvenix.sigti.ui.request.NewRequestActivity
import com.auvenix.sigti.ui.support.ReportActivity
import com.auvenix.sigti.ui.provider.catalog.ServiceCatalog
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.auvenix.sigti.ui.support.Review
import com.auvenix.sigti.ui.support.ReviewAdapter

import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.home.UserNotificationsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity

class WorkerProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvProfession: TextView
    private lateinit var rvServices: RecyclerView
    private lateinit var ratingBar: RatingBar
    private lateinit var tvRatingNumber: TextView

    private lateinit var tvEmptyServices: TextView

    private var workerId: String = ""
    private var workerName: String = ""
    private lateinit var tvExperience: TextView
    private lateinit var rvReviews: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewList = mutableListOf<Review>()

    private val db = FirebaseFirestore.getInstance()

    private val servicesList = mutableListOf<ServiceCatalog>()
    private lateinit var adapter: PublicServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_profile)

        workerId = intent.getStringExtra("EXTRA_WORKER_UID") ?: ""

        if (workerId.isEmpty()) {
            Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupRecycler()
        cargarPerfilReal()
        cargarServicios()
        setupButtons()
        setupBottomNav()
    }

    private fun setupTabs() {

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val rvServices = findViewById<RecyclerView>(R.id.rvServices)
        val rvReviews = findViewById<RecyclerView>(R.id.rvReviews)

        tabLayout.addTab(tabLayout.newTab().setText("Servicios"))
        tabLayout.addTab(tabLayout.newTab().setText("Reseñas"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {

                    0 -> { // Servicios
                        rvServices.visibility = View.VISIBLE
                        rvReviews.visibility = View.GONE
                    }

                    1 -> { // Reseñas
                        rvServices.visibility = View.GONE
                        rvReviews.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun initViews() {
        tvName = findViewById(R.id.tvProfileName)
        tvProfession = findViewById(R.id.tvProfileProfession)
        rvServices = findViewById(R.id.rvServices)
        tvEmptyServices = findViewById(R.id.tvEmptyServicesPerfil)
        ratingBar = findViewById(R.id.ratingBar)
        tvRatingNumber = findViewById(R.id.tvRatingNumber)

        // 🔥 ESTA LÍNEA FALTABA
        tvExperience = findViewById(R.id.tvProfileExperience)
        rvReviews = findViewById(R.id.rvReviews)
        setupTabs()
        setupReviews()

    }

    private fun setupRecycler() {
        rvServices.layoutManager = LinearLayoutManager(this)
        adapter = PublicServiceAdapter(servicesList)
        rvServices.adapter = adapter
    }

    // 🔥 SOLO DATOS REALES (SIN INVENTAR)
    private fun cargarPerfilReal() {

        db.collection("users").document(workerId)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) return@addOnSuccessListener

                // 🔹 NOMBRE
                val nombre = doc.getString("nombre") ?: ""
                val apP = doc.getString("apPaterno") ?: ""
                val apM = doc.getString("apMaterno") ?: ""

                workerName = listOf(nombre, apP, apM)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                tvName.text = workerName

                // 🔹 PROFESION DESDE OFICIOS
                val oficiosRaw = doc.get("oficios")

                var profesion = ""
                var experiencia = ""

                if (oficiosRaw is List<*>) {
                    if (oficiosRaw.isNotEmpty()) {
                        val primero = oficiosRaw[0]

                        if (primero is Map<*, *>) {
                            profesion = primero["nombre"]?.toString() ?: ""

                            val exp = primero["anios_experiencia"]
                            experiencia = if (exp != null) {
                                "Experiencia: $exp años"
                            } else {
                                ""
                            }
                        }
                    }
                }


                if (profesion.isNotEmpty()) {
                    tvProfession.text = profesion
                    if (experiencia.isNotEmpty()) {
                        tvExperience.text = experiencia
                        tvExperience.visibility = View.VISIBLE
                    } else {
                        tvExperience.visibility = View.GONE
                    }
                    tvProfession.visibility = View.VISIBLE
                } else {
                    tvProfession.visibility = View.GONE
                }

                val rating = doc.getString("rating")?.toFloatOrNull() ?: 0f
                ratingBar.rating = rating

                if (rating > 0f) {
                    tvRatingNumber.text = rating.toString()
                } else {
                    tvRatingNumber.text = "Sin reseñas"
                }




            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupReviews() {

        rvReviews.layoutManager = LinearLayoutManager(this)

        db.collection("reviews")
            .whereEqualTo("technician_uid", workerId)
            .get()
            .addOnSuccessListener { documents ->

                reviewList.clear()

                for (doc in documents) {

                    val user = "Usuario" // luego lo mejoramos
                    val comment = doc.getString("comment") ?: ""
                    val rating = doc.getLong("rating")?.toFloat() ?: 0f
                    val date = doc.getTimestamp("created_at")?.toDate()?.toString() ?: ""

                    reviewList.add(
                        Review(user, date, comment)
                    )
                }

                reviewAdapter = ReviewAdapter(reviewList)
                rvReviews.adapter = reviewAdapter
            }
    }

    private fun cargarServicios() {
        db.collection("users").document(workerId)
            .collection("services")
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { docs ->

                servicesList.clear()

                if (docs.isEmpty) {
                    tvEmptyServices.visibility = View.VISIBLE
                    rvServices.visibility = View.GONE
                } else {
                    tvEmptyServices.visibility = View.GONE
                    rvServices.visibility = View.VISIBLE

                    for (doc in docs) {
                        val service = doc.toObject(ServiceCatalog::class.java)
                        servicesList.add(service)
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }

    private fun setupButtons() {

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btnReport).setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("workerId", workerId)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btnRequest).setOnClickListener {
            val intent = Intent(this, NewRequestActivity::class.java)
            intent.putExtra("EXTRA_WORKER_UID", workerId)
            intent.putExtra("EXTRA_WORKER_NAME", workerName)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btnChat).setOnClickListener {
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra("serviceId", workerId)
            intent.putExtra("contactName", workerName)
            startActivity(intent)
        }
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationProfile)

        nav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish(); true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, UserMapActivity::class.java))
                    finish(); true
                }
                R.id.nav_chat -> {
                    val intent = Intent(this, ProviderChatActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
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