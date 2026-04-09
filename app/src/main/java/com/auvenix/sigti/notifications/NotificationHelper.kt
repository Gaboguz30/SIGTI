package com.auvenix.sigti.notifications

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

object NotificationHelper {

    private val db = FirebaseFirestore.getInstance()

    fun enviarNotificacion(
        recipientId: String,
        recipientRole: String,
        title: String,
        body: String,
        type: String // "ALERTA", "PAGO", "INFO"
    ) {
        val notificacion = hashMapOf(
            "recipient_id" to recipientId,
            "recipient_role" to recipientRole,
            "title" to title,
            "body" to body,
            "type" to type,
            "is_read" to false,
            "timestamp" to Timestamp.now()
        )

        // 1. Esto la guarda en la base de datos para que aparezca en la campanita
        db.collection("notifications").add(notificacion)
            .addOnSuccessListener {
                // ¡Éxito! El Cloud Function se encargará de mandar el Push
            }
    }
}