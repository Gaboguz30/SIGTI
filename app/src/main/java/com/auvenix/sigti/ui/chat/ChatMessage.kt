package com.auvenix.sigti.ui.chat
data class ChatMessage(
    val text: String,
    val isMine: Boolean,
    val time: String,
    val seen: Boolean = false
)