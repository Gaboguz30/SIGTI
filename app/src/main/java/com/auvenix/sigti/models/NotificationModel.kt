package com.auvenix.sigti.models

import com.google.firebase.Timestamp

data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "", // ej: "CHAT", "PAGO", "SERVICIO"
    val timestamp: Timestamp? = null,
    val is_read: Boolean = false
)