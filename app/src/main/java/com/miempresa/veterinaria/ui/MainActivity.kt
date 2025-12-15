package com.miempresa.veterinaria.ui

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.veterinaria.receiver.ModoAvionReceiver
import com.miempresa.veterinaria.service.RecordatorioService

class MainActivity : ComponentActivity() {

    // Instancia del Receiver
    private val modoAvionReceiver = ModoAvionReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                // Registramos el Receiver cuando la pantalla está activa
                RegistroReceiver(modoAvionReceiver, this)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MenuPrincipalScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Función auxiliar para manejar el ciclo de vida del Receiver (registro dinámico)
@Composable
fun RegistroReceiver(receiver: ModoAvionReceiver, activity: ComponentActivity) {
    DisposableEffect(Unit) {
        val intentFilter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        activity.registerReceiver(receiver, intentFilter)

        onDispose {
            activity.unregisterReceiver(receiver)
        }
    }
}

@Composable
fun MenuPrincipalScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = "Logo Veterinaria",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Veterinaria App",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botón 1: Gestión (Activity)
        BotonMenu(texto = "Gestión Veterinaria") {
            val intent = Intent(context, GestionActivity::class.java)
            context.startActivity(intent)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón 2: Servicio (Service) - ¡NUEVO!
        OutlinedButton(
            onClick = {
                // Iniciar el servicio explícitamente
                val intentService = Intent(context, RecordatorioService::class.java)
                context.startService(intentService)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Simular Sincronización (Servicio)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Prueba el Receiver activando el Modo Avión en tu barra de estado",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center)
    }
}

@Composable
fun BotonMenu(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Text(text = texto, fontSize = 18.sp)
    }
}