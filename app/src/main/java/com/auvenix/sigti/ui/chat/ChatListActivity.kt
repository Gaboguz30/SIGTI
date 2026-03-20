package com.auvenix.sigti.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    private lateinit var adapter: ChatListAdapter
    private val chats = mutableListOf<ChatPreview>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        recycler = findViewById(R.id.rvChats)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = ChatListAdapter(chats)
        recycler.adapter = adapter

        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = FirebaseDatabase.getInstance()
            .getReference(Constants.NODE_CONVERSATIONS)
            .child(myUid)

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                chats.clear()

                for (chatSnapshot in snapshot.children) {
                    val serviceId   = chatSnapshot.key ?: continue
                    val lastMessage = chatSnapshot.child("lastMessage").getValue(String::class.java) ?: ""
                    val timestamp   = chatSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    val withName = chatSnapshot.child("withName").getValue(String::class.java)
                        ?: "Usuario"

                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

                    chats.add(
                        ChatPreview(
                            serviceId   = serviceId,
                            name        = withName,  //NOMBRE REAL DEL USUARIO
                            lastMessage = lastMessage,
                            time        = time
                        )
                    )
                }

                // Ordenar por más reciente
                chats.sortByDescending { chat ->
                    snapshot.child(chat.serviceId)
                        .child("timestamp").getValue(Long::class.java) ?: 0L
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

