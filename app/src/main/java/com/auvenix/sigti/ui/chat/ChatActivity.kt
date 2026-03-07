package com.auvenix.sigti.ui.chat

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

class ChatActivity : AppCompatActivity() {

    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 1. Botón de regresar
        findViewById<ImageView>(R.id.btnBackChat).setOnClickListener { finish() }

        // 2. Configurar la lista de mensajes
        val rvChatMessages = findViewById<RecyclerView>(R.id.rvChatMessages)
        rvChatMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Para que el chat empiece desde abajo
        }

        // Cargamos los mensajes de tu diseño
        chatMessages.add(ChatMessage("Hola, nos indicas que un cortocircuito, solo esta disponible hoy?", true))
        chatMessages.add(ChatMessage("Si, puedo llegar en a las 4 pm. Me compartes ubicación?", false))
        chatMessages.add(ChatMessage("Va, te mando la direccion y la solicitud con los detalles", true))

        adapter = ChatAdapter(chatMessages)
        rvChatMessages.adapter = adapter

        // 3. Funcionalidad de enviar mensaje
        val etMessageInput = findViewById<EditText>(R.id.etMessageInput)
        val btnSendMessage = findViewById<ImageView>(R.id.btnSendMessage)

        btnSendMessage.setOnClickListener {
            val texto = etMessageInput.text.toString().trim()
            if (texto.isNotEmpty()) {
                // Agregar mi mensaje a la lista
                chatMessages.add(ChatMessage(texto, true))

                // Avisar al adaptador y scrollear hasta abajo
                adapter.notifyItemInserted(chatMessages.size - 1)
                rvChatMessages.scrollToPosition(chatMessages.size - 1)

                // Limpiar la caja de texto
                etMessageInput.text.clear()
            }
        }
    }
}