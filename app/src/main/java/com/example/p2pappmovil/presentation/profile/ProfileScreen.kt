package com.example.p2pappmovil.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOperateClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Mock Data
    val userName = "Juan Pérez"
    val isVerified = true
    val reputation = 4.5
    val completedOps = 120
    val cancelledOps = 3
    val registrationDate = "15/05/2023"
    val comments = listOf(
        "Excelente vendedor, muy rápido.",
        "Todo conforme con la transacción.",
        "Recomendado, muy atento."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Profile Picture
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                
                if (isVerified) {
                    Text(
                        text = "Usuario Verificado",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reputation
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(rating = reputation)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "$reputation / 5.0", style = MaterialTheme.typography.bodyLarge)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Estadísticas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Operaciones completadas:")
                            Text("$completedOps", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Operaciones canceladas:")
                            Text("$cancelledOps", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Miembro desde:")
                            Text(registrationDate, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Comentarios",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold
                )
            }

            if (comments.isEmpty()) {
                item {
                    Text("Este usuario aún no tiene comentarios.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(comments) { comment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = comment,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onOperateClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Operaciones")
                }
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
fun RatingStars(rating: Double) {
    Row {
        repeat(5) { index ->
            val starIndex = index + 1
            when {
                rating >= starIndex -> {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                }
                rating >= starIndex - 0.5 -> {
                    Icon(Icons.Default.StarHalf, contentDescription = null, tint = Color(0xFFFFC107))
                }
                else -> {
                    Icon(Icons.Default.StarOutline, contentDescription = null, tint = Color(0xFFFFC107))
                }
            }
        }
    }
}
