package com.example.p2pappmovil.presentation.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminScreen(
    onUserDetailClick: () -> Unit = {},
    onDisputeDetailClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Panel Administrativo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Métricas
        Text(text = "Métricas Generales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modifier = Modifier.weight(1f)
            MetricCard("Total Usuarios", "1,250", modifier)
            MetricCard("Verificados", "980", modifier)
            MetricCard("Bloqueados", "12", modifier)
            MetricCard("Op. Activas", "45", modifier)
            MetricCard("Op. Completadas", "3,420", modifier)
            MetricCard("Disputas Pend.", "5", modifier)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Módulo de Usuarios
        Text(text = "Gestión de Usuarios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val users = listOf(
                UserSim("Maria Garcia", "m.garcia@email.com", "Verificado", "Activo", "12/01/2024"),
                UserSim("Jose Lora", "j.lora@email.com", "Pendiente", "Activo", "15/05/2024")
            )
            users.forEach { user ->
                UserItem(user, onUserDetailClick)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Módulo de Disputas
        Text(text = "Disputas Recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val disputes = listOf(
                DisputeSim("DISP-101", "OP-9823", "Juan vs Pedro", "20/05/2024", "Abierta"),
                DisputeSim("DISP-102", "OP-1142", "Ana vs Luis", "18/05/2024", "En Revisión")
            )
            disputes.forEach { dispute ->
                DisputeItem(dispute, onDisputeDetailClick)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

data class UserSim(val name: String, val email: String, val verification: String, val status: String, val date: String)

@Composable
fun UserItem(user: UserSim, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = user.name, fontWeight = FontWeight.Bold)
            Text(text = user.email, style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "V: ${user.verification}", fontSize = 11.sp)
                Text(text = "S: ${user.status}", fontSize = 11.sp)
                Text(text = "Reg: ${user.date}", fontSize = 11.sp)
            }
        }
    }
}

data class DisputeSim(val id: String, val op: String, val users: String, val date: String, val status: String)

@Composable
fun DisputeItem(dispute: DisputeSim, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = dispute.id, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text(text = dispute.status, style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "Operación: ${dispute.op}", style = MaterialTheme.typography.bodySmall)
            Text(text = dispute.users, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Fecha: ${dispute.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
