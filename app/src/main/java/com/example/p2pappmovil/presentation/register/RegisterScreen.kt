package com.example.p2pappmovil.presentation.register

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var celular by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nombresError by remember { mutableStateOf<String?>(null) }
    var apellidosError by remember { mutableStateOf<String?>(null) }
    var correoError by remember { mutableStateOf<String?>(null) }
    var celularError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var registrationMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(registrationMessage, isError) {
        if (registrationMessage == "Cuenta creada correctamente" && !isError) {
            kotlinx.coroutines.delay(1000)
            onRegisterSuccess()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Completa tus datos para empezar a operar con seguridad.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nombres
        TextField(
            value = nombres,
            onValueChange = { nombres = it; nombresError = null },
            label = { Text("Nombres") },
            modifier = Modifier.fillMaxWidth(),
            isError = nombresError != null,
            enabled = !isLoading,
            supportingText = { nombresError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Apellidos
        TextField(
            value = apellidos,
            onValueChange = { apellidos = it; apellidosError = null },
            label = { Text("Apellidos") },
            modifier = Modifier.fillMaxWidth(),
            isError = apellidosError != null,
            enabled = !isLoading,
            supportingText = { apellidosError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Correo
        TextField(
            value = correo,
            onValueChange = { correo = it; correoError = null },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = correoError != null,
            enabled = !isLoading,
            supportingText = { correoError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Celular
        TextField(
            value = celular,
            onValueChange = { 
                if (it.length <= 9 && it.all { char -> char.isDigit() }) {
                    celular = it
                    celularError = null 
                }
            },
            label = { Text("Celular (9 dígitos)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = celularError != null,
            enabled = !isLoading,
            supportingText = { celularError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Contraseña
        TextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = { Text("Contraseña (mín. 8 caracteres)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError != null,
            enabled = !isLoading,
            supportingText = { passwordError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confirmar Contraseña
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordError = null },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPasswordError != null,
            enabled = !isLoading,
            supportingText = { confirmPasswordError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (registrationMessage.isNotEmpty()) {
            Text(
                text = registrationMessage,
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
                
                if (nombres.isBlank()) {
                    nombresError = "Nombre obligatorio"
                    hasError = true
                }
                if (apellidos.isBlank()) {
                    apellidosError = "Apellido obligatorio"
                    hasError = true
                }
                if (correo.isBlank()) {
                    correoError = "Correo obligatorio"
                    hasError = true
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                    correoError = "Formato de correo inválido"
                    hasError = true
                }
                if (celular.length != 9) {
                    celularError = "Celular debe tener 9 dígitos"
                    hasError = true
                }
                if (password.length < 8) {
                    passwordError = "Mínimo 8 caracteres"
                    hasError = true
                }
                if (confirmPassword != password) {
                    confirmPasswordError = "Las contraseñas no coinciden"
                    hasError = true
                }

                if (!hasError) {
                    isLoading = true
                    registrationMessage = ""
                    isError = false
                    
                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseFirestore.getInstance()

                    auth.createUserWithEmailAndPassword(correo, password)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                if (uid != null) {
                                    Log.d("RegisterScreen", "UID creado: $uid")
                                    Log.d("RegisterScreen", "Correo: $correo")
                                    
                                    val userProfile = hashMapOf(
                                        "nombres" to nombres,
                                        "apellidos" to apellidos,
                                        "correo" to correo,
                                        "celular" to celular,
                                        "rol" to "USER",
                                        "fechaRegistro" to FieldValue.serverTimestamp()
                                    )

                                    db.collection("users").document(uid).set(userProfile)
                                        .addOnCompleteListener { dbTask ->
                                            isLoading = false
                                            if (dbTask.isSuccessful) {
                                                registrationMessage = "Cuenta creada correctamente"
                                                isError = false
                                                Log.d("RegisterScreen", "Documento Firestore creado: $uid")
                                            } else {
                                                registrationMessage = "La cuenta fue creada pero no se pudo guardar el perfil."
                                                isError = true
                                                Log.e("RegisterScreen", "Error al guardar en Firestore: ${dbTask.exception?.message}", dbTask.exception)
                                            }
                                        }
                                } else {
                                    isLoading = false
                                    isError = true
                                    registrationMessage = "Error inesperado al obtener UID"
                                }
                            } else {
                                isLoading = false
                                isError = true
                                val exception = authTask.exception
                                registrationMessage = when (exception) {
                                    is FirebaseAuthUserCollisionException -> "Este correo ya está registrado."
                                    is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil."
                                    is FirebaseAuthInvalidCredentialsException -> "El correo electrónico no es válido."
                                    else -> {
                                        Log.e("RegisterScreen", "Error en registro: ${exception?.message}", exception)
                                        "Error: ${exception?.localizedMessage ?: "Ocurrió un error inesperado"}"
                                    }
                                }
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Crear Cuenta")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onLoginClick, enabled = !isLoading) {
            Text("¿Ya tienes una cuenta? Iniciar Sesión")
        }
    }
}
