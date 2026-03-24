package com.auvenix.sigti.ui.provider.catalog

// Modelo actualizado para incluir descripción
data class ServiceCatalog(
    val id: String = "",
    val name: String = "",
    val description: String = "", // 🔥 Nuevo campo
    val price: Double = 0.0
)