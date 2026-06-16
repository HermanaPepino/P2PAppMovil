package com.example.p2pappmovil.presentation.publishoffer

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pappmovil.data.exchange.ExchangeRetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishOfferScreen(
    onPublishSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("exchange_prefs", Context.MODE_PRIVATE) }
    
    var isBuying by remember { mutableStateOf(true) }
    var currencyGive by remember { mutableStateOf("PEN") }
    var currencyReceive by remember { mutableStateOf("USD") }
    
    // State for rates
    var marketRate by remember { mutableDoubleStateOf(0.0) }
    var customRate by remember { mutableDoubleStateOf(0.0) }
    var isLoading by remember { mutableStateOf(false) }
    var offlineMessage by remember { mutableStateOf<String?>(null) }
    var rateErrorMessage by remember { mutableStateOf<String?>(null) }

    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val currencies = listOf("PEN", "USD", "EUR", "BRL", "CLP")
    val paymentMethods = listOf("Transferencia Bancaria", "Yape", "Plin", "Interbank", "BCP", "BBVA", "Scotiabank")

    val coroutineScope = rememberCoroutineScope()

    // Fetch rate logic
    fun fetchRate(base: String, target: String) {
        if (base == target) {
            marketRate = 1.0
            customRate = 1.0
            offlineMessage = null
            rateErrorMessage = null
            return
        }

        coroutineScope.launch {
            isLoading = true
            rateErrorMessage = null
            try {
                val response = ExchangeRetrofitClient.service.getLatestRates(base)
                val rate = response.rates[target]
                if (rate != null) {
                    marketRate = rate
                    customRate = rate
                    offlineMessage = null
                    
                    // Save to shared prefs
                    sharedPrefs.edit().putString("${base}_${target}", rate.toString()).apply()
                } else {
                    rateErrorMessage = "Moneda no encontrada"
                }
            } catch (e: Exception) {
                // Try to load from shared prefs
                val savedRateStr = sharedPrefs.getString("${base}_${target}", null)
                if (savedRateStr != null) {
                    val savedRate = savedRateStr.toDouble()
                    marketRate = savedRate
                    customRate = savedRate
                    offlineMessage = "Sin conexión. Mostrando última tasa disponible."
                } else {
                    rateErrorMessage = "No se pudo obtener el tipo de cambio."
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Effect to fetch rate when currencies change
    LaunchedEffect(currencyGive, currencyReceive) {
        fetchRate(currencyGive, currencyReceive)
    }

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

            // Market Rate Display
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
            } else {
                Column {
                    Text(
                        text = "Tasa de mercado: ${String.format(Locale.US, "%.4f", marketRate)} $currencyReceive/$currencyGive",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    offlineMessage?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFA500))
                    }
                    rateErrorMessage?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Tasa Ajustable
            Text("Tasa de cambio", fontWeight = FontWeight.SemiBold)
            val minRate = marketRate * 0.90
            val maxRate = marketRate * 1.10

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { if (customRate > minRate) customRate -= 0.001 },
                    enabled = customRate > minRate && marketRate > 0
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Disminuir")
                }

                OutlinedTextField(
                    value = String.format(Locale.US, "%.4f", customRate),
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                )

                IconButton(
                    onClick = { if (customRate < maxRate) customRate += 0.001 },
                    enabled = customRate < maxRate && marketRate > 0
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar")
                }
            }
            Text(
                text = "Rango permitido: ${String.format(Locale.US, "%.4f", minRate)} - ${String.format(Locale.US, "%.4f", maxRate)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
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
                    val min = minAmount.toDoubleOrNull() ?: 0.0
                    val max = maxAmount.toDoubleOrNull() ?: 0.0

                    when {
                        marketRate <= 0 -> {
                            errorMessage = "Espere a que cargue la tasa de cambio"
                            successMessage = null
                        }
                        minAmount.isEmpty() || maxAmount.isEmpty() || paymentMethod.isEmpty() -> {
                            errorMessage = "Todos los campos excepto descripción son obligatorios"
                            successMessage = null
                        }
                        currencyGive == currencyReceive -> {
                            errorMessage = "Moneda origen y destino no pueden ser iguales"
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
