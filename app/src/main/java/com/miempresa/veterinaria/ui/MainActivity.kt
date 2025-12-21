package com.miempresa.veterinaria.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.miempresa.veterinaria.R
import com.miempresa.veterinaria.service.RecordatorioService
import com.miempresa.veterinaria.ui.theme.VeterinariaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VeterinariaAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaInicio()
                }
            }
        }
    }
}

@Composable
fun PantallaInicio() {
    val context = LocalContext.current

    // Lógica para pedir permisos de notificación (Android 13+)
    val launcherPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { esConcedido ->
            if (esConcedido) {
                // Iniciar servicio si dan permiso
                val intentService = Intent(context, RecordatorioService::class.java)
                context.startService(intentService)
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permiso = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED) {
                // Ya tiene permiso, iniciar servicio
                val intentService = Intent(context, RecordatorioService::class.java)
                context.startService(intentService)
            } else {
                // Pedir permiso
                launcherPermisos.launch(permiso)
            }
        } else {
            // Android antiguo, iniciar directo
            val intentService = Intent(context, RecordatorioService::class.java)
            context.startService(intentService)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Veterinaria App",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                val intent = Intent(context, GestionActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("IR A GESTIÓN")
        }
    }
}