package com.auvenix.sigti.ui.provider.catalog

// Modelo actualizado con el estado activo/inactivo
data class ServiceCatalog(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val active: Boolean = true // 🔥 Por defecto es true para que los viejos no se rompan
)