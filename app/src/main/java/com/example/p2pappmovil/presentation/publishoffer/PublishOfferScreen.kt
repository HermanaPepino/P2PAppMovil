package com.example.p2pappmovil.presentation.publishoffer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishOfferScreen(
    onPublishSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var isBuying by remember { mutableStateOf(true) }
    var currencyGive by remember { mutableStateOf("PEN") }
    var currencyReceive by remember { mutableStateOf("USD") }
    var exchangeRate by remember { mutableStateOf("") }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val currencies = listOf("PEN", "USD", "EUR", "BRL", "CLP")
    val paymentMethods = listOf("Transferencia Bancaria", "Yape", "Plin", "Interbank", "BCP", "BBVA", "Scotiabank")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Publicar Oferta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tipo de operación
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isBuying,
                    onClick = { isBuying = true },
                    label = { Text("Comprar") }
                )
                FilterChip(
                    selected = !isBuying,
                    onClick = { isBuying = false },
                    label = { Text("Vender") }
                )
            }

            // Monedas
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Doy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    SimpleDropdownSelector(selected = currencyGive, options = currencies) { currencyGive = it }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recibo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    SimpleDropdownSelector(selected = currencyReceive, options = currencies) { currencyReceive = it }
                }
            }

            // Tasa y Montos
            OutlinedTextField(
                value = exchangeRate,
                onValueChange = { exchangeRate = it },
                label = { Text("Tasa de cambio") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minAmount,
                    onValueChange = { minAmount = it },
                    label = { Text("Monto mín") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = maxAmount,
                    onValueChange = { maxAmount = it },
                    label = { Text("Monto máx") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Método de pago
            Text("Método de pago", fontWeight = FontWeight.SemiBold)
            SimpleDropdownSelector(selected = paymentMethod, options = paymentMethods, placeholder = "Seleccionar método") {
                paymentMethod = it
            }

            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            if (successMessage != null) {
                Text(successMessage!!, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val rate = exchangeRate.toDoubleOrNull() ?: 0.0
                    val min = minAmount.toDoubleOrNull() ?: 0.0
                    val max = maxAmount.toDoubleOrNull() ?: 0.0

                    when {
                        exchangeRate.isEmpty() || minAmount.isEmpty() || maxAmount.isEmpty() || paymentMethod.isEmpty() -> {
                            errorMessage = "Todos los campos excepto descripción son obligatorios"
                            successMessage = null
                        }
                        currencyGive == currencyReceive -> {
                            errorMessage = "Moneda origen y destino no pueden ser iguales"
                            successMessage = null
                        }
                        rate <= 0 -> {
                            errorMessage = "Tasa debe ser mayor a 0"
                            successMessage = null
                        }
                        min <= 0 -> {
                            errorMessage = "Monto mínimo debe ser mayor a 0"
                            successMessage = null
                        }
                        max < min -> {
                            errorMessage = "Monto máximo debe ser mayor o igual al mínimo"
                            successMessage = null
                        }
                        else -> {
                            errorMessage = null
                            successMessage = "Oferta publicada correctamente"
                            onPublishSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publicar Oferta")
            }
            
            OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdownSelector(
    selected: String,
    options: List<String>,
    placeholder: String = "",
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.ifEmpty { placeholder },
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
