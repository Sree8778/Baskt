package com.example.supportagent.model

import java.util.UUID

data class DriverChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val isFromCustomer: Boolean,
    val text: String,
    val timestamp: String
)
