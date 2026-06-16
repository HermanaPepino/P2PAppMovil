package com.example.p2pappmovil.presentation.login

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onAdminLoginSuccess: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var correoError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var loginMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ingresa tus credenciales para continuar.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = correo,
            onValueChange = { correo = it; correoError = null },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            isError = correoError != null,
            enabled = !isLoading,
            supportingText = { correoError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            enabled = !isLoading,
            supportingText = { passwordError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (loginMessage.isNotEmpty()) {
            Text(
                text = loginMessage,
                color = if (isError) Color.Red else Color.Green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
        }

        Button(
            onClick = {
                var hasError = false
                if (correo.isBlank()) {
                    correoError = "El correo es obligatorio"
                    hasError = true
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                    correoError = "Formato de correo inválido"
                    hasError = true
                }
                
                if (password.isBlank()) {
                    passwordError = "La contraseña es obligatoria"
                    hasError = true
                }

                if (!hasError) {
                    isLoading = true
                    loginMessage = ""
                    isError = false

                    val auth = FirebaseAuth.getInstance()
                    auth.signInWithEmailAndPassword(correo, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                if (uid != null) {
                                    Log.d("LoginScreen", "UID: $uid")
                                    FirebaseFirestore.getInstance().collection("users").document(uid).get()
                                        .addOnSuccessListener { document ->
                                            isLoading = false
                                            if (document.exists()) {
                                                val rol = document.getString("rol") ?: "USER"
                                                Log.d("LoginScreen", "ROL: $rol")
                                                Log.d("LoginScreen", "Pantalla destino: ${if (rol == "ADMIN") "ADMIN" else "USER"}")
                                                
                                                if (rol == "ADMIN") {
                                                    onAdminLoginSuccess()
                                                } else {
                                                    onLoginSuccess()
                                                }
                                            } else {
                                                isError = true
                                                loginMessage = "Documento de usuario no encontrado."
                                                Log.e("LoginScreen", "Documento users no encontrado para UID: $uid")
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            isError = true
                                            loginMessage = "Error al consultar perfil."
                                            Log.e("LoginScreen", "Error Firestore: ${e.message}")
                                        }
                                }
                            } else {
                                isLoading = false
                                isError = true
                                val exception = task.exception
                                loginMessage = when (exception) {
                                    is FirebaseAuthInvalidUserException -> "Usuario inexistente."
                                    is FirebaseAuthInvalidCredentialsException -> "Contraseña incorrecta o correo inválido."
                                    else -> {
                                        Log.e("LoginScreen", "Error Login: ${exception?.message}")
                                        "Error de conexión o inesperado."
                                    }
                                }
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onRegisterClick, enabled = !isLoading) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}
