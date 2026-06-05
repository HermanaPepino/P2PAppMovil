package com.example.p2pappmovil.presentation.filters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterOffersScreen(
    onApplyFilters: () -> Unit = {},
    onClearFilters: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    var currencyGive by remember { mutableStateOf("PEN") }
    var currencyReceive by remember { mutableStateOf("USD") }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }
    var onlyVerified by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    val currencies = listOf("PEN", "USD", "EUR", "BRL", "CLP")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Filtrar ofertas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Text("X", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
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
            // Moneda que entrego
            Text("Moneda que entrego", fontWeight = FontWeight.SemiBold)
            CurrencySelector(selected = currencyGive, options = currencies) { currencyGive = it }

            // Moneda que recibo
            Text("Moneda que recibo", fontWeight = FontWeight.SemiBold)
            CurrencySelector(selected = currencyReceive, options = currencies) { currencyReceive = it }

            // Montos
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minAmount,
                    onValueChange = { minAmount = it },
                    label = { Text("Monto mínimo") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = maxAmount,
                    onValueChange = { maxAmount = it },
                    label = { Text("Monto máximo") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Switch verificado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Solo usuarios verificados")
                Switch(checked = onlyVerified, onCheckedChange = { onlyVerified = it })
            }

            errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botones de acción
            Button(
                onClick = {
                    val min = minAmount.toDoubleOrNull() ?: 0.0
                    val max = maxAmount.toDoubleOrNull() ?: 0.0

                    if (min < 0 || max < 0) {
                        errorMsg = "Los montos no pueden ser negativos"
                    } else if (maxAmount.isNotEmpty() && min > max) {
                        errorMsg = "El monto mínimo no puede ser mayor al máximo"
                    } else {
                        errorMsg = null
                        onApplyFilters()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar filtros")
            }

            OutlinedButton(
                onClick = {
                    currencyGive = "PEN"
                    currencyReceive = "USD"
                    minAmount = ""
                    maxAmount = ""
                    onlyVerified = false
                    errorMsg = null
                    onClearFilters()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpiar filtros")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
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
