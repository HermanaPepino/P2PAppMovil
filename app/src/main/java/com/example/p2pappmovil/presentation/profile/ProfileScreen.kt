package com.example.p2pappmovil.presentation.profile

import android.util.Log
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class UserComment(
    val comment: String = "",
    val rating: Int = 0,
    val date: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onOperateClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var userName by remember { mutableStateOf("Cargando...") }
    var isVerified by remember { mutableStateOf(false) }
    var reputation by remember { mutableDoubleStateOf(0.0) }
    var completedOps by remember { mutableIntStateOf(0) }
    var cancelledOps by remember { mutableIntStateOf(0) }
    var registrationDate by remember { mutableStateOf("...") }
    val comments = remember { mutableStateListOf<UserComment>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            
            // 1. Cargar datos del usuario
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombres = document.getString("nombres") ?: ""
                        val apellidos = document.getString("apellidos") ?: ""
                        userName = "$nombres $apellidos".trim().ifEmpty { "Usuario" }
                        isVerified = document.getBoolean("isVerified") ?: false
                        
                        completedOps = document.getLong("completedOps")?.toInt() ?: 0
                        cancelledOps = document.getLong("cancelledOps")?.toInt() ?: 0
                        
                        val timestamp = document.getTimestamp("fechaRegistro")
                        if (timestamp != null) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            registrationDate = sdf.format(timestamp.toDate())
                        }
                    }
                }

            // 2. Cargar comentarios y calcular reputación real
            db.collection("ratings")
                .whereEqualTo("toUserId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    comments.clear()
                    var totalRating = 0
                    for (doc in snapshot.documents) {
                        val r = doc.toObject(UserComment::class.java)
                        if (r != null) {
                            comments.add(r)
                            totalRating += r.rating
                        }
                    }
                    if (comments.isNotEmpty()) {
                        reputation = totalRating.toDouble() / comments.size
                    } else {
                        reputation = 5.0 // Por defecto si es nuevo
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileScreen", "Error: ${e.message}")
                    isLoading = false
                }
        }
    }

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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingStars(rating = reputation)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${String.format(Locale.US, "%.1f", reputation)} / 5.0", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
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
                        text = "Comentarios (${comments.size})",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (comments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "Este usuario aún no tiene comentarios de otros comerciantes.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(comments) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RatingStars(rating = item.rating.toDouble())
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.comment.ifEmpty { "Sin comentario escrito." },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
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
}

@Composable
fun RatingStars(rating: Double) {
    Row {
        repeat(5) { index ->
            val starIndex = index + 1
            when {
                rating >= starIndex -> {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                }
                rating >= starIndex - 0.5 -> {
                    Icon(Icons.Default.StarHalf, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                }
                else -> {
                    Icon(Icons.Default.StarOutline, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
