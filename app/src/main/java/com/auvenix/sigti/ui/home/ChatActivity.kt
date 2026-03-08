package com.auvenix.sigti.ui.chat

import android.content.Intent
import android.os.Bundle
import android.os.Bundleimport android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.databinding.ActivityChatBinding
import com.auvenix.sigti.ui.home.HomeActivity
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWorkerInfo()
        setupRecyclerView()
        setupListeners()
        loadMockConversation()
    }

    private fun setupWorkerInfo() {
        // Recibir nombre desde Intent o usar default
        val name = intent.getStringExtra("WORKER_NAME") ?: "Vianca Ramirez"
        binding.tvWorkerName.text = name
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true // Los mensajes empiezan desde abajo
            }
            adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSecurity.setOnClickListener {
            Toast.makeText(this, "Abriendo panel de seguridad...", Toast.LENGTH_SHORT).show()
        }

        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.navHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun loadMockConversation() {
        messages.add(ChatMessage(1, "Hola, necesito revisar un corto en mi sala ¿Está disponible hoy?", true, "10:00 AM"))
        messages.add(ChatMessage(2, "Sí, puedo después de las 5 p.m. ¿Me compartes ubicación?", false, "10:05 AM"))
        chatAdapter.notifyDataSetChanged()
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isNotEmpty()) {
            // Mensaje del usuario
            val userMsg = ChatMessage(System.currentTimeMillis(), text, true, "Ahora")
            messages.add(userMsg)
            chatAdapter.notifyItemInserted(messages.size - 1)
            binding.etMessage.text.clear()
            binding.rvChat.smoothScrollToPosition(messages.size - 1)

            // Simular respuesta del prestador
            Handler(Looper.getMainLooper()).postDelayed({
                simulateResponse()
            }, 1500)
        }
    }

    private fun simulateResponse() {
        val responses = listOf(
            "Perfecto, compárteme ubicación por favor.",
            "De acuerdo, envíame los detalles.",
            "Sí, estaré atento a tu solicitud.",
            "Entendido, nos vemos más tarde."
        )
        val randomResponse = ChatMessage(System.currentTimeMillis(), responses.random(), false, "Ahora")
        messages.add(randomResponse)
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.smoothScrollToPosition(messages.size - 1)
    }
}