package com.auvenix.sigti.ui.support

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.session.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyReportsActivity : AppCompatActivity() {

    private lateinit var rvMyReports: RecyclerView
    private lateinit var tvEmptyReports: TextView
    private lateinit var adapter: ReportAdapter
    private val reportList = mutableListOf<ReportModel>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reports)

        sessionManager = SessionManager(this)

        val header = findViewById<View>(R.id.header)
        val title = header.findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "Mis Reportes"

        val back = header.findViewById<ImageView>(R.id.btnBackHeader)
        back.setOnClickListener { finish() }

        rvMyReports = findViewById(R.id.rvMyReports)
        tvEmptyReports = findViewById(R.id.tvEmptyReports)

        rvMyReports.layoutManager = LinearLayoutManager(this)
        adapter = ReportAdapter(reportList)
        rvMyReports.adapter = adapter

        cargarReportes()
    }

    private fun cargarReportes() {
        val myUid = auth.currentUser?.uid ?: return
        val myRole = sessionManager.getRole() ?: "SOLICITANTE"

        val campoFiltro = if (myRole == "PRESTADOR") "reportedWorkerId" else "reporterUid"

        tvEmptyReports.visibility = View.GONE
        rvMyReports.visibility = View.GONE

        db.collection("reports")
            .whereEqualTo(campoFiltro, myUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar reportes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                reportList.clear()

                if (snapshots != null) {
                    for (doc in snapshots) {
                        val report = ReportModel(
                            id = doc.id,
                            incidentType = doc.getString("incidentType") ?: "Incidente",
                            description = doc.getString("description") ?: "",
                            dateReport = doc.getString("dateReport") ?: "",
                            status = doc.getString("status") ?: "Pendiente",
                            isProviderView = (myRole == "PRESTADOR") // 🔥 AQUÍ LE DECIMOS AL MODELO QUIÉN LO VE
                        )
                        reportList.add(report)
                    }
                }

                if (reportList.isEmpty()) {
                    tvEmptyReports.visibility = View.VISIBLE
                    rvMyReports.visibility = View.GONE

                    if (myRole == "PRESTADOR") {
                        tvEmptyReports.text = "¡Felicidades! No tienes reportes en tu contra."
                    } else {
                        tvEmptyReports.text = "No has levantado ningún reporte."
                    }
                } else {
                    tvEmptyReports.visibility = View.GONE
                    rvMyReports.visibility = View.VISIBLE
                }

                adapter.notifyDataSetChanged()
            }
    }
}