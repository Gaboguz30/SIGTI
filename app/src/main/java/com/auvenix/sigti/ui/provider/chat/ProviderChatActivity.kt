package com.auvenix.sigti.ui.provider.chat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityProviderChatBinding
import com.auvenix.sigti.ui.chat.ChatDetailActivity
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.auvenix.sigti.ui.provider.catalog.ProviderCatalogActivity
import com.auvenix.sigti.ui.provider.home.ProviderHomeActivity
import com.auvenix.sigti.ui.provider.jobs.ProviderJobsActivity
import com.auvenix.sigti.ui.provider.profile.ProviderProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ProviderChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProviderChatBinding
    private val chatList         = mutableListOf<ChatModel>()
    private lateinit var adapter : ChatListAdapter
    private lateinit var dbRef   : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. PRIMERO el adapter
        setupRecyclerView()
        // 2. DESPUÉS cargamos los datos
        loadChatsFromFirebase()
        // 3. Menú
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        // Redirigimos a la ÚNICA pantalla de chat (ChatDetailActivity)
        adapter = ChatListAdapter(chatList) { chat ->
            startActivity(
                Intent(this, ChatDetailActivity::class.java).apply {
                    putExtra("serviceId",   chat.id)
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
                    val targetId    = child.key ?: continue
                    val lastMessage = child.child("lastMessage").getValue(String::class.java) ?: ""
                    val withName    = child.child("withName").getValue(String::class.java)    ?: "Usuario"
                    val timestamp   = child.child("timestamp").getValue(Long::class.java)     ?: 0L

                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

                    chatList.add(
                        ChatModel(
                            id          = targetId,
                            name        = withName,
                            lastMessage = lastMessage,
                            time        = time
                        )
                    )
                }

                // Ordenar por más reciente primero
                chatList.sortByDescending { chat ->
                    snapshot.child(chat.id).child("timestamp").getValue(Long::class.java) ?: 0L
                }

                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationProvider.selectedItemId = R.id.nav_provider_chat

        binding.bottomNavigationProvider.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_provider_home -> { startActivity(Intent(this, ProviderHomeActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_provider_jobs -> { startActivity(Intent(this, ProviderJobsActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_provider_chat    -> true
                R.id.nav_provider_catalog -> { startActivity(Intent(this, ProviderCatalogActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                R.id.nav_provider_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); overridePendingTransition(0, 0); finish(); true }
                else -> false
            }
        }
    }
}