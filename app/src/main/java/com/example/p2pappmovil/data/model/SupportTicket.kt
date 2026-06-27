package com.example.p2pappmovil.data.model

import com.google.firebase.Timestamp

data class SupportTicket(
    val ticketId: String = "",
    val userId: String = "",
    val userName: String = "",
    val status: String = "OPEN", // OPEN, PENDING, RESOLVED, CLOSED
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val messages: List<SupportMessage> = emptyList()
)

data class SupportMessage(
    val sender: String = "", // USER, ADMIN
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
