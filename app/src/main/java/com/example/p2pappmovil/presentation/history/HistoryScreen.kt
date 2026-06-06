package com.example.p2pappmovil.presentation.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Operation(
    val code: String,
    val date: String,
    val sourceCurrency: String,
    val destCurrency: String,
    val amount: String,
    val counterparty: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOperationClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val operations = listOf(
        Operation("OP-982341", "24/05/2024 15:30", "USD", "PEN", "100.00", "Juan Pérez", "Pendiente de Pago"),
        Operation("OP-982342", "23/05/2024 10:15", "PEN", "USD", "500.00", "María García", "Completada"),
        Operation("OP-982343", "22/05/2024 18:00", "EUR", "PEN", "200.00", "Carlos López", "En Proceso"),
        Operation("OP-982344", "21/05/2024 08:45", "USD", "PEN", "50.00", "Ana Torres", "Completada"),
        Operation("OP-982345", "20/05/2024 12:30", "PEN", "USD", "300.00", "Pedro Sánchez", "Cancelada")
    )

    fun statusColor(status: String): Color = when (status) {
        "Completada" -> Color(0xFF4CAF50)
        "Pendiente de Pago" -> Color(0xFFFFA500)
        "En Proceso" -> Color(0xFF2196F3)
        "Cancelada" -> Color(0xFFF44336)
        else -> Color.Gray
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
        if (operations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no tienes operaciones registradas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(operations) { operation ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOperationClick() },
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
                                    text = operation.code,
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
                                    text = "${operation.amount} ${operation.sourceCurrency}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Contraparte: ${operation.counterparty}",
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
