package com.auvenix.sigti.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.databinding.ActivityChatDetailBinding
import com.auvenix.sigti.ui.provider.chat.ChatMessageAdapter
import com.auvenix.sigti.ui.provider.chat.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatDetailBinding
    private val messages = mutableListOf<MessageModel>()
    private lateinit var adapter: ChatMessageAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var chatId = "sala_chat_prueba"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val workerName = intent.getStringExtra("EXTRA_WORKER_NAME") ?: "Trabajador"
        binding.tvChatSubtitle.text = workerName
        binding.tvChatTitle.text = "Chat de Soporte"

        binding.btnBackChat.setOnClickListener { finish() }

        setupRecyclerView()
        escucharMensajesFirebase()

        binding.btnSendMessage.setOnClickListener { enviarMensajeFirebase() }
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter(messages)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.rvChatMessages.layoutManager = layoutManager
        binding.rvChatMessages.adapter = adapter
    }

    private fun escucharMensajesFirebase() {
        val miUid = auth.currentUser?.uid ?: return
        db.collection("chats").document(chatId).collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                messages.clear()
                for (doc in snapshots!!) {
                    val texto = doc.getString("texto") ?: ""
                    val senderUid = doc.getString("senderUid") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    val isMine = (senderUid == miUid)
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val hora = sdf.format(Date(timestamp))
                    messages.add(MessageModel(texto, hora, isMine))
                }
                adapter.notifyDataSetChanged()
                if (messages.isNotEmpty()) binding.rvChatMessages.scrollToPosition(messages.size - 1)
            }
    }

    private fun enviarMensajeFirebase() {
        val texto = binding.etMessageInput.text.toString().trim()
        val miUid = auth.currentUser?.uid ?: return
        if (texto.isNotEmpty()) {
            val timestamp = System.currentTimeMillis()
            val nuevoMensaje = hashMapOf("texto" to texto, "senderUid" to miUid, "timestamp" to timestamp)
            db.collection("chats").document(chatId).collection("mensajes")
                .add(nuevoMensaje)
                .addOnSuccessListener { binding.etMessageInput.text.clear() }
        }
    }
}