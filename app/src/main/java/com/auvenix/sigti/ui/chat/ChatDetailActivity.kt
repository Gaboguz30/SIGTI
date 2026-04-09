package com.auvenix.sigti.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.widget.TextView
import android.widget.ImageView

import com.auvenix.sigti.ui.home.*
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity

class ChatDetailActivity : AppCompatActivity() {

    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var chatRef: DatabaseReference
    private lateinit var conversationsRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvUnreadBanner: TextView

    private var targetUid = ""
    private var contactName = "Usuario"
    private var myName = "Usuario"
    private var myRole = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)
        val header = findViewById<View>(R.id.header)

        val title = header.findViewById<TextView>(R.id.tvHeaderTitle)
        val back = header.findViewById<ImageView>(R.id.btnBackHeader)

        back.setOnClickListener {
            finish() // Aquí no usamos la función mágica porque el botón de 'Atrás' del header solo debe cerrar la pestaña
        }

        val rv = findViewById<RecyclerView>(R.id.rvChatMessages)
        val input = findViewById<EditText>(R.id.etMessageInput)
        val send = findViewById<ImageView>(R.id.btnSendMessage)
        val subtitle = findViewById<TextView>(R.id.tvChatSubtitle)

        tvUnreadBanner = findViewById(R.id.tvUnreadBanner)

        auth = FirebaseAuth.getInstance()
        val myUid = auth.currentUser?.uid ?: return

        targetUid = intent.getStringExtra("serviceId") ?: ""
        if (targetUid.isEmpty()) {
            Toast.makeText(this, "Error: chat sin usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        subtitle.text = "En línea"

        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        adapter = ChatAdapter(chatMessages)
        rv.adapter = adapter

        conversationsRef = FirebaseDatabase.getInstance().getReference("conversations")

        db.collection("users")
            .document(targetUid)
            .get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: ""
                val apP = doc.getString("apPaterno") ?: ""
                val apM = doc.getString("apMaterno") ?: ""

                val full = listOf(nombre, apP, apM).filter { it.isNotBlank() }.joinToString(" ")
                contactName = if (full.isNotEmpty()) full else "Usuario"
                title.text = contactName
            }

        db.collection("users").document(myUid).get()
            .addOnSuccessListener {
                val nombre = it.getString("nombre") ?: ""
                val apellido = it.getString("apellidoPaterno") ?: ""

                myName = "$nombre $apellido".trim()
                if (myName.isEmpty()) myName = "Usuario"

                myRole = it.getString("role") ?: "SOLICITANTE"
                setupBottomNavigation() // Lo llamamos hasta aquí para saber qué menú pintar
            }

        val roomId = if (myUid < targetUid) "${myUid}_$targetUid" else "${targetUid}_$myUid"

        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(roomId)

        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(s: DataSnapshot, p: String?) {
                val msg = s.child("message").value?.toString() ?: return
                val sender = s.child("sender_uid").getValue(String::class.java) ?: ""
                val time = s.child("timestamp").getValue(Long::class.java) ?: 0

                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
                val mine = sender == myUid

                chatMessages.add(ChatMessage(msg, mine, hora, false))
                adapter.notifyItemInserted(chatMessages.size - 1)
                rv.scrollToPosition(chatMessages.size - 1)
            }

            override fun onChildChanged(s: DataSnapshot, p: String?) {}
            override fun onChildRemoved(s: DataSnapshot) {}
            override fun onChildMoved(s: DataSnapshot, p: String?) {}
            override fun onCancelled(e: DatabaseError) {}
        })

        send.setOnClickListener {
            val texto = input.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener

            val time = System.currentTimeMillis()

            chatRef.push().setValue(
                mapOf(
                    "message" to texto,
                    "sender_uid" to myUid,
                    "timestamp" to time,
                    "seen" to false
                )
            )

            val dataMe = mapOf(
                "lastMessage" to texto,
                "timestamp" to time,
                "withName" to contactName,
                "unreadCount" to 0
            )

            val dataOther = mapOf(
                "lastMessage" to texto,
                "timestamp" to time,
                "withName" to myName,
                "unreadCount" to 1
            )

            conversationsRef.child(myUid).child(targetUid).updateChildren(dataMe)
            conversationsRef.child(targetUid).child(myUid).updateChildren(dataOther)

            input.setText("")
        }
    }

    // 🔥 NAVEGACIÓN SUAVE DINÁMICA (Decide qué menú mostrar)
    private fun setupBottomNavigation() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationChat)

        if (myRole.equals("PRESTADOR", ignoreCase = true)) {
            nav.menu.clear()
            nav.inflateMenu(R.menu.provider_bottom_nav_menu)

            nav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home_provider -> { irAPantalla(ProviderHomeActivity::class.java); true }
                    R.id.nav_catalog -> { irAPantalla(ProviderCatalogActivity::class.java); true }
                    R.id.nav_chat -> { irAPantalla(com.auvenix.sigti.ui.provider.chat.ProviderChatActivity::class.java); true }
                    R.id.nav_jobs -> { irAPantalla(ProviderJobsActivity::class.java); true }
                    R.id.nav_profile -> { irAPantalla(ProfileActivity::class.java); true }
                    else -> false
                }
            }
        } else {
            nav.menu.clear()
            nav.inflateMenu(R.menu.bottom_nav_menu)

            nav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { irAPantalla(HomeActivity::class.java); true }
                    R.id.nav_map -> { irAPantalla(UserMapActivity::class.java); true }
                    R.id.nav_chat -> { irAPantalla(com.auvenix.sigti.ui.provider.chat.ProviderChatActivity::class.java); true }
                    R.id.nav_jobs -> { irAPantalla(ProviderJobsActivity::class.java); true }
                    R.id.nav_profile -> { irAPantalla(ProfileActivity::class.java); true }
                    else -> false
                }
            }
        }
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