package com.auvenix.sigti.ui.home

data class Worker(
    val id: Int,
    val name: String,
    val job: String,
    val availability: String,
    val rating: Double,
    val distance: String,
    val price: String,
    var isSelected: Boolean = false // Para demostrar el borde azul
)