package com.example.p2pappmovil.presentation.voucher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(
    transactionId: String, // Recibe el ID de navegación
    onVoucherSent: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isFileValid by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun simulateFileSelection(source: String) {
        selectedFileName = if (source == "camera") "foto_voucher.jpg" else "voucher_galeria.png"
        isFileValid = true
        errorMessage = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subir Voucher") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Código de Operación", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(text = transactionId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { simulateFileSelection("camera") }, modifier = Modifier.weight(1f), enabled = !isLoading) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tomar Foto")
                }
                OutlinedButton(onClick = { simulateFileSelection("gallery") }, modifier = Modifier.weight(1f), enabled = !isLoading) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galería")
                }
            }

            if (selectedFileName != null && isFileValid) {
                Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = selectedFileName ?: "", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (selectedFileName == null) {
                            errorMessage = "Selecciona un archivo antes de continuar."
                        } else {
                            isLoading = true
                            val db = FirebaseFirestore.getInstance()

                            // Actualizamos el estado y guardamos el nombre simulado en la BD
                            db.collection("transactions").document(transactionId)
                                .update(
                                    mapOf(
                                        "status" to "Validando Voucher",
                                        "voucherUrl" to "simulated_storage_path/$selectedFileName"
                                    )
                                )
                                .addOnSuccessListener {
                                    isLoading = false
                                    onVoucherSent() // Redirige al historial
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = "Error al actualizar estado: ${e.message}"
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Voucher")
                }
            }

            OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                Text("Volver")
            }
        }
    }
}