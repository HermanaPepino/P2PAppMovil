package com.example.p2pappmovil.presentation.support

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
import com.example.p2pappmovil.data.gemini.GeminiRepository
import com.example.p2pappmovil.data.model.SupportMessage
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSupportScreen(
    onBackClick: () -> Unit = {},
    onContactSupportClick: () -> Unit = {}
) {
    val messages = remember { mutableStateListOf<SupportMessage>() }
    var messageText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isTyping by remember { mutableStateOf(false) }
    val geminiRepository = remember { GeminiRepository() }

    // Initial AI greeting
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(
                SupportMessage(
                    sender = "AI",
                    text = "¡Hola! Soy tu asistente IA de CambioSeguro. ¿En qué puedo ayudarte hoy?",
                    timestamp = Timestamp.now()
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistente IA", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = onContactSupportClick) {
                        Text("Contactar Soporte", color = MaterialTheme.colorScheme.primary)
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                if (isTyping) {
                    item {
                        Text(
                            "IA está escribiendo...",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                items(messages.reversed()) { msg ->
                    AiChatBubble(msg)
                }
            }

            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Pregunta algo a la IA...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                val userMsg = messageText
                                val userMessage = SupportMessage(
                                    sender = "USER",
                                    text = userMsg,
                                    timestamp = Timestamp.now()
                                )
                                messages.add(userMessage)
                                messageText = ""
                                
                                // Gemini AI Response
                                scope.launch {
                                    isTyping = true
                                    // Pasamos el historial de la conversación (últimos 10 mensajes para eficiencia)
                                    val aiResponse = geminiRepository.getAiResponse(messages.takeLast(11).toList())
                                    messages.add(
                                        SupportMessage(
                                            sender = "AI",
                                            text = aiResponse,
                                            timestamp = Timestamp.now()
                                        )
                                    )
                                    isTyping = false
                                }
                            }
                        },
                        enabled = messageText.isNotBlank() && !isTyping
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun AiChatBubble(message: SupportMessage) {
    val isUser = message.sender == "USER"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
            shape = shape
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

