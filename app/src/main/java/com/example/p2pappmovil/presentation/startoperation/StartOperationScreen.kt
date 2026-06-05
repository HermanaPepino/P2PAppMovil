package com.example.p2pappmovil.presentation.startoperation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartOperationScreen(
    onConfirmOperation: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Mock Data
    val offerorName = "Maria García"
    val sourceCurrency = "USD"
    val destCurrency = "PEN"
    val exchangeRate = 3.75
    val minAmount = 10.0
    val maxAmount = 500.0
    val paymentMethod = "Transferencia BCP"

    var inputAmount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amountValue = inputAmount.toDoubleOrNull() ?: 0.0
    val receivedAmount = amountValue * exchangeRate

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Operación") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Información del Oferente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Nombre: $offerorName")
                    Text(text = "Método de Pago: $paymentMethod")
                    TextButton(onClick = onProfileClick) {
                        Text("Ver perfil")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Condiciones de Cambio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tasa de cambio:")
                        Text("1 $sourceCurrency = $exchangeRate $destCurrency", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Monto mínimo:")
                        Text("$minAmount $sourceCurrency", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Monto máximo:")
                        Text("$maxAmount $sourceCurrency", fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedTextField(
                value = inputAmount,
                onValueChange = {
                    inputAmount = it
                    errorMessage = null
                },
                label = { Text("Monto a enviar ($sourceCurrency)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = errorMessage != null,
                supportingText = {
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Resumen", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Entregas: ${String.format("%.2f", amountValue)} $sourceCurrency")
                    Text(
                        text = "Recibes: ${String.format("%.2f", receivedAmount)} $destCurrency",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amount = inputAmount.toDoubleOrNull()
                    if (amount == null) {
                        errorMessage = "Ingrese un monto válido"
                    } else if (amount < minAmount || amount > maxAmount) {
                        errorMessage = "El monto debe estar entre $minAmount y $maxAmount"
                    } else {
                        onConfirmOperation()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Operación")
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}
