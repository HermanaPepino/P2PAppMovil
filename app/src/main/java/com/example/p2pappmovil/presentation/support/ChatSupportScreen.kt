package com.example.p2pappmovil.presentation.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pappmovil.data.model.SupportMessage
import com.example.p2pappmovil.data.model.SupportTicket
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSupportScreen(
    ticketId: String? = null, // Parámetro opcional para abrir un ticket específico
    onBackClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onAiSupportClick: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    
    var activeTicket by remember { mutableStateOf<SupportTicket?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showFaqs by remember { mutableStateOf(ticketId == null) } // Si hay ticketId, no mostramos FAQs
    var currentAnswer by remember { mutableStateOf<String?>(null) }
    var messageText by remember { mutableStateOf("") }
    var isStartingTicket by remember { mutableStateOf(false) }

    val faqs = listOf(
        "¿Cómo publicar una oferta?" to "Ve a la pantalla principal y pulsa el botón '+'. Completa los datos de moneda y tasa de cambio.",
        "¿Cómo iniciar una operación?" to "En el marketplace, selecciona una oferta y pulsa en ella para ver los detalles e iniciar el intercambio.",
        "¿Cómo subir un comprobante?" to "Dentro del resumen de la operación, encontrarás el botón 'Subir Comprobante' para adjuntar tu foto.",
        "¿Cómo reportar un problema?" to "En el detalle de la operación, selecciona 'Reportar un problema' para abrir una disputa.",
        "¿Cómo calificar un usuario?" to "Al finalizar una operación, aparecerá automáticamente la opción de calificar con estrellas.",
        "¿Cómo cancelar una operación?" to "Las operaciones pueden cancelarse antes de que se suba el comprobante de pago."
    )

    // Listen for tickets
    LaunchedEffect(currentUser, ticketId) {
        if (currentUser != null) {
            if (ticketId != null) {
                // Si venimos de una notificación con un ticket específico
                db.collection("supportTickets").document(ticketId)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            activeTicket = snapshot.toObject(SupportTicket::class.java)
                        }
                        isLoading = false
                    }
            } else {
                // Lógica normal de búsqueda de ticket activo
                db.collection("supportTickets")
                    .whereEqualTo("userId", currentUser.uid)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && !snapshot.isEmpty) {
                            val tickets = snapshot.toObjects(SupportTicket::class.java)
                            activeTicket = tickets.find { it.status != "CLOSED" }
                            // ELIMINADO: showFaqs = activeTicket == null
                            // Ahora showFaqs se mantiene en true hasta que el usuario decida entrar al chat
                        }
                        isLoading = false
                    }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Centro de Ayuda", style = MaterialTheme.typography.titleMedium)
                        activeTicket?.let { 
                            Text("Estado: ${it.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
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
            } else if (showFaqs && !isStartingTicket) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("Preguntas Frecuentes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Button(
                            onClick = onAiSupportClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Preguntar a la IA")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(faqs) { (q, a) ->
                        OutlinedButton(
                            onClick = { currentAnswer = a },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(q)
                        }
                    }

                    currentAnswer?.let {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Text(it, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        if (activeTicket != null && activeTicket?.status != "CLOSED") {
                            Button(
                                onClick = { showFaqs = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Ir a mi ticket activo")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Button(
                            onClick = { isStartingTicket = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Realizar una nueva consulta")
                        }
                    }
                }
            } else {
                // Chat Interface
                val messages = activeTicket?.messages ?: emptyList()
                
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { msg ->
                        ChatBubble(msg)
                    }
                    
                    if (activeTicket?.status == "CLOSED") {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Tu solicitud ha sido atendida y el ticket fue cerrado.",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { 
                                        activeTicket = null
                                        showFaqs = true
                                        isStartingTicket = false
                                    }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Crear nuevo ticket")
                                    }
                                    OutlinedButton(onClick = onHistoryClick, modifier = Modifier.fillMaxWidth()) {
                                        Text("Ver historial")
                                    }
                                }
                            }
                        }
                    }
                }

                if (activeTicket?.status != "CLOSED") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Escribe tu consulta...") },
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
                                    sendMessage(db, currentUser, activeTicket, messageText)
                                    messageText = ""
                                    isStartingTicket = false // Transition to ticket view if it was starting
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

@Composable
fun ChatBubble(message: SupportMessage) {
    val isUser = message.sender == "USER"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = color,
            shape = shape,
            tonalElevation = 2.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp.toDate()),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun sendMessage(
    db: FirebaseFirestore,
    user: com.google.firebase.auth.FirebaseUser?,
    ticket: SupportTicket?,
    text: String
) {
    if (user == null) return

    val newMessage = SupportMessage(
        sender = "USER",
        text = text,
        timestamp = Timestamp.now()
    )

    if (ticket == null || ticket.status == "CLOSED") {
        // Create new ticket
        val newId = db.collection("supportTickets").document().id
        val newTicket = SupportTicket(
            ticketId = newId,
            userId = user.uid,
            userName = user.displayName ?: user.email ?: "Usuario",
            status = "OPEN",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            messages = listOf(newMessage)
        )
        db.collection("supportTickets").document(newId).set(newTicket)
    } else {
        // Update existing ticket
        val updatedMessages = ticket.messages + newMessage
        db.collection("supportTickets").document(ticket.ticketId)
            .update(
                "messages", updatedMessages,
                "updatedAt", Timestamp.now(),
                "status", "PENDING" // Mark as pending admin response
            )
    }
}
