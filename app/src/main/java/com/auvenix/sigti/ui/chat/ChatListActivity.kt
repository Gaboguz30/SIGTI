package com.auvenix.sigti.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private val chats = mutableListOf<ChatPreview>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ESTE layout existe y coincide con tu XML
        setContentView(R.layout.activity_chat_list)

        recycler = findViewById(R.id.rvChats)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = ChatListAdapter(chats)
        recycler.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = FirebaseDatabase.getInstance()
            .getReference("conversations")
            .child(uid)

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                chats.clear()

                for (chatSnapshot in snapshot.children) {

                    val serviceId = chatSnapshot.key ?: continue

                    val lastMessage = chatSnapshot
                        .child("lastMessage")
                        .getValue(String::class.java) ?: ""

                    val timestamp = chatSnapshot
                        .child("timestamp")
                        .getValue(Long::class.java) ?: 0L

                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val time = sdf.format(Date(timestamp))

                    chats.add(
                        ChatPreview(
                            serviceId = serviceId,
                            name = serviceId,
                            lastMessage = lastMessage,
                            time = time
                        )
                    )
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}