package com.example.p2pappmovil.presentation.admin

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.p2pappmovil.data.model.Dispute
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDisputeScreen(
    onBackClick: () -> Unit = {}
) {
    var disputes by remember { mutableStateOf<List<Dispute>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDispute by remember { mutableStateOf<Dispute?>(null) }
    var filterStatus by remember { mutableStateOf("Abierta") }
    var isResolving by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    fun loadDisputes() {
        val query = if (filterStatus == "Todas") {
            db.collection("disputes").orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
        } else {
            db.collection("disputes")
                .whereEqualTo("status", filterStatus)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
        }

        query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("AdminDisputeScreen", "Error: ${error.message}")
                isLoading = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                disputes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Dispute::class.java)?.copy(id = doc.id)
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(filterStatus) {
        loadDisputes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Disputas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { if (selectedDispute != null) selectedDispute = null else onBackClick() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (selectedDispute == null) {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Abiertas") },
                                onClick = { filterStatus = "Abierta"; showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Resueltas") },
                                onClick = { filterStatus = "Resuelta"; showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = { filterStatus = "Todas"; showMenu = false }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                selectedDispute != null -> DisputeDetailContent(
                    dispute = selectedDispute!!,
                    isResolving = isResolving,
                    onResolve = { newStatus ->
                        isResolving = true
                        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())
                        db.collection("disputes").document(selectedDispute!!.id)
                            .update(
                                "status", "Resuelta ($newStatus)",
                                "resolution", newStatus,
                                "resolvedAt", dateStr
                            )
                            .addOnSuccessListener {
                                db.collection("transactions")
                                    .document(selectedDispute!!.transactionId)
                                    .update("status", newStatus)
                                    .addOnSuccessListener {
                                        val adminList = db.collection("users")
                                            .whereEqualTo("rol", "ADMIN")
                                            .get()
                                        adminList.addOnSuccessListener { admins ->
                                            for (adminDoc in admins.documents) {
                                                val notif = hashMapOf(
                                                    "userId" to adminDoc.id,
                                                    "title" to "Disputa Resuelta",
                                                    "message" to "La disputa de la transacción ${selectedDispute!!.transactionId.take(8)} fue resuelta: $newStatus",
                                                    "date" to dateStr,
                                                    "isRead" to false,
                                                    "type" to "DISPUTE",
                                                    "referenceId" to selectedDispute!!.transactionId,
                                                    "timestamp" to Timestamp.now()
                                                )
                                                db.collection("notifications").add(notif)
                                            }
                                        }
                                        isResolving = false
                                        selectedDispute = null
                                    }
                                    .addOnFailureListener { isResolving = false }
                            }
                            .addOnFailureListener { isResolving = false }
                    },
                    onBack = { selectedDispute = null }
                )
                disputes.isEmpty() -> {
                    Text("No hay disputas $filterStatus.", color = Color.Gray)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(disputes, key = { it.id }) { dispute ->
                            DisputeCard(
                                dispute = dispute,
                                onClick = { selectedDispute = dispute }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DisputeCard(dispute: Dispute, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (dispute.status == "Abierta") Color(0xFFFEEBEE) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (dispute.status == "Abierta") {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Transacción: ${dispute.transactionId.take(8)}", fontWeight = FontWeight.Bold)
                }
                Text(dispute.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text("Motivo: ${dispute.reason.take(80)}...", maxLines = 2, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Estado: ${dispute.status}",
                fontWeight = FontWeight.SemiBold,
                color = if (dispute.status == "Abierta") Color.Red else Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun DisputeDetailContent(
    dispute: Dispute,
    isResolving: Boolean,
    onResolve: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Disputa Activa", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
                HorizontalDivider()
                Text("ID Transacción: ${dispute.transactionId}", style = MaterialTheme.typography.labelSmall)
                Text("ID Disputa: ${dispute.id}", style = MaterialTheme.typography.labelSmall)
                Text("Fecha: ${dispute.date}")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Motivo del Reclamo", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(dispute.reason)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Acción de Resolución", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        if (isResolving) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = { onResolve("Completada") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Resolver a favor - Completar Transacción")
            }

            Button(
                onClick = { onResolve("Cancelada") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Resolver en contra - Cancelar Transacción")
            }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al listado")
        }
    }
}
