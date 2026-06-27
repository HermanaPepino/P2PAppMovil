package com.example.p2pappmovil.presentation.history

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Operation(
    val id: String = "",
    val date: String = "",
    val sourceCurrency: String = "",
    val destCurrency: String = "",
    val amountSent: Double = 0.0,
    val amountReceived: Double = 0.0,
    val offererName: String = "",
    val status: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOperationClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val operations = remember { mutableStateListOf<Operation>() }
    var isLoading by remember { mutableStateOf(true) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    fun statusColor(status: String): Color = when (status) {
        "Completada" -> Color(0xFF4CAF50)
        "Pendiente de Pago" -> Color(0xFFFFA500)
        "Validando Voucher" -> Color(0xFF2196F3)
        "En Disputa" -> Color(0xFFF44336)
        "Cancelada" -> Color(0xFF757575)
        else -> Color.Gray
    }

    // Escuchador en tiempo real filtrado por el UID del usuario actual
    DisposableEffect(currentUid) {
        if (currentUid == null) {
            isLoading = false
            return@DisposableEffect onDispose {}
        }

        val db = FirebaseFirestore.getInstance()

        // Consultamos las transacciones donde el usuario actual sea el cliente comprador
        // Nota: Si requieren filtrar también por 'ownerUid' simultáneamente, Firestore requiere índices compuestos.
        // Por simplicidad inmediata de entrega académica, filtramos por transacciones iniciadas por el cliente.
        val listenerRegistration = db.collection("transactions")
            .whereEqualTo("clientUid", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HistoryScreen", "Error al traer historial: ${error.message}")
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    operations.clear()
                    for (document in snapshot.documents) {
                        val op = document.toObject(Operation::class.java)?.copy(id = document.id)
                        if (op != null) {
                            operations.add(op)
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
                title = { Text("Historial de Operaciones") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (operations.isEmpty()) {
                Text(
                    text = "Aún no tienes operaciones registradas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(operations, key = { it.id }) { operation ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOperationClick(operation.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ID: ${operation.id.take(8)}...",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = operation.date,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                HorizontalDivider()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${operation.sourceCurrency} → ${operation.destCurrency}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.2f", operation.amountSent)} ${operation.sourceCurrency}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Oferente: ${operation.offererName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = operation.status,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor(operation.status)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}