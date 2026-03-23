package com.auvenix.sigti.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatListActivity : AppCompatActivity() {

    private lateinit var recycler   : RecyclerView
    private lateinit var adapter    : ChatListAdapter
    private lateinit var tvEmpty    : TextView
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
    }
}