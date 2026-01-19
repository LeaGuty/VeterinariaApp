package com.miempresa.veterinaria.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.veterinaria.ui.theme.VeterinariaAppTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeterinariaAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaLogin(
                        modifier = Modifier.padding(innerPadding),
                        onLoginSuccess = {
                            // Navegar al MainActivity (Menú Principal)
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Cierra el Login para que no se pueda volver atrás
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaLogin(modifier: Modifier = Modifier, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Estados del formulario
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorLogin by remember { mutableStateOf(false) }
    var mostrarDialogoRecuperar by remember { mutableStateOf(false) }

    // Credenciales simuladas (Hardcoded)
    val correoCorrecto = "admin@veterinaria.com"
    val passwordCorrecta = "1234"

    // Función de login reutilizable
    val realizarLogin = {
        if (correo == correoCorrecto && password == passwordCorrecta) {
            onLoginSuccess()
        } else {
            errorLogin = true
            Toast.makeText(context, "Error de autenticación", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo o Ícono
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bienvenido",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "Inicia sesión para continuar", color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // Campo Correo
        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                errorLogin = false
            },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = errorLogin
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorLogin = false
            },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    realizarLogin()
                }
            ),
            isError = errorLogin
        )

        // Mensaje de Error
        AnimatedVisibility(visible = errorLogin) {
            Text(
                text = "Credenciales incorrectas (Prueba: admin@veterinaria.com / 1234)",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Olvidé mi contraseña
        TextButton(onClick = { mostrarDialogoRecuperar = true }) {
            Text("¿Olvidaste tu contraseña?")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Login
        Button(
            onClick = realizarLogin,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("INGRESAR")
        }
    }

    // Diálogo de Recuperación de Contraseña
    if (mostrarDialogoRecuperar) {
        DialogoRecuperarPassword(onDismiss = { mostrarDialogoRecuperar = false })
    }
}

@Composable
fun DialogoRecuperarPassword(onDismiss: () -> Unit) {
    var correoRecuperacion by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recuperar Contraseña") },
        text = {
            Column {
                Text("Ingresa tu correo para recibir un enlace de recuperación.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = correoRecuperacion,
                    onValueChange = { correoRecuperacion = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (correoRecuperacion.isNotEmpty()) {
                    Toast.makeText(context, "Correo de recuperación enviado a $correoRecuperacion", Toast.LENGTH_LONG).show()
                    onDismiss()
                } else {
                    Toast.makeText(context, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
