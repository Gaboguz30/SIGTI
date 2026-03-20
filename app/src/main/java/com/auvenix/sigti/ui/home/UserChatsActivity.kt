package com.auvenix.sigti.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ActivityUserChatsBinding
import com.auvenix.sigti.ui.chat.ChatListActivity
import com.auvenix.sigti.ui.profile.ProfileActivity

class UserChatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserChatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ FIX 5: Esta pantalla ya no es un callejón sin salida.
        //    Redirige inmediatamente a ChatListActivity que tiene
        //    la lógica real de Firebase Realtime Database.
        startActivity(Intent(this, ChatListActivity::class.java))
        finish()
    }
}