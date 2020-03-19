package com.paulo.mychat

data class Contact(
    val uuid: String = "",
    val username: String = "",
    val lastMessage: String = "",
    val photoUrl: String = "",
    val timestamp: Long = 0
)