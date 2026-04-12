package com.auvenix.sigti.ui.support

data class ReportModel(
    val id: String = "",
    val incidentType: String = "",
    val description: String = "",
    val dateReport: String = "",
    val status: String = "Pendiente",
    val isProviderView: Boolean = false // 🔥 Nueva bandera
)