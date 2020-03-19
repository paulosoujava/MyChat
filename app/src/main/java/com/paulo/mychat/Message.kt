package com.paulo.mychat

data class Message(
    val text: String = "",
    val timestamp: Long = 0,
    val fromId: String = "",
    val toId: String = ""
)