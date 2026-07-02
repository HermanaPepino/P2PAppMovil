package com.example.p2pappmovil.presentation.operationdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationDetailScreen(
    transactionId: String,
    onRateUserClick: (String) -> Unit = {},
    onReportProblemClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var opCode by remember { mutableStateOf("") }
    var opDateTime by remember { mutableStateOf("") }
    var opAmount by remember { mutableStateOf("") }
    var opRate by remember { mutableStateOf("") }
    var opPaymentMethod by remember { mutableStateOf("") }
    var opStatus by remember { mutableStateOf("") }
    var opVoucher by remember { mutableStateOf("No disponible") }
    var counterpartyUserId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(transactionId) {
        if (transactionId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("transactions").document(transactionId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        opCode = doc.id.take(12).uppercase()
                        opDateTime = doc.getString("date") ?: ""
                        val amountSent = doc.getDouble("amountSent") ?: 0.0
                        val amountRec = doc.getDouble("amountReceived") ?: 0.0
                        val sCurr = doc.getString("sourceCurrency") ?: ""
                        val dCurr = doc.getString("destCurrency") ?: ""
                        opAmount = "${String.format(Locale.US, "%.2f", amountSent)} $sCurr -> ${String.format(Locale.US, "%.2f", amountRec)} $dCurr"
                        opRate = String.format(Locale.US, "%.3f", doc.getDouble("exchangeRate") ?: 0.0)
                        opPaymentMethod = doc.getString("paymentMethod") ?: "No especificado"
                        opStatus = doc.getString("status") ?: ""
                        opVoucher = doc.getString("voucherUrl") ?: "Sin adjuntar"

                        val offerId = doc.getString("offerId")
                        if (offerId != null) {
                            db.collection("offers").document(offerId).get()
                                .addOnSuccessListener { offerDoc ->
                                    counterpartyUserId = offerDoc.getString("userId") ?: ""
                                }
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    // Historial simulado por ahora o podrías tener una subcolección 'history'
    val statusHistory = listOf(
        StatusUpdate("Operación Iniciada", opDateTime),
        StatusUpdate(opStatus, "Actualizado recientemente")
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
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
                    if (opStatus == "Completada") {
                        Button(
                            onClick = { onRateUserClick(counterpartyUserId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Calificar Usuario")
                        }
                    }
                    OutlinedButton(
                        onClick = { onReportProblemClick(transactionId) },
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
            color = if (isStatus) MaterialTheme.colorScheme.primary else Color.Unspecified,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1
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
