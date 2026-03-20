package com.auvenix.sigti.ui.chat   // ← paquete correcto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter : ChatListAdapter
    private val chats = mutableListOf<ChatPreview>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        recycler = findViewById(R.id.rvChats)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ChatListAdapter(chats)
        recycler.adapter = adapter

        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference(Constants.NODE_CONVERSATIONS)
            .child(myUid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    chats.clear()
                    for (child in snapshot.children) {
                        val serviceId   = child.key ?: continue
                        val lastMessage = child.child("lastMessage").getValue(String::class.java) ?: ""
                        val timestamp   = child.child("timestamp").getValue(Long::class.java)   ?: 0L
                        val withName    = child.child("withName").getValue(String::class.java)  ?: "Usuario"
                        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
                        chats.add(ChatPreview(serviceId, withName, lastMessage, time))
                    }
                    chats.sortByDescending { chat ->
                        snapshot.child(chat.serviceId).child("timestamp").getValue(Long::class.java) ?: 0L
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}