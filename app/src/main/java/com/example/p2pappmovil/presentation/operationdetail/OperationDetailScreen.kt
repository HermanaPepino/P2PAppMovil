package com.example.p2pappmovil.presentation.operationdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationDetailScreen(
    onRateUserClick: () -> Unit = {},
    onReportProblemClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Datos simulados
    val opCode = "TX-20231025-9981"
    val opDateTime = "25 Oct 2023, 14:30"
    val opAmount = "$ 500.00 USD (PEN 1,925.00)"
    val opRate = "3.85"
    val opPaymentMethod = "BCP Transferencia"
    val opStatus = "Completada"
    val opVoucher = "voucher_12345.pdf"

    val statusHistory = listOf(
        StatusUpdate("Iniciada", "25 Oct 2023, 14:15"),
        StatusUpdate("Pago Enviado", "25 Oct 2023, 14:20"),
        StatusUpdate("Validando Voucher", "25 Oct 2023, 14:22"),
        StatusUpdate("Completada", "25 Oct 2023, 14:30")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle de Operación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailItem("Código:", opCode)
                        DetailItem("Fecha y Hora:", opDateTime)
                        DetailItem("Monto:", opAmount)
                        DetailItem("Tasa aplicada:", opRate)
                        DetailItem("Método de pago:", opPaymentMethod)
                        DetailItem("Estado:", opStatus, isStatus = true)
                        DetailItem("Voucher:", opVoucher)
                    }
                }
            }

            item {
                Text("Historial de cambios", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            items(statusHistory) { history ->
                HistoryRow(history)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRateUserClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calificar Usuario")
                }
                OutlinedButton(
                    onClick = onReportProblemClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reportar Problema")
                }
                TextButton(
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
fun DetailItem(label: String, value: String, isStatus: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium, color = Color.Gray)
        Text(
            value,
            fontWeight = FontWeight.Bold,
            color = if (isStatus) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}

data class StatusUpdate(val status: String, val time: String)

@Composable
fun HistoryRow(update: StatusUpdate) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(update.status, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(update.time, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
