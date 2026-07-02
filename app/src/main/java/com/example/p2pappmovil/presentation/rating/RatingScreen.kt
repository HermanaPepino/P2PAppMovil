package com.example.p2pappmovil.presentation.rating

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    transactionId: String,
    targetUserId: String,
    onRatingSent: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var counterpartyName by remember { mutableStateOf("Cargando...") }
    var operationCode by remember { mutableStateOf("") }
    var operationDate by remember { mutableStateOf("") }
    val maxCommentLength = 500

    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isCompletedOperation by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(transactionId) {
        db.collection("transactions").document(transactionId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val status = doc.getString("status") ?: ""
                    isCompletedOperation = status == "Completada"
                    counterpartyName = doc.getString("offererName") ?: "Usuario"
                    operationCode = "TX-${doc.id.take(8).uppercase()}"
                    operationDate = doc.getString("date") ?: ""
                } else {
                    isCompletedOperation = false
                }
                isLoading = false
            }
            .addOnFailureListener {
                isCompletedOperation = false
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calificar Usuario") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (!isCompletedOperation) {
                Text(
                    text = "Solo puedes calificar operaciones completadas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                )
                OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volver")
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = counterpartyName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = operationCode,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = operationDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = "Puntuación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = {
                                rating = index + 1
                                errorMessage = null
                            },
                            enabled = !isSending
                        ) {
                            Icon(
                                imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = "Estrella ${index + 1}",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                if (rating > 0) {
                    val label = when (rating) {
                        1 -> "Muy malo"
                        2 -> "Malo"
                        3 -> "Regular"
                        4 -> "Bueno"
                        5 -> "Excelente"
                        else -> ""
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = {
                        if (it.length <= maxCommentLength) {
                            comment = it
                        }
                    },
                    label = { Text("Comentario (opcional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4,
                    maxLines = 6,
                    enabled = !isSending,
                    supportingText = {
                        Text(
                            text = "${comment.length}/$maxCommentLength",
                            color = if (comment.length >= maxCommentLength) MaterialTheme.colorScheme.error else Color.Gray
                        )
                    }
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (successMessage != null) {
                    Text(
                        text = successMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (isSending) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (rating == 0) {
                                errorMessage = "Debes seleccionar una puntuación"
                                successMessage = null
                            } else {
                                isSending = true
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                val ratingData = hashMapOf(
                                    "transactionId" to transactionId,
                                    "fromUserId" to (currentUser?.uid ?: ""),
                                    "toUserId" to targetUserId,
                                    "rating" to rating,
                                    "comment" to comment,
                                    "date" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(Date())
                                )

                                db.collection("ratings").add(ratingData)
                                    .addOnSuccessListener {
                                        successMessage = "Calificación registrada correctamente"
                                        onRatingSent()
                                    }
                                    .addOnFailureListener {
                                        errorMessage = "Error al enviar calificación"
                                        isSending = false
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar Calificación")
                    }
                }

                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volver")
                }
            }
        }
    }
}
