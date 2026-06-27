package com.example.p2pappmovil.presentation.marketplace

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

data class Offer(
    val id: String = "",
    val userId: String = "", // Añadimos el UID del creador para saber a quién pertenece
    val userName: String = "",
    val isVerified: Boolean = false,
    val reputation: String = "0.0/5 (0 ops)",
    val isBuying: Boolean = true, // true = Compra, false = Vender
    val originCurrency: String = "USD",
    val targetCurrency: String = "PEN",
    val exchangeRate: Double = 0.0,
    val minAmount: Double = 0.0,
    val maxAmount: Double = 0.0,
    val paymentMethod: String = "",
    val date: String = ""
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onFilterClick: () -> Unit = {},
    onPublishOfferClick: () -> Unit = {},
    onOfferClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Estado reactivo que almacenará las ofertas reales de la base de datos
    val offersList = remember { mutableStateListOf<Offer>() }
    var isLoading by remember { mutableStateOf(true) }

    // Escuchador en tiempo real de Firestore
    DisposableEffect (Unit) {
        val db = FirebaseFirestore.getInstance()
        // Escuchamos la colección "offers" ordenada por fecha descendente (más recientes primero)
        val listenerRegistration = db.collection("offers")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MarketplaceScreen", "Error al escuchar ofertas: ${error.message}")
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Limpiamos la lista local antes de añadir los nuevos cambios
                    offersList.clear()
                    for (document in snapshot.documents) {
                        val offer = document.toObject(Offer::class.java)?.copy(id = document.id)
                        if (offer != null) {
                            offersList.add(offer)
                        }
                    }
                    isLoading = false
                }
            }

        // Se remueve el escuchador automáticamente cuando la pantalla sale de la composición
        onDispose {
            listenerRegistration.remove()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Configuración",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("Historial") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onHistoryClick()
                        }
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogoutClick()
                        }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Marketplace", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onFilterClick) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                        }
                        IconButton(onClick = onNotificationsClick) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onPublishOfferClick) {
                    Icon(Icons.Default.Add, contentDescription = "Publicar Oferta")
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (offersList.isEmpty()) {
                    Text(
                        text = "No hay ofertas disponibles por el momento.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(offersList, key = { it.id }) { offer ->
                            OfferCard(offer = offer, onClick = {onOfferClick(offer.id)})
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferCard(offer: Offer, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = offer.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (offer.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verificado",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = offer.reputation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Cambio", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = if (offer.isBuying) "Compra: ${offer.originCurrency} -> ${offer.targetCurrency}" else "Venta: ${offer.originCurrency} -> ${offer.targetCurrency}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Tasa", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = String.format(java.util.Locale.US, "%.3f", offer.exchangeRate),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Límites", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "${offer.originCurrency} ${String.format(java.util.Locale.US, "%.2f", offer.minAmount)} - ${String.format(java.util.Locale.US, "%.2f", offer.maxAmount)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Pago", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = offer.paymentMethod,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Publicado: ${offer.date}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
