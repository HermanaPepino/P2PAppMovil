package com.example.p2pappmovil.presentation.admindetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDetailScreen(
    onBackClick: () -> Unit = {},
    onBlockUserClick: () -> Unit = {},
    onResolveDisputeClick: () -> Unit = {}
) {
    // Mock Data - User
    val userName = "Carlos Ruiz"
    val userEmail = "carlos.ruiz@example.com"
    val verificationStatus = "Verificado"
    val accountStatus = "Activo"
    val registrationDate = "10/01/2024"
    val reputation = 4.8

    // Mock Data - Dispute
    val disputeCode = "DISP-2024-001"
    val associatedOp = "OP-99821"
    val involvedUsers = "Carlos Ruiz vs. Ana López"
    val description = "El comprador afirma no haber recibido los soles después de enviar los dólares."
    val evidences = listOf("Captura_transferencia.png", "Chat_P2P_1.pdf")
    val history = listOf(
        "12/03/2024 10:00 - Disputa abierta por Ana López",
        "12/03/2024 10:15 - Carlos Ruiz subió evidencia",
        "12/03/2024 11:30 - Administrador asignado"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle Administrativo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Detail Section
            item {
                Text(text = "Detalle de Usuario", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(label = "Nombre", value = userName)
                        DetailRow(label = "Correo", value = userEmail)
                        DetailRow(label = "Verificación", value = verificationStatus)
                        DetailRow(label = "Estado Cuenta", value = accountStatus)
                        DetailRow(label = "Registro", value = registrationDate)
                        DetailRow(label = "Reputación", value = "$reputation / 5.0")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onBlockUserClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Bloquear")
                            }
                            OutlinedButton(
                                onClick = { /* Desbloquear */ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Desbloquear")
                            }
                        }
                    }
                }
            }

            // Dispute Detail Section
            item {
                Text(text = "Detalle de Disputa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(label = "Código", value = disputeCode)
                        DetailRow(label = "Operación", value = associatedOp)
                        DetailRow(label = "Involucrados", value = involvedUsers)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Descripción:", fontWeight = FontWeight.Bold)
                        Text(text = description, style = MaterialTheme.typography.bodyMedium)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Evidencias:", fontWeight = FontWeight.Bold)
                        evidences.forEach { evidence ->
                            Text(text = "• $evidence", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Historial:", fontWeight = FontWeight.Bold)
                        history.forEach { event ->
                            Text(text = event, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onResolveDisputeClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Aprobar reclamo")
                            }
                            Button(
                                onClick = { /* Rechazar */ },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Rechazar reclamo")
                            }
                            OutlinedButton(
                                onClick = { /* Solicitar info */ },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Solicitar información adicional")
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
