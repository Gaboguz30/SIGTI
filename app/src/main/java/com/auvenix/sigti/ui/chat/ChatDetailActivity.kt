package com.auvenix.sigti.ui.chat

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.bottomnavigation.BottomNavigationView // 🔥 IMPORT NUEVO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

// 🔥 IMPORTS DE NAVEGACIÓN (Para ambos roles)
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.home.UserNotificationsActivity
import com.auvenix.sigti.ui.profile.ProfileActivity

import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.chat.ProviderChatActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity

class ChatDetailActivity : AppCompatActivity() {

    private val chatMessages     = mutableListOf<ChatMessage>()
    private lateinit var adapter : ChatAdapter
    private lateinit var chatRef : DatabaseReference
    private lateinit var conversationsRef: DatabaseReference
    private lateinit var auth    : FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private var targetUid   = ""
    private var contactName = "Chat"
    private var myName = "Cargando..."
    private var myRole = "" // Variable para guardar si soy PRESTADOR o SOLICITANTE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        val rvChatMessages = findViewById<RecyclerView>(R.id.rvChatMessages)
        val etMessageInput = findViewById<EditText>(R.id.etMessageInput)
        val btnSendMessage = findViewById<ImageView>(R.id.btnSendMessage)
        val tvChatTitle    = findViewById<TextView>(R.id.tvChatTitle)
        val tvChatSubtitle = findViewById<TextView>(R.id.tvChatSubtitle)
        val btnBack        = findViewById<ImageView>(R.id.btnBackChat)

        targetUid   = intent.getStringExtra("serviceId")   ?: "default_chat"
        contactName = intent.getStringExtra("contactName") ?: "Chat"

        tvChatTitle.text    = contactName
        tvChatSubtitle.text = "En línea"

        btnBack.setOnClickListener { finish() }

        rvChatMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        adapter = ChatAdapter(chatMessages)
        rvChatMessages.adapter = adapter

        auth = FirebaseAuth.getInstance()
        val myUid = auth.currentUser?.uid ?: return

        // 🔥 Vamos a Firestore por tu nombre real y tu ROL
        db.collection("users").document(myUid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellido = doc.getString("apellidoPaterno") ?: ""
                    myName = "$nombre $apellido".trim()

                    // Guardamos el rol para la barra de navegación
                    myRole = doc.getString("role") ?: "SOLICITANTE"

                    if (myName.isEmpty()) myName = "Usuario"

                    // Como ya sabemos el rol, configuramos la barra
                    setupBottomNavigation()
                } else {
                    myName = "Usuario"
                }
            }

        val roomId = if (myUid < targetUid) "${myUid}_$targetUid" else "${targetUid}_$myUid"

        chatRef = FirebaseDatabase.getInstance()
            .getReference("chats")
            .child(roomId)
            .child("messages")

        conversationsRef = FirebaseDatabase.getInstance()
            .getReference("conversations")

        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message   = snapshot.child("message").getValue(String::class.java)    ?: return
                val sender    = snapshot.child("sender_uid").getValue(String::class.java) ?: ""
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java)

                val isMine = sender == myUid
                val time   = timestamp?.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
                } ?: ""

                chatMessages.add(ChatMessage(message, isMine, time))
                adapter.notifyItemInserted(chatMessages.size - 1)
                rvChatMessages.scrollToPosition(chatMessages.size - 1)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        btnSendMessage.setOnClickListener {
            val texto = etMessageInput.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener

            val timestamp = System.currentTimeMillis()

            chatRef.push().setValue(
                hashMapOf(
                    "message"    to texto,
                    "sender_uid" to myUid,
                    "timestamp"  to timestamp
                )
            )

            val previewForMe = hashMapOf<String, Any>(
                "lastMessage" to texto,
                "timestamp"   to timestamp,
                "serviceId"   to targetUid,
                "withName"    to contactName
            )
            conversationsRef.child(myUid).child(targetUid).setValue(previewForMe)

            val previewForThem = hashMapOf<String, Any>(
                "lastMessage" to texto,
                "timestamp"   to timestamp,
                "serviceId"   to myUid,
                "withName"    to myName
            )
            conversationsRef.child(targetUid).child(myUid).setValue(previewForThem)

            etMessageInput.setText("")
        }
    }

    // ==========================================
    // MAGIA NUEVA: Barra de Navegación Inteligente
    // ==========================================
    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationChat)

        // Dependiendo de mi rol, inflo el menú correcto en la barra de abajo
        if (myRole == "PRESTADOR") {
            bottomNavigation.menu.clear()
            bottomNavigation.inflateMenu(R.menu.provider_bottom_nav_menu)
            bottomNavigation.selectedItemId = R.id.nav_provider_chat

            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_provider_home -> { startActivity(Intent(this, ProviderHomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_provider_jobs -> { startActivity(Intent(this, ProviderJobsActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_provider_chat -> true // Ya estamos aquí
                    R.id.nav_provider_catalog -> { startActivity(Intent(this, ProviderCatalogActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_provider_profile -> { startActivity(Intent(this, ProviderProfileActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    else -> false
                }
            }
        } else {
            // Si soy Solicitante (o no encontró el rol)
            bottomNavigation.menu.clear()
            bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
            bottomNavigation.selectedItemId = R.id.nav_chat

            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_map -> { startActivity(Intent(this, UserMapActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_chat -> true // Ya estamos aquí
                    R.id.nav_notifications -> { startActivity(Intent(this, UserNotificationsActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                    else -> false
                }
            }
        }
    }
}