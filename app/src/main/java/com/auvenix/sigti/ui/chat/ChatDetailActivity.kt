package com.auvenix.sigti.ui.chat

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatDetailActivity : AppCompatActivity() {

    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var chatRef: DatabaseReference
    private lateinit var conversationsRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var serviceId = "service_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        val rvChatMessages = findViewById<RecyclerView>(R.id.rvChatMessages)
        val etMessageInput = findViewById<EditText>(R.id.etMessageInput)
        val btnSendMessage = findViewById<ImageView>(R.id.btnSendMessage)

        rvChatMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        adapter = ChatAdapter(chatMessages)
        rvChatMessages.adapter = adapter

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        // si en el futuro mandas serviceId por intent
        serviceId = intent.getStringExtra("serviceId") ?: "service_1"

        chatRef = FirebaseDatabase.getInstance()
            .getReference("chats")
            .child(serviceId)
            .child("messages")

        conversationsRef = FirebaseDatabase.getInstance()
            .getReference("conversations")

        // escuchar mensajes
        chatRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val message = snapshot.child("message").getValue(String::class.java)
                val sender = snapshot.child("sender_uid").getValue(String::class.java)
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java)

                if (message != null) {

                    val isMine = sender == uid

                    val time = timestamp?.let {
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        sdf.format(Date(it))
                    } ?: ""

                    chatMessages.add(ChatMessage(message, isMine, time))

                    adapter.notifyItemInserted(chatMessages.size - 1)
                    rvChatMessages.scrollToPosition(chatMessages.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}

        })

        // enviar mensaje
        btnSendMessage.setOnClickListener {

            val texto = etMessageInput.text.toString().trim()

            if (texto.isNotEmpty()) {

                val timestamp = System.currentTimeMillis()

                val msg = HashMap<String, Any>()
                msg["message"] = texto
                msg["sender_uid"] = uid
                msg["timestamp"] = timestamp

                chatRef.push().setValue(msg)

                // guardar conversación para la lista de chats
                val conversationData = HashMap<String, Any>()
                conversationData["lastMessage"] = texto
                conversationData["timestamp"] = timestamp
                conversationData["serviceId"] = serviceId

                conversationsRef
                    .child(uid)
                    .child(serviceId)
                    .setValue(conversationData)

                etMessageInput.setText("")
            }
        }
    }
}