package com.example.p2pappmovil.data.model

data class Dispute(
    val id: String = "",
    val transactionId: String = "",
    val userId: String = "",
    val userName: String = "",
    val reason: String = "",
    val date: String = "",
    val status: String = "Abierta",
    val resolution: String = "",
    val resolvedAt: String = ""
)
