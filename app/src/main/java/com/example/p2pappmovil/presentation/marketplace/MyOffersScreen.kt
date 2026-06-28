package com.example.p2pappmovil.presentation.marketplace

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOffersScreen(
    onBackClick: () -> Unit = {},
    onOfferClick: (String) -> Unit = {}
) {
    val myOffers = remember { mutableStateListOf<Offer>() }
    var isLoading by remember { mutableStateOf(true) }
    val miUid = FirebaseAuth.getInstance().currentUser?.uid

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("offers")
            .whereEqualTo("userId", miUid)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MyOffersScreen", "Error al escuchar mis ofertas: ${error.message}")
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    myOffers.clear()
                    for (document in snapshot.documents) {
                        val offer = document.toObject(Offer::class.java)?.copy(id = document.id)
                        if (offer != null) {
                            myOffers.add(offer)
                        }
                    }
                    isLoading = false
                }
            }

        onDispose {
            listenerRegistration.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Publicaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
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
            } else if (myOffers.isEmpty()) {
                Text(
                    text = "No tienes publicaciones activas.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myOffers, key = { it.id }) { offer ->
                        OfferCard(offer = offer, onClick = { onOfferClick(offer.id) })
                    }
                }
            }
        }
    }
}
