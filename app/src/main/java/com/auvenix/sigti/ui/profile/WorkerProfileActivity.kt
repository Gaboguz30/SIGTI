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
import com.auvenix.sigti.ui.support.Review
import com.auvenix.sigti.ui.support.ReviewAdapter

import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.home.UserNotificationsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity

// 🔥 IMPORTANTE: Cuando tengas Firebase Storage, asegúrate de tener Glide en tu build.gradle
// import com.bumptech.glide.Glide

class WorkerProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvProfession: TextView
    private lateinit var rvServices: RecyclerView
    private lateinit var ratingBar: RatingBar
    private lateinit var tvRatingNumber: TextView
    private lateinit var tvEmptyServices: TextView
    private lateinit var tvExperience: TextView
    private lateinit var rvReviews: RecyclerView

    // Variables de datos
    private var workerId: String = ""
    private var workerName: String = ""
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewList = mutableListOf<Review>()
    private val servicesList = mutableListOf<ServiceCatalog>()
    private lateinit var adapter: PublicServiceAdapter

    private lateinit var fabAddReview: com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_profile)

        // 1. Cabecera y botón de retroceso (Ya no está duplicado)
        val header = findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "Perfil"
        val back = header.findViewById<ImageView>(R.id.btnBackHeader)

        back.setOnClickListener {
            finish()
        }

        // 2. Obtenemos el ID del trabajador que nos pasaron
        workerId = intent.getStringExtra("EXTRA_WORKER_UID") ?: ""

        if (workerId.isEmpty()) {
            Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 3. Inicializamos todo
        initViews()
        setupRecycler()
        cargarPerfilReal()
        cargarServicios()
        setupButtons()
        setupBottomNav()
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

        setupTabs()
        setupReviews()

        fabAddReview = findViewById(R.id.fabAddReview)
        fabAddReview.setOnClickListener {
            // 🔥 Levantamos el BottomSheet y le pasamos el ID del prestador
            val bottomSheet = com.auvenix.sigti.ui.support.AddReviewBottomSheet(workerId)
            bottomSheet.show(supportFragmentManager, "AddReviewBottomSheet")
        }
    }

    private fun setupTabs() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        tabLayout.addTab(tabLayout.newTab().setText("Servicios"))
        tabLayout.addTab(tabLayout.newTab().setText("Reseñas"))

        // Selección inicial (Sin animación)
        tabLayout.getTabAt(0)?.select()
        rvServices.visibility = View.VISIBLE
        rvReviews.visibility = View.GONE

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        crossfadeAnim(rvReviews, rvServices)
                        fabAddReview.hide() // 🔥 Ocultamos el botón en Servicios
                    }
                    1 -> {
                        crossfadeAnim(rvServices, rvReviews)
                        fabAddReview.show() // 🔥 Mostramos el botón en Reseñas
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // 🔥 LA FUNCIÓN MÁGICA DE ANIMACIÓN (Crossfade)
    private fun crossfadeAnim(viewToHide: View, viewToShow: View) {
        viewToHide.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                viewToHide.visibility = View.GONE
                viewToShow.alpha = 0f
                viewToShow.visibility = View.VISIBLE
                viewToShow.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
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

                // 🔹 NOMBRE
                val nombre = doc.getString("nombre") ?: ""
                val apP = doc.getString("apPaterno") ?: ""
                val apM = doc.getString("apMaterno") ?: ""

                workerName = listOf(nombre, apP, apM)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                tvName.text = workerName

                // 🔹 FOTO DE PERFIL (Ejemplo de cómo usar Glide para borde redondo)
                /* val profileUrl = doc.getString("profileImage") ?: ""
                if (profileUrl.isNotEmpty()) {
                    val ivProfilePic = findViewById<ImageView>(R.id.ivProfilePic)
                    Glide.with(this)
                        .load(profileUrl)
                        .circleCrop() // 🔥 ESTO HACE EL CÍRCULO PERFECTO
                        .into(ivProfilePic)
                }
                */

                // 🔹 PROFESION Y EXPERIENCIA
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

                // 🔹 RATING
                val rating = doc.getString("rating")?.toFloatOrNull() ?: 0f
                ratingBar.rating = rating
                tvRatingNumber.text = if (rating > 0f) rating.toString() else "(0 reseñas)"
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
                    val imageUrl = doc.getString("imageUrl") // Por si ya hay fotos

                    // Pasamos todos los datos al modelo
                    reviewList.add(Review(user, date, comment, rating, imageUrl))
                }

                reviewAdapter = ReviewAdapter(reviewList)
                rvReviews.adapter = reviewAdapter
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

                    // Ordenamos: Activos primero, luego alfabéticamente
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
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationProfile)

        nav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); finish(); true }
                R.id.nav_map -> { startActivity(Intent(this, UserMapActivity::class.java)); finish(); true }
                R.id.nav_chat -> {
                    val intent = Intent(this, ProviderChatActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_notifications -> { startActivity(Intent(this, UserNotificationsActivity::class.java)); finish(); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); finish(); true }
                else -> false
            }
        }
    }
}