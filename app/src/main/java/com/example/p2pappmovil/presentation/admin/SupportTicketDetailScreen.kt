package com.example.p2pappmovil.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.p2pappmovil.data.model.SupportMessage
import com.example.p2pappmovil.data.model.SupportTicket
import com.example.p2pappmovil.presentation.support.ChatBubble
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportTicketDetailScreen(
    ticketId: String,
    onBackClick: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    var ticket by remember { mutableStateOf<SupportTicket?>(null) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(ticketId) {
        db.collection("supportTickets").document(ticketId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    ticket = snapshot.toObject(SupportTicket::class.java)
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(ticket?.userName ?: "Cargando...", style = MaterialTheme.typography.titleMedium)
                        Text("Ticket: $ticketId", style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (ticket?.status != "CLOSED") {
                        TextButton(onClick = { 
                            db.collection("supportTickets").document(ticketId).update("status", "CLOSED")
                        }) {
                            Text("Cerrar Ticket", color = Color.Red)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val messages = ticket?.messages ?: emptyList()
                
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { msg ->
                        ChatBubble(msg)
                    }
                }

                if (ticket?.status != "CLOSED") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Respuesta del administrador...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    sendAdminResponse(db, ticket!!, messageText)
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

private fun sendAdminResponse(
    db: FirebaseFirestore,
    ticket: SupportTicket,
    text: String
) {
    val newMessage = SupportMessage(
        sender = "ADMIN",
        text = text,
        timestamp = Timestamp.now()
    )

    val updatedMessages = ticket.messages + newMessage
    
    db.collection("supportTickets").document(ticket.ticketId)
        .update(
            "messages", updatedMessages,
            "updatedAt", Timestamp.now(),
            "status", "RESPONDED"
        )

    // Create notification for user
    val notificationId = db.collection("notifications").document().id
    val notification = mapOf(
        "userId" to ticket.userId,
        "title" to "Soporte Técnico",
        "message" to "El administrador ha respondido a tu consulta.",
        "date" to SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date()),
        "isRead" to false,
        "type" to "SUPPORT_REPLY",
        "ticketId" to ticket.ticketId,
        "timestamp" to Timestamp.now()
    )
    db.collection("notifications").document(notificationId).set(notification)
}
