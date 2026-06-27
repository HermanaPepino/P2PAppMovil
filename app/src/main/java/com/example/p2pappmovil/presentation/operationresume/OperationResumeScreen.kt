package com.example.p2pappmovil.presentation.operationresume

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun OperationResumeScreen(
    transactionId: String, // Recibe el ID de navegación
    onUploadVoucherClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var offererName by remember { mutableStateOf("Cargando...") }
    var sourceCurrency by remember { mutableStateOf("") }
    var destCurrency by remember { mutableStateOf("") }
    var amountSent by remember { mutableStateOf(0.0) }
    var amountReceived by remember { mutableStateOf(0.0) }
    var exchangeRate by remember { mutableStateOf(0.0) }
    var status by remember { mutableStateOf("Cargando...") }
    var date by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(transactionId) {
        FirebaseFirestore.getInstance().collection("transactions").document(transactionId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    offererName = doc.getString("offererName") ?: ""
                    sourceCurrency = doc.getString("sourceCurrency") ?: ""
                    destCurrency = doc.getString("destCurrency") ?: ""
                    amountSent = doc.getDouble("amountSent") ?: 0.0
                    amountReceived = doc.getDouble("amountReceived") ?: 0.0
                    exchangeRate = doc.getDouble("exchangeRate") ?: 0.0
                    status = doc.getString("status") ?: ""
                    date = doc.getString("date") ?: ""
                }
                isLoading = false
            }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Resumen de Operación", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.weight(1f))
        } else {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResumeItem(label = "Código de operación", value = transactionId)
                    ResumeItem(label = "Oferente", value = offererName)
                    ResumeItem(label = "Moneda origen", value = sourceCurrency)
                    ResumeItem(label = "Moneda destino", value = destCurrency)
                    ResumeItem(label = "Monto enviado", value = "$amountSent $sourceCurrency")
                    ResumeItem(label = "Monto a recibir", value = "$amountReceived $destCurrency")
                    ResumeItem(label = "Tasa de cambio", value = exchangeRate.toString())
                    ResumeItem(label = "Estado", value = status, valueColor = Color(0xFFFFA500))
                    ResumeItem(label = "Fecha", value = date)
                }
            }
        }

        Button(onClick = onUploadVoucherClick, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
            Text("Subir Voucher")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
            Text("Volver")
        }
    }
}

@Composable
fun ResumeItem(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = valueColor)
    }
}