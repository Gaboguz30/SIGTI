package com.auvenix.sigti.ui.provider.chat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderChatBinding

// IMPORTS
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity

class ProviderChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderChatBinding
    private val chatList = mutableListOf<ChatModel>()
    private lateinit var adapter: ChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        cargarChatsFalsos()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_chat

        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home -> {
                    startActivity(Intent(this, ProviderHomeActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_provider_jobs -> {
                    startActivity(Intent(this, ProviderJobsActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_provider_chat -> true // Ya estamos aquí
                R.id.nav_provider_catalog -> {
                    startActivity(Intent(this, ProviderCatalogActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_provider_profile -> {
                    startActivity(Intent(this, ProviderProfileActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter(chatList) { chat ->
            Toast.makeText(this, "Abriendo chat con: ${chat.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvChats.layoutManager = LinearLayoutManager(this)
        binding.rvChats.adapter = adapter
    }

    private fun cargarChatsFalsos() {
        chatList.clear()
        chatList.add(ChatModel("1", "Vianca Ramírez", "¡Muchas gracias por la reparación!", "14:52"))
        chatList.add(ChatModel("2", "Roberto Sánchez", "¿A qué hora llegas mañana al domicilio?", "Ayer"))
        chatList.add(ChatModel("3", "Edgar Ramírez", "Te mandé la foto del medidor, revísala.", "Lunes"))
        adapter.notifyDataSetChanged()
    }
}