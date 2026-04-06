package com.auvenix.sigti.ui.support

// 🔥 EL ÚNICO Y VERDADERO MODELO DE REVIEW
data class Review(
    val user: String = "",
    val date: String = "",
    val comment: String = "",
    val rating: Float = 0f,
    val imageUrl: String? = null
)