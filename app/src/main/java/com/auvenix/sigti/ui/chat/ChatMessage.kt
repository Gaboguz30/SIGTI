package com.auvenix.sigti.ui.chat
data class ChatMessage(
    val message: String,
    val mine: Boolean,
    val time: String,
    val seen: Boolean,
    val type: String = "text"
)