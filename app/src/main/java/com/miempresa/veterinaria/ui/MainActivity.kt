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
import com.miempresa.veterinaria.R
import com.miempresa.veterinaria.ui.theme.VeterinariaAppTheme
import com.miempresa.veterinaria.util.AppLogger
import com.miempresa.veterinaria.util.SimuladorFuga
import com.miempresa.veterinaria.viewmodel.MainViewModel

/**
 * MainActivity - Pantalla de inicio de la aplicación (Paso 4-5 - Memory Leaks)
 *
 * Esta Activity demuestra cómo detectar y prevenir fugas de memoria usando LeakCanary.
 *
 * INSTRUCCIONES PARA REPRODUCIR MEMORY LEAK (Paso 5):
 * 1. Descomentar la línea marcada con "FUGA INTENCIONAL" en onCreate()
 * 2. Compilar y ejecutar la app
 * 3. Navegar a GestionActivity y luego presionar "Back"
 * 4. Repetir el paso 3 varias veces
 * 5. Esperar la notificación de LeakCanary
 * 6. Analizar el heap trace generado
 *
 * ANÁLISIS DEL HEAP TRACE:
 * LeakCanary mostrará un path como:
 * ```
 * ┬───
 * │ GC Root: Global variable in runtime
 * │
 * ├─ com.miempresa.veterinaria.util.SimuladorFuga class
 * │    ↓ static SimuladorFuga.contextoAtrapado
 * │                         ~~~~~~~~~~~~~~~~~~~
 * ╰→ com.miempresa.veterinaria.ui.MainActivity instance
 * ```
 *
 * SOLUCIÓN IMPLEMENTADA:
 * - Llamar SimuladorFuga.limpiarTodo() en onDestroy()
 * - Usar WeakReference para contextos que deben sobrevivir al ciclo de vida
 *
 * @author Veterinaria App Team
 * @since 1.0
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    /** ViewModel compartido para acceso a datos */
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    /**
     * Inicialización de la Activity
     *
     * Aquí se configura la UI con Jetpack Compose y se puede activar
     * la demostración de memory leaks para LeakCanary.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.i(TAG, "onCreate - Activity iniciada")

        // ====================================================================
        // PASO 4-5: DEMOSTRACIÓN DE MEMORY LEAK PARA LEAKCANARY
        // ====================================================================
        // INSTRUCCIONES:
        // 1. Descomentar UNA de las siguientes líneas para causar una fuga
        // 2. Ejecutar la app y navegar entre pantallas
        // 3. Presionar Back para destruir esta Activity
        // 4. LeakCanary detectará la fuga y mostrará una notificación
        //
        // TIPO 1: Contexto atrapado en singleton (más común)
        // SimuladorFuga.contextoAtrapado = this
        //
        // TIPO 2: Acumulación de contextos en lista
        // SimuladorFuga.acumularContexto(this)
        //
        // TIPO 3: Callback con referencia a Activity
        // SimuladorFuga.registrarCallback(object : SimuladorFuga.FugaCallback {
        //     override fun onEvento(mensaje: String) {
        //         // Este closure captura 'this@MainActivity'
        //         AppLogger.d(TAG, "Callback recibido: $mensaje en $this@MainActivity")
        //     }
        // })
        //
        // TIPO 4: Handler con tarea pendiente
        // SimuladorFuga.programarTareaConFuga(this, 60000) // 1 minuto
        // ====================================================================

        // Logging de estadísticas de retención (para debugging)
        AppLogger.d(TAG, SimuladorFuga.obtenerEstadisticas())

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

    /**
     * Limpieza al destruir la Activity (Paso 5 - Prevención de fugas)
     *
     * IMPORTANTE: Siempre limpiar referencias en onDestroy() para prevenir
     * memory leaks. Esto incluye:
     * - Callbacks registrados
     * - Handlers con tareas pendientes
     * - Referencias en singletons
     */
    override fun onDestroy() {
        AppLogger.i(TAG, "onDestroy - Limpiando referencias para prevenir memory leaks")

        // SOLUCIÓN: Limpiar todas las referencias del SimuladorFuga
        // Esto previene las fugas demostradas arriba
        SimuladorFuga.limpiarTodo()

        super.onDestroy()
    }

    /**
     * Logging del ciclo de vida para debugging
     */
    override fun onStart() {
        super.onStart()
        AppLogger.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        AppLogger.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        AppLogger.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        AppLogger.d(TAG, "onStop")
    }
}

/**
 * Pantalla de inicio de la aplicación
 *
 * Muestra el logo, título y botón de navegación principal
 */
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
        // Logo de la aplicación
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Logo Veterinaria App",
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Título principal
        Text(
            text = "Veterinaria App",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de navegación a gestión
        Button(
            onClick = {
                val intent = Intent(context, GestionActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("GESTIÓN GENERAL")
        }
    }
}
