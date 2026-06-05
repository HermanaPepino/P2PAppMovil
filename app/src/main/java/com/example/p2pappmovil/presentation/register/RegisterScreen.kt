package com.example.p2pappmovil.presentation.register

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
            supportingText = { confirmPasswordError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (registrationMessage.isNotEmpty()) {
            Text(
                text = registrationMessage,
                color = Color.Green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
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
                    registrationMessage = "Cuenta creada correctamente"
                    onRegisterSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Cuenta")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onLoginClick) {
            Text("¿Ya tienes una cuenta? Iniciar Sesión")
        }
    }
}
