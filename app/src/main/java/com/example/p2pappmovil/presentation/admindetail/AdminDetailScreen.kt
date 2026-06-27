package com.example.p2pappmovil.presentation.admindetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDetailScreen(
    transactionId: String,
    onBackClick: () -> Unit = {}
) {
    var offererName by remember { mutableStateOf("") }
    var amountSent by remember { mutableStateOf(0.0) }
    var sourceCurrency by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var voucherUrl by remember { mutableStateOf("") }
    
    // Datos de Disputa
    var disputeReason by remember { mutableStateOf<String?>(null) }
    var disputeDate by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isActionLoading by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(transactionId) {
        db.collection("transactions").document(transactionId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    offererName = doc.getString("offererName") ?: ""
                    amountSent = doc.getDouble("amountSent") ?: 0.0
                    sourceCurrency = doc.getString("sourceCurrency") ?: ""
                    status = doc.getString("status") ?: ""
                    voucherUrl = doc.getString("voucherUrl") ?: "No adjunto"
                    
                    if (status == "En Disputa") {
                        // Buscar la disputa asociada
                        db.collection("disputes")
                            .whereEqualTo("transactionId", transactionId)
                            .get()
                            .addOnSuccessListener { disputeSnap ->
                                if (!disputeSnap.isEmpty) {
                                    val dDoc = disputeSnap.documents[0]
                                    disputeReason = dDoc.getString("reason")
                                    disputeDate = dDoc.getString("date") ?: ""
                                }
                            }
                    }
                }
                isLoading = false
            }
    }

    fun updateStatus(newStatus: String) {
        isActionLoading = true
        db.collection("transactions").document(transactionId)
            .update("status", newStatus)
            .addOnSuccessListener {
                // Si había una disputa, cerrarla también
                if (status == "En Disputa") {
                    db.collection("disputes")
                        .whereEqualTo("transactionId", transactionId)
                        .get()
                        .addOnSuccessListener { snap ->
                            for (d in snap.documents) {
                                d.reference.update("status", "Resuelta ($newStatus)")
                            }
                            isActionLoading = false
                            onBackClick()
                        }
                } else {
                    isActionLoading = false
                    onBackClick()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moderación de Operación") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !isActionLoading) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (status == "En Disputa") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (status == "En Disputa") {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Estado: $status", fontWeight = FontWeight.Bold)
                            }
                            Text("ID: $transactionId", style = MaterialTheme.typography.labelSmall)
                            Text("Afectado: $offererName")
                            Text("Monto: $amountSent $sourceCurrency")
                        }
                    }

                    if (disputeReason != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Motivo del Reclamo:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(disputeReason!!)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Fecha de reclamo: $disputeDate", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }

                    Text("Evidencia (Voucher):", fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.LightGray, shape = MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[ Imagen del Voucher ]\n$voucherUrl",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isActionLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = { updateStatus("Completada") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (status == "En Disputa") "Resolver a favor (Completar)" else "Aprobar Operación")
                        }

                        Button(
                            onClick = { updateStatus("Cancelada") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (status == "En Disputa") "Anular Operación (Cancelar)" else "Rechazar Operación")
                        }
                    }
                }
            }
        }
    }
}