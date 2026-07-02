package com.example.p2pappmovil.presentation.voucher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(
    transactionId: String,
    onVoucherSent: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val storageRef = remember { FirebaseStorage.getInstance().reference }
    val db = remember { FirebaseFirestore.getInstance() }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            selectedImageUri = cameraImageUri
            errorMessage = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            errorMessage = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempImageUri(context)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            errorMessage = "Permiso de cámara denegado"
        }
    }

    fun openCamera() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> {
                val uri = createTempImageUri(context)
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    fun openGallery() {
        galleryLauncher.launch("image/*")
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
                OutlinedButton(onClick = { openCamera() }, modifier = Modifier.weight(1f), enabled = !isLoading) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tomar Foto")
                }
                OutlinedButton(onClick = { openGallery() }, modifier = Modifier.weight(1f), enabled = !isLoading) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galería")
                }
            }

            if (selectedImageUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Imagen seleccionada", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
                        val uri = selectedImageUri
                        if (uri == null) {
                            errorMessage = "Selecciona un archivo antes de continuar."
                        } else {
                            isLoading = true
                            uploadVoucher(context, storageRef, db, transactionId, uri) { success, error ->
                                isLoading = false
                                if (success) {
                                    onVoucherSent()
                                } else {
                                    errorMessage = error ?: "Error al subir el voucher"
                                }
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

private fun createTempImageUri(context: Context): Uri {
    val vouchersDir = File(context.cacheDir, "vouchers")
    vouchersDir.mkdirs()
    val photoFile = File(vouchersDir, "voucher_${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
}

private fun uploadVoucher(
    context: Context,
    storageRef: com.google.firebase.storage.StorageReference,
    db: FirebaseFirestore,
    transactionId: String,
    imageUri: Uri,
    onResult: (Boolean, String?) -> Unit
) {
    val fileName = "vouchers/$transactionId/${UUID.randomUUID()}.jpg"
    val fileRef = storageRef.child(fileName)

    val inputStream = context.contentResolver.openInputStream(imageUri)
    if (inputStream == null) {
        onResult(false, "No se pudo leer la imagen")
        return
    }

    fileRef.putStream(inputStream)
        .addOnSuccessListener {
            inputStream.close()
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                db.collection("transactions").document(transactionId)
                    .update(
                        mapOf(
                            "status" to "Validando Voucher",
                            "voucherUrl" to downloadUrl.toString()
                        )
                    )
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { e -> onResult(false, "Error al actualizar estado: ${e.message}") }
            }.addOnFailureListener { e ->
                onResult(false, "Error al obtener URL de descarga: ${e.message}")
            }
        }
        .addOnFailureListener { e ->
            inputStream.close()
            onResult(false, "Error al subir imagen: ${e.message}")
        }
}
