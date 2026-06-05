package com.example.p2pappmovil.presentation.operationresume

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OperationResumeScreen(
    onUploadVoucherClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Datos simulados
    val operationCode = "OP-982341"
    val offererName = "Juan Pérez"
    val sourceCurrency = "USD"
    val destCurrency = "PEN"
    val amountSent = "100.00"
    val amountReceived = "375.50"
    val exchangeRate = "3.755"
    val status = "Pendiente de Pago"
    val date = "24/05/2024 15:30"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Resumen de Operación",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResumeItem(label = "Código de operación", value = operationCode)
                ResumeItem(label = "Oferente", value = offererName)
                ResumeItem(label = "Moneda origen", value = sourceCurrency)
                ResumeItem(label = "Moneda destino", value = destCurrency)
                ResumeItem(label = "Monto enviado", value = "$amountSent $sourceCurrency")
                ResumeItem(label = "Monto a recibir", value = "$amountReceived $destCurrency")
                ResumeItem(label = "Tasa de cambio", value = exchangeRate)
                ResumeItem(label = "Estado", value = status, valueColor = Color(0xFFFFA500)) // Orange-ish
                ResumeItem(label = "Fecha", value = date)
            }
        }

        Button(
            onClick = onUploadVoucherClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Subir Voucher")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun ResumeItem(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
