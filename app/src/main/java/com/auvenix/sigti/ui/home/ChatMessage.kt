package com.auvenix.sigti.ui.chat

data class ChatMessage(
    val id: Long,
    val text: String,
    val isMine: Boolean,
    val timestamp: String
)