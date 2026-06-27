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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartOperationScreen(
    offerId: String, // Recibe el ID real de la oferta seleccionada
    onConfirmOperation: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Variables de estado dinámicas
    var offerorUid by remember { mutableStateOf("") }
    var offerorName by remember { mutableStateOf("") }
    var sourceCurrency by remember { mutableStateOf("") }
    var destCurrency by remember { mutableStateOf("") }
    var exchangeRate by remember { mutableDoubleStateOf(0.0) }
    var minAmount by remember { mutableDoubleStateOf(0.0) }
    var maxAmount by remember { mutableDoubleStateOf(0.0) }
    var paymentMethod by remember { mutableStateOf("") }

    var isDataLoading by remember { mutableStateOf(true) }
    var inputAmount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val amountValue = inputAmount.toDoubleOrNull() ?: 0.0
    val receivedAmount = amountValue * exchangeRate

    // Efecto para jalar los datos verdaderos de la oferta desde la BD
    LaunchedEffect(offerId) {
        FirebaseFirestore.getInstance().collection("offers").document(offerId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    offerorUid = doc.getString("userId") ?: ""
                    offerorName = doc.getString("userName") ?: "Desconocido"
                    sourceCurrency = doc.getString("currencyGive") ?: ""
                    destCurrency = doc.getString("currencyReceive") ?: ""
                    exchangeRate = doc.getDouble("exchangeRate") ?: 0.0
                    minAmount = doc.getDouble("minAmount") ?: 0.0
                    maxAmount = doc.getDouble("maxAmount") ?: 0.0
                    paymentMethod = doc.getString("paymentMethod") ?: ""
                }
                isDataLoading = false
            }
            .addOnFailureListener {
                offerorName = "Error al cargar"
                isDataLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Operación") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !isLoading) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isDataLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
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
                        TextButton(onClick = { onProfileClick(offerorUid) }) { Text("Ver perfil") }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Condiciones de Cambio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // CORRECCIÓN 1: Cambiado fillMaxSize() por fillMaxWidth()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tasa de cambio:")
                            // CORRECCIÓN 2: Se agregó String.format para truncar los decimales infinitos
                            Text("1 $sourceCurrency = ${String.format(java.util.Locale.US, "%.3f", exchangeRate)} $destCurrency", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monto mínimo:")
                            Text("${String.format(java.util.Locale.US, "%.2f", minAmount)} $sourceCurrency", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monto máximo:")
                            Text("${String.format(java.util.Locale.US, "%.2f", maxAmount)} $sourceCurrency", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { inputAmount = it; errorMessage = null },
                    label = { Text("Monto a enviar ($sourceCurrency)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    enabled = !isLoading,
                    supportingText = { if (errorMessage != null) Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error) }
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Resumen", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Entregas: ${String.format(java.util.Locale.US, "%.2f", amountValue)} $sourceCurrency")
                        Text(
                            text = "Recibes: ${String.format(java.util.Locale.US, "%.2f", receivedAmount)} $destCurrency",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = {
                            val amount = inputAmount.toDoubleOrNull()
                            val uid = FirebaseAuth.getInstance().currentUser?.uid

                            when {
                                amount == null -> errorMessage = "Ingrese un monto válido"
                                amount < minAmount || amount > maxAmount -> errorMessage = "El monto debe estar entre $minAmount y $maxAmount"
                                uid == null -> errorMessage = "Sesión inválida"
                                else -> {
                                    isLoading = true
                                    val db = FirebaseFirestore.getInstance()

                                    val nuevaTransaccion = hashMapOf(
                                        "clientUid" to uid,
                                        "offerId" to offerId,
                                        "offererName" to offerorName,
                                        "sourceCurrency" to sourceCurrency,
                                        "destCurrency" to destCurrency,
                                        "amountSent" to amount,
                                        "amountReceived" to receivedAmount,
                                        "exchangeRate" to exchangeRate,
                                        "status" to "Pendiente de Pago",
                                        "date" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(Date())
                                    )

                                    db.collection("transactions").add(nuevaTransaccion)
                                        .addOnSuccessListener { documentReference ->
                                            // CREAR NOTIFICACIÓN PARA EL OFERENTE
                                            val notification = hashMapOf(
                                                "userId" to offerorUid,
                                                "title" to "Nueva Operación Iniciada",
                                                "message" to "El usuario ${FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@")} quiere cambiar ${String.format(java.util.Locale.US, "%.2f", amount)} $sourceCurrency.",
                                                "date" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(Date()),
                                                "isRead" to false,
                                                "type" to "TRANSACTION",
                                                "referenceId" to documentReference.id
                                            )
                                            db.collection("notifications").add(notification)

                                            isLoading = false
                                            onConfirmOperation(documentReference.id)
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = "Error al iniciar operación: ${e.message}"
                                        }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirmar Operación")
                    }
                }

                OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                    Text("Volver")
                }
            }
        }
    }
}