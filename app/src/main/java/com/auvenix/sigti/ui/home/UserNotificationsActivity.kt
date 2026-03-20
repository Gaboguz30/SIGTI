package com.auvenix.sigti.ui.home

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
import com.auvenix.sigti.databinding.ActivityUserNotificationsBinding
import com.auvenix.sigti.ui.chat.ChatListActivity
import com.auvenix.sigti.ui.profile.ProfileActivity
import com.auvenix.sigti.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class AppNotification(
    val id        : String  = "",
    val type      : String  = "",
    val title     : String  = "",
    val body      : String  = "",
    val timestamp : Long    = 0L,
    val read      : Boolean = false
)

class NotificationsAdapter(
    private val items: MutableList<AppNotification>,
    private val onItemClick: (AppNotification) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle : TextView = view.findViewById(R.id.tvNotifTitle)
        val tvBody  : TextView = view.findViewById(R.id.tvNotifBody)
        val tvTime  : TextView = view.findViewById(R.id.tvNotifTime)
        val tvType  : TextView = view.findViewById(R.id.tvNotifType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val notif = items[position]

        holder.tvTitle.text = notif.title
        holder.tvBody.text  = notif.body

        holder.tvType.text = when (notif.type) {
            Constants.NOTIF_TYPE_CHAT     -> "💬 Mensaje"
            Constants.NOTIF_TYPE_REQUEST  -> "📋 Solicitud"
            Constants.NOTIF_TYPE_ALERT    -> "🔔 Alerta"
            Constants.NOTIF_TYPE_REMINDER -> "📅 Recordatorio"
            else                          -> "Notificación"
        }

        holder.tvTime.text = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            .format(Date(notif.timestamp))

        // Leídas aparecen más opacas
        holder.itemView.alpha = if (notif.read) 0.55f else 1.0f

        holder.itemView.setOnClickListener { onItemClick(notif) }
    }

    override fun getItemCount() = items.size
}

class UserNotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserNotificationsBinding
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val notifList = mutableListOf<AppNotification>()
    private lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadNotifications()
        setupBottomNav()
    }

    private fun setupRecyclerView() {
        adapter = NotificationsAdapter(notifList) { notif ->
            markAsRead(notif)
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    //  CARGAR NOTIFICACIONES DESDE FIRESTORE
    //  Ruta: users/{uid}/notifications — ordenadas por fecha desc
    private fun loadNotifications() {
        val uid = auth.currentUser?.uid ?: return

        db.collection(Constants.COLLECTION_USERS)
            .document(uid)
            .collection(Constants.COLLECTION_NOTIFICATIONS)   // ← CORREGIDO (URL eliminada)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                notifList.clear()

                for (doc in snapshot.documents) {
                    notifList.add(
                        AppNotification(
                            id        = doc.id,
                            type      = doc.getString("type")      ?: "",
                            title     = doc.getString("title")     ?: "",
                            body      = doc.getString("body")      ?: "",
                            timestamp = doc.getLong("timestamp")   ?: 0L,
                            read      = doc.getBoolean("read")     ?: false
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
    }

    //  MARCAR COMO LEÍDA
    private fun markAsRead(notif: AppNotification) {
        val uid = auth.currentUser?.uid ?: return
        db.collection(Constants.COLLECTION_USERS)
            .document(uid)
            .collection(Constants.COLLECTION_NOTIFICATIONS)   // ← CORREGIDO (URL eliminada)
            .document(notif.id)
            .update("read", true)
    }

    //  BOTTOM NAVIGATION
    private fun setupBottomNav() {
        binding.bottomNavigation.selectedItemId = R.id.nav_notifications

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, UserMapActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, UserChatsActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                R.id.nav_notifications -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0); finish(); true
                }
                else -> false
            }
        }
    }
}