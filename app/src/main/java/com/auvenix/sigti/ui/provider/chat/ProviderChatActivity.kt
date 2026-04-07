package com.auvenix.sigti.ui.provider.chat

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderChatBinding
import com.auvenix.sigti.session.SessionManager // 🔥 IMPORTAMOS LA MEMORIA LOCAL
import com.auvenix.sigti.ui.chat.ChatDetailActivity
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProviderChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProviderChatBinding
    private val chatList = mutableListOf<ChatModel>()
    private lateinit var adapter : ChatListAdapter
    private lateinit var dbRef   : DatabaseReference
    lateinit var fullList: List<ChatModel>

    private lateinit var sessionManager: SessionManager // 🔥 VARIABLE DE SESIÓN
    private var myRole = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val poppins = ResourcesCompat.getFont(this, R.font.poppins_medium)

        binding.chipTodos.typeface = poppins
        binding.chipNoLeidos.typeface = poppins
        binding.chipFavoritos.typeface = poppins

        val title = findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "Bandeja de Entrada"

        binding.etSearch.addTextChangedListener {
            adapter.filter(it.toString())
        }

        binding.chipTodos.setOnClickListener {
            adapter.filter("")
        }

        binding.chipNoLeidos.setOnClickListener {
            val filtrados = fullList.filter { it.unreadCount > 0 }
            adapter.chatList.clear()
            adapter.chatList.addAll(filtrados)
            adapter.notifyDataSetChanged()
        }

        binding.chipFavoritos.setOnClickListener {
            val favoritos = fullList.filter { it.name.contains("A") }
            adapter.chatList.clear()
            adapter.chatList.addAll(favoritos)
            adapter.notifyDataSetChanged()
        }

        // 🔥 MAGIA CONTRA EL PARPADEO (Carga en 1 milisegundo)
        myRole = sessionManager.getRole() ?: "SOLICITANTE"
        setupBottomNavigation() // Pinta la barra de INMEDIATO

        // 🔥 AUTO-CURACIÓN: Consultamos Firebase en segundo plano solo para confirmar
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .get()
            .addOnSuccessListener { doc ->
                val realRole = doc.getString("role") ?: "SOLICITANTE"
                // Si la memoria estaba mal, la corrige y repinta, si no, no hace nada visualmente
                if (myRole != realRole) {
                    sessionManager.saveRole(realRole)
                    myRole = realRole
                    setupBottomNavigation()
                }
            }

        setupRecyclerView()
        loadChatsFromFirebase()
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter(chatList) { chat ->
            adapter.setFullList(chatList)
            startActivity(
                Intent(this, ChatDetailActivity::class.java).apply {
                    putExtra("serviceId", chat.id)
                    putExtra("contactName", chat.name)
                }
            )
        }
        binding.rvChats.layoutManager = LinearLayoutManager(this)
        binding.rvChats.adapter = adapter
    }

    private fun loadChatsFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        dbRef = FirebaseDatabase.getInstance()
            .getReference("conversations")
            .child(uid)

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()

                for (child in snapshot.children) {
                    val targetId = child.key ?: continue
                    val lastMessage = child.child("lastMessage").getValue(String::class.java) ?: ""
                    val withName = child.child("withName").getValue(String::class.java) ?: "Usuario"
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L

                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

                    chatList.add(
                        ChatModel(
                            id = targetId,
                            name = withName,
                            lastMessage = lastMessage,
                            time = time
                        )
                    )
                }

                chatList.sortByDescending {
                    snapshot.child(it.id).child("timestamp").getValue(Long::class.java) ?: 0L
                }
                fullList = chatList.toList()
                adapter.setFullList(chatList)
                adapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupBottomNavigation() {
        val nav = binding.bottomNavigationProvider
        nav.menu.clear()

        if (myRole.equals("PRESTADOR", ignoreCase = true)) {
            nav.inflateMenu(R.menu.provider_bottom_nav_menu)
            nav.selectedItemId = R.id.nav_provider_chat

            nav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_provider_home -> start(ProviderHomeActivity::class.java)
                    R.id.nav_provider_jobs -> start(ProviderJobsActivity::class.java)
                    R.id.nav_provider_chat -> true
                    R.id.nav_provider_catalog -> start(ProviderCatalogActivity::class.java)
                    R.id.nav_provider_profile -> start(ProfileActivity::class.java) // 🔥 CORREGIDO (Ya no usa el fantasma)
                    else -> false
                }
            }

        } else {
            nav.inflateMenu(R.menu.bottom_nav_menu)
            nav.selectedItemId = R.id.nav_chat

            nav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> start(com.auvenix.sigti.ui.home.HomeActivity::class.java)
                    R.id.nav_map -> start(com.auvenix.sigti.ui.home.UserMapActivity::class.java)
                    R.id.nav_chat -> true
                    R.id.nav_profile -> start(ProfileActivity::class.java)
                    else -> false
                }
            }
        }
    }

    // 🔥 Le metemos el overridePendingTransition(0,0) aquí para matar las animaciones bruscas del Android
    private fun start(activity: Class<*>): Boolean {
        if (this::class.java == activity) return true

        val intent = Intent(this, activity)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
        return true
    }
}