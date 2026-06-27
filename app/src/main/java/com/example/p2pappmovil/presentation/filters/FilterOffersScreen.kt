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
    onApplyFilters: (String, String, Double?, Double?, Boolean) -> Unit = { _, _, _, _, _ -> },
    onClearFilters: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    var currencyGive by remember { mutableStateOf("") }
    var currencyReceive by remember { mutableStateOf("") }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }
    var onlyVerified by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    val currencies = listOf("Cualquiera", "PEN", "USD", "EUR", "BRL", "CLP")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Filtrar ofertas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Text("✕", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Personaliza tu búsqueda",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Moneda que entrego
            Text("Moneda que entrego", fontWeight = FontWeight.SemiBold)
            CurrencySelector(selected = currencyGive.ifEmpty { "Cualquiera" }, options = currencies) { 
                currencyGive = if (it == "Cualquiera") "" else it 
            }

            // Moneda que recibo
            Text("Moneda que recibo", fontWeight = FontWeight.SemiBold)
            CurrencySelector(selected = currencyReceive.ifEmpty { "Cualquiera" }, options = currencies) { 
                currencyReceive = if (it == "Cualquiera") "" else it 
            }

            Text("Rango de Monto", fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minAmount,
                    onValueChange = { minAmount = it },
                    label = { Text("Mínimo") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    placeholder = { Text("Ej: 10.00") }
                )
                OutlinedTextField(
                    value = maxAmount,
                    onValueChange = { maxAmount = it },
                    label = { Text("Máximo") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    placeholder = { Text("Ej: 1000.00") }
                )
            }

            // Switch verificado
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Solo usuarios verificados", fontWeight = FontWeight.Medium)
                        Text("Mostrar solo ofertantes con check de seguridad", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Switch(checked = onlyVerified, onCheckedChange = { onlyVerified = it })
                }
            }

            errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botones de acción
            Button(
                onClick = {
                    val min = minAmount.toDoubleOrNull()
                    val max = maxAmount.toDoubleOrNull()

                    if ((min != null && min < 0) || (max != null && max < 0)) {
                        errorMsg = "Los montos no pueden ser negativos"
                    } else if (min != null && max != null && min > max) {
                        errorMsg = "El monto mínimo no puede ser mayor al máximo"
                    } else {
                        errorMsg = null
                        onApplyFilters(currencyGive, currencyReceive, min, max, onlyVerified)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Aplicar filtros", modifier = Modifier.padding(vertical = 8.dp))
            }

            OutlinedButton(
                onClick = {
                    currencyGive = ""
                    currencyReceive = ""
                    minAmount = ""
                    maxAmount = ""
                    onlyVerified = false
                    errorMsg = null
                    onClearFilters()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Limpiar filtros", modifier = Modifier.padding(vertical = 8.dp))
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
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
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
