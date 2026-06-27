package com.example.p2pappmovil.presentation.admin

// Credenciales de acceso para Admin:
// Correo: admin@p2p.com
// Contraseña: admin123

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
    onUserDetailClick: (String) -> Unit = {}, // Envía el ID seleccionado
    onDisputeDetailClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val pendingOperations = remember { mutableStateListOf<Operation>() }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()

        // Escuchamos en tiempo real todas las operaciones que necesitan revisión
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
                        // Solo filtramos las que están pendientes de revisión por el admin
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

        onDispose {
            listenerRegistration.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión Admin")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (pendingOperations.isEmpty()) {
                Text("No hay operaciones pendientes de revisión.", color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
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
