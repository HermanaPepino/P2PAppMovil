package com.example.p2pappmovil.presentation.dispute

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DisputeScreen(
    onDisputeSent: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Datos simulados de la operación
    val operationCode = "OP-123456"
    val counterpartyName = "Carlos Mendoza"
    val currentStatus = "Pendiente de Confirmación"

    var description by remember { mutableStateOf("") }
    var imagesCount by remember { mutableIntStateOf(0) }
    var successMessage by remember { mutableStateOf("") }
    var errorDescription by remember { mutableStateOf<String?>(null) }
    var errorImages by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Crear Disputa",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Operación: $operationCode", fontWeight = FontWeight.Medium)
                Text(text = "Contraparte: $counterpartyName")
                Text(text = "Estado actual: $currentStatus")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { 
                description = it
                errorDescription = null
            },
            label = { Text("Descripción del problema") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            isError = errorDescription != null,
            supportingText = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(errorDescription ?: "")
                    Text("${description.length}/1000")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Evidencias ($imagesCount/5)", fontWeight = FontWeight.Medium)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { if (imagesCount < 5) imagesCount++ else errorImages = "Máximo 5 imágenes" },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cámara", fontSize = 12.sp)
            }
            Button(
                onClick = { if (imagesCount < 5) imagesCount++ else errorImages = "Máximo 5 imágenes" },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Galería", fontSize = 12.sp)
            }
        }
        
        errorImages?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lista simulada de evidencias
        LazyColumn(modifier = Modifier.weight(1f)) {
            items((1..imagesCount).toList()) { index ->
                ListItem(
                    headlineContent = { Text("Evidencia_$index.jpg") },
                    supportingContent = { Text("Formato: JPG") },
                    trailingContent = {
                        TextButton(onClick = { imagesCount-- }) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        }

        if (successMessage.isNotEmpty()) {
            Text(
                text = successMessage,
                color = Color.Green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                var hasError = false
                if (description.length < 20) {
                    errorDescription = "Mínimo 20 caracteres"
                    hasError = true
                }
                if (description.isEmpty()) {
                    errorDescription = "Descripción obligatoria"
                    hasError = true
                }
                
                if (!hasError) {
                    successMessage = "Disputa registrada correctamente"
                    onDisputeSent()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Enviar Disputa")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
