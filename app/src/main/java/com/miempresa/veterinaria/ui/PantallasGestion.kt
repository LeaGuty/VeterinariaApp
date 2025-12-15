package com.miempresa.veterinaria.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.veterinaria.model.Cliente
import com.miempresa.veterinaria.util.Validaciones
import com.miempresa.veterinaria.viewmodel.MainViewModel

@Composable
fun PantallaGestion(viewModel: MainViewModel) {
    var pestanaActual by remember { mutableStateOf(0) }
    val titulos = listOf("Clientes", "Mascotas", "Agenda")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pestanaActual) {
            titulos.forEachIndexed { index, titulo ->
                Tab(
                    selected = pestanaActual == index,
                    onClick = { pestanaActual = index },
                    text = { Text(titulo) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (pestanaActual) {
                0 -> {
                    // Vista combinada de Formulario y Lista de Clientes (lo que ya tenías)
                    Column {
                        FormularioCliente(viewModel)
                        Spacer(modifier = Modifier.height(16.dp))
                        ListaClientes(viewModel)
                    }
                }
                1 -> PantallaMascotas(viewModel)
                2 -> PantallaConsultas(viewModel)
            }
        }
    }
}

@Composable
fun FormularioCliente(viewModel: MainViewModel) {
    val context = LocalContext.current

    // Variables del formulario
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }

    // --- CORRECCIÓN AQUÍ ---
    // Ahora el botón solo se activa si TODAS las validaciones pasan, incluido el RUT
    val formularioValido = Validaciones.esTextoValido(nombre) &&
            Validaciones.esCorreoValido(correo) &&
            Validaciones.esTelefonoValido(telefono) &&
            Validaciones.esRutValido(rut) // <--- ¡Esto faltaba!

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nuevo Cliente", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            isError = correo.isNotEmpty() && !Validaciones.esCorreoValido(correo),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono (+56...)") },
            modifier = Modifier.fillMaxWidth(),
            isError = telefono.isNotEmpty() && !Validaciones.esTelefonoValido(telefono),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        // --- CAMPO RUT MEJORADO ---
        OutlinedTextField(
            value = rut,
            onValueChange = { rut = it },
            label = { Text("RUT (ej: 12.345.678-9)") },
            modifier = Modifier.fillMaxWidth(),
            // Se marca error si no está vacío Y el RUT no es válido
            isError = rut.isNotEmpty() && !Validaciones.esRutValido(rut)
        )

        Button(
            onClick = {
                val nuevoCliente = Cliente(nombre, correo, telefono, rut)
                viewModel.registrarCliente(nuevoCliente)

                Toast.makeText(context, "Cliente Guardado!", Toast.LENGTH_SHORT).show()

                // Limpiar campos
                nombre = ""
                correo = ""
                telefono = ""
                rut = ""
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = formularioValido // Ahora respeta la validación del RUT
        ) {
            Text("Guardar Cliente")
        }
    }
}

@Composable
fun ListaClientes(viewModel: MainViewModel) {
    val listaClientes by viewModel.clientes
    val context = LocalContext.current // Necesario para lanzar el Intent

    if (listaClientes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay clientes registrados aún.")
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaClientes) { cliente ->
                Card(
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Datos del Cliente
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = cliente.nombre, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "📧 ${cliente.correo}")
                            Text(text = "📱 ${cliente.telefono}")
                        }

                        // Botón de Compartir (Intent Implícito)
                        IconButton(onClick = {
                            val textoACompartir = """
                                🐾 Ficha de Cliente 🐾
                                Nombre: ${cliente.nombre}
                                Contacto: ${cliente.telefono}
                                Correo: ${cliente.correo}
                            """.trimIndent()

                            // Crear el Intent Implícito
                            val intent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, textoACompartir)
                                type = "text/plain"
                            }
                            // Lanzar el menú de compartir del sistema
                            val shareIntent = android.content.Intent.createChooser(intent, "Compartir cliente vía...")
                            context.startActivity(shareIntent)
                        }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Share,
                                contentDescription = "Compartir",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}