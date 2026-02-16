package com.miempresa.veterinaria.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.veterinaria.R // Asegúrate de importar tu R
import com.miempresa.veterinaria.ui.theme.VeterinariaAppTheme
import com.miempresa.veterinaria.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    // Ahora que arreglaste el MainViewModel, este error desaparecerá
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ⚠️ CAUSANDO LA FUGA INTENCIONALMENTE
        // Le entregamos "this" (la MainActivity) al objeto estático.
        // Aunque cierres la pantalla, SimuladorFuga seguirá apuntando a ella.
        //com.miempresa.veterinaria.util.SimuladorFuga.contextoAtrapado = this
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Asegúrate de tener este recurso o comenta la imagen si da error
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

        // Botón 1: Ir a Gestión (Esto abre GestionActivity)
        Button(
            onClick = {
                val intent = Intent(context, GestionActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("GESTIÓN GENERAL")
        }

        // El botón de Consultas era el que daba error.
        // Como las consultas están DENTRO de GestionActivity (en la pestaña 3),
        // simplemente abrimos la misma activity.
    }
}