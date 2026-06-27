package com.example.p2pappmovil.presentation.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.p2pappmovil.data.model.SupportTicket
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportTicketsScreen(
    onTicketClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    var tickets by remember { mutableStateOf<List<SupportTicket>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        db.collection("supportTickets")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    tickets = snapshot.toObjects(SupportTicket::class.java)
                }
                isLoading = false
            }
    }

    val filteredTickets = remember(tickets, selectedTab) {
        if (selectedTab == 0) {
            tickets.filter { it.status != "CLOSED" }
        } else {
            tickets.filter { it.status == "CLOSED" }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes de Soporte", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Activos") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Historial") }
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (filteredTickets.isEmpty()) {
                    Text(
                        if (selectedTab == 0) "No hay tickets activos." else "No hay historial de tickets.",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTickets) { ticket ->
                            SupportTicketItem(ticket = ticket, onClick = { onTicketClick(ticket.ticketId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupportTicketItem(ticket: SupportTicket, onClick: () -> Unit) {
    val lastMessage = ticket.messages.lastOrNull()
    val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when (ticket.status) {
                "OPEN" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                "PENDING" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = ticket.userName, fontWeight = FontWeight.Bold)
                Text(text = dateFormat.format(ticket.updatedAt.toDate()), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Estado: ${ticket.status}",
                color = when (ticket.status) {
                    "OPEN" -> Color.Red
                    "PENDING" -> Color(0xFFFF5722)
                    "RESPONDED" -> Color(0xFF3F51B5)
                    "CLOSED" -> Color(0xFF4CAF50)
                    else -> Color.Gray
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            lastMessage?.let {
                Text(
                    text = "Último: ${it.text}",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${ticket.messages.size} mensajes",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
