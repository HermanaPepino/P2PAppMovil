package com.example.p2pappmovil.presentation.dispute

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

@Composable
fun DisputeScreen(
    transactionId: String,
    onDisputeSent: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var operationCode by remember { mutableStateOf("") }
    var counterpartyName by remember { mutableStateOf("") }
    var currentStatus by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }
    var imagesCount by remember { mutableIntStateOf(0) }
    var successMessage by remember { mutableStateOf("") }
    var errorDescription by remember { mutableStateOf<String?>(null) }
    var errorImages by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(transactionId) {
        db.collection("transactions").document(transactionId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    operationCode = doc.id.take(8).uppercase()
                    counterpartyName = doc.getString("offererName") ?: "Desconocido"
                    currentStatus = doc.getString("status") ?: ""
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Crear Disputa",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Operación: $operationCode", fontWeight = FontWeight.Medium)
                    Text(text = "Contraparte: $counterpartyName")
                    Text(text = "Estado actual: $currentStatus")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { 
                    description = it
                    errorDescription = null
                },
                label = { Text("Descripción del problema") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                isError = errorDescription != null,
                enabled = !isSending,
                supportingText = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(errorDescription ?: "")
                        Text("${description.length}/1000")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Evidencias ($imagesCount/5)", fontWeight = FontWeight.Medium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { if (imagesCount < 5) imagesCount++ else errorImages = "Máximo 5 imágenes" },
                    modifier = Modifier.weight(1f),
                    enabled = !isSending
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cámara", fontSize = 12.sp)
                }
                Button(
                    onClick = { if (imagesCount < 5) imagesCount++ else errorImages = "Máximo 5 imágenes" },
                    modifier = Modifier.weight(1f),
                    enabled = !isSending
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Galería", fontSize = 12.sp)
                }
            }
            
            errorImages?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items((1..imagesCount).toList()) { index ->
                    ListItem(
                        headlineContent = { Text("Evidencia_$index.jpg") },
                        supportingContent = { Text("Formato: JPG") },
                        trailingContent = {
                            TextButton(onClick = { if (!isSending) imagesCount-- }) {
                                Text("Eliminar", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            }

            if (successMessage.isNotEmpty()) {
                Text(
                    text = successMessage,
                    color = Color.Green,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        var hasError = false
                        if (description.length < 20) {
                            errorDescription = "Mínimo 20 caracteres"
                            hasError = true
                        }
                        if (description.isEmpty()) {
                            errorDescription = "Descripción obligatoria"
                            hasError = true
                        }
                        
                        if (!hasError) {
                            isSending = true
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            
                            val disputeData = hashMapOf(
                                "transactionId" to transactionId,
                                "userId" to (currentUser?.uid ?: ""),
                                "reason" to description,
                                "date" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(Date()),
                                "status" to "Abierta"
                            )

                            db.collection("disputes").add(disputeData)
                                .addOnSuccessListener {
                                    // Actualizar estado de la transacción
                                    db.collection("transactions").document(transactionId)
                                        .get()
                                        .addOnSuccessListener { txDoc ->
                                            val clientUid = txDoc.getString("clientUid") ?: ""
                                            val offerId = txDoc.getString("offerId") ?: ""
                                            
                                            db.collection("transactions").document(transactionId)
                                                .update("status", "En Disputa")
                                            
                                            // Buscar el dueño de la oferta para notificarlo
                                            db.collection("offers").document(offerId).get()
                                                .addOnSuccessListener { offerDoc ->
                                                    val offererUid = offerDoc.getString("userId") ?: ""
                                                    val targetUid = if (currentUser?.uid == clientUid) offererUid else clientUid
                                                    
                                                    if (targetUid.isNotEmpty()) {
                                                        val notif = hashMapOf(
                                                            "userId" to targetUid,
                                                            "title" to "Disputa Iniciada",
                                                            "message" to "Se ha abierto una disputa en la operación $operationCode.",
                                                            "date" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(Date()),
                                                            "isRead" to false,
                                                            "type" to "DISPUTE_CREATED",
                                                            "referenceId" to transactionId,
                                                            "timestamp" to com.google.firebase.Timestamp.now()
                                                        )
                                                        db.collection("notifications").add(notif)
                                                    }
                                                }

                                            successMessage = "Disputa registrada correctamente"
                                            isSending = false
                                            onDisputeSent()
                                        }
                                }
                                .addOnFailureListener {
                                    isSending = false
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Enviar Disputa")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSending
            ) {
                Text("Volver")
            }
        }
    }
}
