package com.auvenix.sigti.ui.chat

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.home.HomeActivity
import com.auvenix.sigti.ui.home.UserMapActivity
import com.auvenix.sigti.ui.home.UserNotificationsActivity
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatListActivity : AppCompatActivity() {

    private lateinit var recycler   : RecyclerView
    private lateinit var adapter    : ChatListAdapter
    // Borré tvEmpty porque no lo estabas usando y a veces causa advertencias
    private val chats               = mutableListOf<ChatPreview>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        recycler = findViewById(R.id.rvChats)

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ChatListAdapter(chats)
        recycler.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("conversations")
            .child(uid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    chats.clear()

                    for (child in snapshot.children) {
                        val serviceId   = child.key ?: continue
                        val lastMessage = child.child("lastMessage").getValue(String::class.java) ?: ""
                        val timestamp   = child.child("timestamp").getValue(Long::class.java)    ?: 0L
                        val withName    = child.child("withName").getValue(String::class.java)   ?: "Usuario"
                        val time        = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

                        chats.add(ChatPreview(serviceId, withName, lastMessage, time))
                    }

                    // Ordenar por más reciente
                    chats.sortByDescending { chat ->
                        snapshot.child(chat.serviceId).child("timestamp").getValue(Long::class.java) ?: 0L
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)

        // ¡ESTO ES NUEVO! Le dice al menú que "ilumine" el ícono de chat
        bottomNavigation?.selectedItemId = R.id.nav_chat

        // Aquí ya conectamos los clics para viajar a otras pantallas
        bottomNavigation?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, UserMapActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_chat -> true
                R.id.nav_notifications -> {
                    startActivity(Intent(this, UserNotificationsActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                else -> false
            }
        }
    }
}