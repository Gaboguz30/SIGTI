package com.auvenix.sigti.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.auvenix.sigti.ui.support.Review
import com.auvenix.sigti.ui.support.ReviewAdapter

import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.support.ReviewActivity1
import com.bumptech.glide.Glide

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
    private lateinit var ivProfilePic: ImageView

    private val servicesList = mutableListOf<ServiceCatalog>()
    private lateinit var adapter: PublicServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_profile)

        val header = findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "Perfil"
        val back = header.findViewById<ImageView>(R.id.btnBackHeader)

        back.setOnClickListener {
            finish()
        }

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

        tabLayout.getTabAt(0)?.select()
        rvServices.visibility = View.VISIBLE
        rvReviews.visibility = View.GONE

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
        tvExperience = findViewById(R.id.tvProfileExperience)
        rvReviews = findViewById(R.id.rvReviews)
        ivProfilePic = findViewById(R.id.ivProfilePic)
        setupTabs()
        setupReviews()
    }

    private fun setupRecycler() {
        rvServices.layoutManager = LinearLayoutManager(this)
        adapter = PublicServiceAdapter(servicesList)
        rvServices.adapter = adapter
    }

    private fun cargarPerfilReal() {
        db.collection("users").document(workerId)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) return@addOnSuccessListener
                val documentacion = doc.get("documentacion") as? Map<*, *>
                val photoUrl = documentacion?.get("url_selfie")?.toString()

                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .circleCrop()
                    .into(ivProfilePic)

                val nombre = doc.getString("nombre") ?: ""
                val apP = doc.getString("apPaterno") ?: ""
                val apM = doc.getString("apMaterno") ?: ""

                workerName = listOf(nombre, apP, apM).filter { it.isNotBlank() }.joinToString(" ")
                tvName.text = workerName

                val oficiosRaw = doc.get("oficios")
                var profesion = ""
                var experiencia = ""

                if (oficiosRaw is List<*> && oficiosRaw.isNotEmpty()) {
                    val primero = oficiosRaw[0]
                    if (primero is Map<*, *>) {
                        profesion = primero["nombre"]?.toString() ?: ""
                        val exp = primero["anios_experiencia"]
                        experiencia = if (exp != null) "Experiencia: $exp años" else ""
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

                // 🔥 ESTRELLAS Y NÚMEROS EN TIEMPO REAL 🔥
                db.collection("reviews")
                    .whereEqualTo("technician_uid", workerId)
                    .addSnapshotListener { reviews, e ->
                        if (e != null || reviews == null) return@addSnapshotListener

                        var total = 0f
                        val count = reviews.size()

                        for (r in reviews) {
                            val rating = r.getDouble("rating")?.toFloat() ?: r.getLong("rating")?.toFloat() ?: 0f
                            total += rating
                        }

                        val promedio = if (count > 0) total / count else 0f
                        ratingBar.rating = promedio

                        if (count > 0) {
                            tvRatingNumber.text = String.format("%.1f (%d reseñas)", promedio, count)
                        } else {
                            tvRatingNumber.text = "(0 reseñas)"
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔥 LISTA DE RESEÑAS EN TIEMPO REAL 🔥
    private fun setupReviews() {
        rvReviews.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(reviewList)
        rvReviews.adapter = reviewAdapter

        db.collection("reviews")
            .whereEqualTo("technician_uid", workerId)
            .addSnapshotListener { documents, e ->
                if (e != null || documents == null) return@addSnapshotListener

                reviewList.clear()

                for (doc in documents) {
                    val user = doc.getString("userName") ?: "Usuario"
                    val comment = doc.getString("comment") ?: ""
                    val rating = doc.getDouble("rating")?.toFloat() ?: doc.getLong("rating")?.toFloat() ?: 0f
                    val timestamp = doc.getTimestamp("created_at")?.toDate()

                    val formato = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                    val date = if (timestamp != null) formato.format(timestamp) else ""
                    val imageUrl = doc.getString("imageUrl")

                    reviewList.add(Review(user, date, comment, rating, imageUrl))
                }

                reviewAdapter.notifyDataSetChanged()
            }
    }

    private fun cargarServicios() {
        db.collection("users").document(workerId)
            .collection("services")
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
                        val service = doc.toObject(ServiceCatalog::class.java).copy(id = doc.id)
                        servicesList.add(service)
                    }
                    servicesList.sortWith(compareBy({ !it.active }, { it.name.lowercase() }))
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun setupButtons() {
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

        findViewById<TextView>(R.id.btnReview).setOnClickListener {
            val intent = Intent(this, ReviewActivity1::class.java)
            intent.putExtra("providerId", workerId)
            startActivity(intent)
        }
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationProfile)

        nav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> { irAPantalla(HomeActivity::class.java); true }
                R.id.nav_map -> { irAPantalla(UserMapActivity::class.java); true }
                R.id.nav_chat -> { irAPantalla(ProviderChatActivity::class.java); true }
                R.id.nav_jobs -> { irAPantalla(com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity::class.java); true }
                R.id.nav_profile -> { irAPantalla(ProfileActivity::class.java); true }
                else -> false
            }
        }
    }

    private fun irAPantalla(activityClass: Class<*>) {
        if (this::class.java == activityClass) return
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}