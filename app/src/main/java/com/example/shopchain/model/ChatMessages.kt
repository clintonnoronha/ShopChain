package com.example.shopchain.model

data class ChatMessages(
    val fromId: String = "",
    val id: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val fileName: String = "",
    val timestamp: Long = -1,
    val toId: String = ""
)