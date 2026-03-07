package com.auvenix.sigti.data.model

data class LoginResponse(
    val ok: Boolean,
    val message: String? = null,
    val token: String? = null
)