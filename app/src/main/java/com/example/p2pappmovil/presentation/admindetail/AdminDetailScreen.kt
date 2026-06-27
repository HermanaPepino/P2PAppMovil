package com.example.p2pappmovil.presentation.admindetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDetailScreen(
    transactionId: String, // Recibe el ID de la transacción a moderar
    onBackClick: () -> Unit = {}
) {
    var offererName by remember { mutableStateOf("") }
    var amountSent by remember { mutableStateOf(0.0) }
    var sourceCurrency by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var voucherUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isActionLoading by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    // Cargar los datos de la transacción en cuestión
    LaunchedEffect(transactionId) {
        db.collection("transactions").document(transactionId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    offererName = doc.getString("offererName") ?: ""
                    amountSent = doc.getDouble("amountSent") ?: 0.0
                    sourceCurrency = doc.getString("sourceCurrency") ?: ""
                    status = doc.getString("status") ?: ""
                    voucherUrl = doc.getString("voucherUrl") ?: "No adjunto"
                }
                isLoading = false
            }
    }

    fun updateStatus(newStatus: String) {
        isActionLoading = true
        db.collection("transactions").document(transactionId)
            .update("status", newStatus)
            .addOnSuccessListener {
                isActionLoading = false
                onBackClick() // Regresa al listado de administración automáticamente
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Moderación") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !isActionLoading) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("ID Transacción:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(transactionId, fontWeight = FontWeight.Bold)
                            Text("Usuario afectado: $offererName")
                            Text("Monto enviado: $amountSent $sourceCurrency")
                            Text("Estado actual: $status", fontWeight = FontWeight.Bold, color = Color(0xFFFFA500))
                        }
                    }

                    Text("Voucher Adjuntado:", fontWeight = FontWeight.SemiBold)

                    // Contenedor simulador del archivo visual del voucher
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.LightGray, shape = MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[ Imagen del Voucher ]\nArchivo: $voucherUrl",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (isActionLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = { updateStatus("Completada") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aprobar Operación (Completar)")
                        }

                        Button(
                            onClick = { updateStatus("Cancelada") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rechazar Voucher (Cancelar)")
                        }
                    }
                }
            }
        }
    }
}