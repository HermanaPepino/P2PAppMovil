package com.example.p2pappmovil.presentation.admin

// Credenciales de acceso para Admin:
// Correo: admin@cambioseguro.com
// Contraseña: 12345678

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pappmovil.presentation.history.Operation
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onUserDetailClick: (String) -> Unit = {},
    onDisputeDetailClick: (String) -> Unit = {},
    onDisputeManagementClick: () -> Unit = {},
    onSupportRequestsClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val pendingOperations = remember { mutableStateListOf<Operation>() }
    var isLoading by remember { mutableStateOf(true) }
    var openDisputeCount by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()

        val listenerRegistration = db.collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdminScreen", "Error al escuchar transacciones: ${error.message}")
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    pendingOperations.clear()
                    for (document in snapshot.documents) {
                        val status = document.getString("status") ?: ""
                        if (status == "Validando Voucher" || status == "En Disputa") {
                            val op = document.toObject(Operation::class.java)?.copy(id = document.id)
                            if (op != null) {
                                pendingOperations.add(op)
                            }
                        }
                    }
                }
                isLoading = false
            }

        val disputeListener = db.collection("disputes")
            .whereEqualTo("status", "Abierta")
            .addSnapshotListener { snapshot, _ ->
                openDisputeCount = snapshot?.size() ?: 0
            }

        onDispose {
            listenerRegistration.remove()
            disputeListener.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onSupportRequestsClick) {
                        Text("Solicitudes Soporte", fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión Admin")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Gestión de Disputas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDisputeManagementClick),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEEBEE))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Disputas Activas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            if (openDisputeCount > 0) "$openDisputeCount disputa(s) pendiente(s)" else "Sin disputas abiertas",
                            color = if (openDisputeCount > 0) Color.Red else Color.Gray
                        )
                    }
                    Text("Ver >", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider()

            Text(
                text = "Operaciones Pendientes de Revisión",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (pendingOperations.isEmpty()) {
                Text("No hay operaciones pendientes de revisión.", color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingOperations, key = { it.id }) { op ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onUserDetailClick(op.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (op.status == "En Disputa") Color(0xFFFEEBEE) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Revisar ID: ${op.id.take(6)}...", fontWeight = FontWeight.Bold)
                                    Text(op.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Text("Monto: ${op.amountSent} ${op.sourceCurrency} → ${op.amountReceived} ${op.destCurrency}")
                                Text(
                                    text = "Estado: ${op.status}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (op.status == "En Disputa") Color.Red else Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
