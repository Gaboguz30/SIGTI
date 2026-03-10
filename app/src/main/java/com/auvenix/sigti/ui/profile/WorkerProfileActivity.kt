package com.auvenix.sigti.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.ui.chat.ChatActivity
import com.auvenix.sigti.ui.request.NewRequestActivity
import com.auvenix.sigti.ui.support.ReportActivity
import com.google.android.material.button.MaterialButton

class WorkerProfileActivity : AppCompatActivity() {

    private var workerId: String = "worker_demo_001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_profile)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnReport = findViewById<TextView>(R.id.btnReport)

        val btnRequest = findViewById<MaterialButton>(R.id.btnRequest)
        val btnChat = findViewById<MaterialButton>(R.id.btnChat)

        val rvServices = findViewById<RecyclerView>(R.id.rvServices)

        btnBack.setOnClickListener {
            finish()
        }

        btnReport.setOnClickListener {

            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("workerId", workerId)

            startActivity(intent)
        }

        btnRequest.setOnClickListener {

            val intent = Intent(this, NewRequestActivity::class.java)
            startActivity(intent)
        }

        btnChat.setOnClickListener {

            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        rvServices.layoutManager = LinearLayoutManager(this)

        val services = listOf(
            ServiceItem("Instalación eléctrica","Cableado completo","300"),
            ServiceItem("Revisión de corto","Diagnóstico eléctrico","200"),
            ServiceItem("Mantenimiento","Revisión general","250")
        )

        rvServices.adapter = ServiceAdapter(services)
    }
}