package com.auvenix.sigti.ui.chat

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatDetailActivity : AppCompatActivity() {

    private val chatMessages     = mutableListOf<ChatMessage>()
    private lateinit var adapter : ChatAdapter
    private lateinit var chatRef : DatabaseReference
    private lateinit var conversationsRef: DatabaseReference
    private lateinit var auth    : FirebaseAuth

    private var serviceId   = ""
    private var contactName = "Chat"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        // Vistas
        val rvChatMessages = findViewById<RecyclerView>(R.id.rvChatMessages)
        val etMessageInput = findViewById<EditText>(R.id.etMessageInput)
        val btnSendMessage = findViewById<ImageView>(R.id.btnSendMessage)
        val tvChatTitle    = findViewById<TextView>(R.id.tvChatTitle)
        val tvChatSubtitle = findViewById<TextView>(R.id.tvChatSubtitle)
        val btnBack        = findViewById<ImageView>(R.id.btnBackChat)

        // Datos del intent
        serviceId   = intent.getStringExtra("serviceId")   ?: "default_chat"
        contactName = intent.getStringExtra("contactName") ?: "Chat"

        // Nombre real
        tvChatTitle.text    = contactName
        tvChatSubtitle.text = "En línea"     // puedes conectar esto a Firestore si quieres

        // Btn regresar
        btnBack.setOnClickListener { finish() }

        // ── RecyclerView ─────────────────────────────────────────
        rvChatMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true   // los mensajes arrancan desde abajo
        }
        adapter = ChatAdapter(chatMessages)
        rvChatMessages.adapter = adapter

        // ── Firebase ─────────────────────────────────────────────
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        chatRef = FirebaseDatabase.getInstance()
            .getReference("chats")
            .child(serviceId)
            .child("messages")

        conversationsRef = FirebaseDatabase.getInstance()
            .getReference("conversations")

        // ── Escuchar mensajes en tiempo real ─────────────────────
        chatRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message   = snapshot.child("message").getValue(String::class.java)    ?: return
                val sender    = snapshot.child("sender_uid").getValue(String::class.java) ?: ""
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java)

                val isMine = sender == uid
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

        // Enviar mensaje
        btnSendMessage.setOnClickListener {
            val texto = etMessageInput.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener

            val timestamp = System.currentTimeMillis()

            // Guardar mensaje en Realtime Database
            chatRef.push().setValue(
                hashMapOf(
                    "message"    to texto,
                    "sender_uid" to uid,
                    "timestamp"  to timestamp
                )
            )

            // Actualizar vista previa en la lista de chats (para ambos usuarios)
            val preview = hashMapOf<String, Any>(
                "lastMessage" to texto,
                "timestamp"   to timestamp,
                "serviceId"   to serviceId,
                "withName"    to contactName
            )
            conversationsRef.child(uid).child(serviceId).setValue(preview)

            etMessageInput.setText("")
        }
    }
}