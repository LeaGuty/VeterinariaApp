package com.miempresa.veterinaria.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.miempresa.veterinaria.model.Cliente
import com.miempresa.veterinaria.model.Mascota
import com.miempresa.veterinaria.viewmodel.MainViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PantallaMascotas(viewModel: MainViewModel) {
    val context = LocalContext.current
    // Obtenemos el valor de la lista de clientes desde el State
    val listaClientes by viewModel.clientes

    var mascotaAEditar by remember { mutableStateOf<Mascota?>(null) }
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var edadStr by remember { mutableStateOf("") }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var expandirDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(mascotaAEditar) {
        if (mascotaAEditar != null) {
            nombre = mascotaAEditar!!.nombre
            tipo = mascotaAEditar!!.tipo
            raza = mascotaAEditar!!.raza
            edadStr = mascotaAEditar!!.edad.toString()
            clienteSeleccionado = mascotaAEditar!!.dueno
        } else {
            nombre = ""; tipo = ""; raza = ""; edadStr = ""; clienteSeleccionado = null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().animateContentSize()
    ) {
        // Título dinámico con AnimatedContent
        AnimatedContent(
            targetState = mascotaAEditar == null,
            transitionSpec = {
                fadeIn(tween(220, delayMillis = 90)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)) with
                        fadeOut(tween(90))
            },
            label = "TituloMascotaAnim"
        ) { esNuevo ->
            Text(
                if (esNuevo) "Registrar Mascota" else "Editar Mascota",
                style = MaterialTheme.typography.titleLarge,
                color = if (esNuevo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = raza, onValueChange = { raza = it }, label = { Text("Raza") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = edadStr, onValueChange = { edadStr = it }, label = { Text("Edad") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            OutlinedButton(onClick = { expandirDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                Text(clienteSeleccionado?.nombre ?: "Seleccionar Dueño")
            }
            DropdownMenu(expanded = expandirDropdown, onDismissRequest = { expandirDropdown = false }) {
                listaClientes.forEach { cliente ->
                    DropdownMenuItem(text = { Text(cliente.nombre) }, onClick = { clienteSeleccionado = cliente; expandirDropdown = false })
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (nombre.isNotBlank() && clienteSeleccionado != null && edadStr.toIntOrNull() != null) {
                        // CORRECCIÓN AQUÍ: Usamos argumentos nombrados para evitar errores con el 'id'
                        val nuevaMascota = Mascota(
                            nombre = nombre,
                            tipo = tipo,
                            raza = raza,
                            edad = edadStr.toInt(),
                            dueno = clienteSeleccionado!!
                            // El id se pone en 0 automáticamente por defecto
                        )

                        if (mascotaAEditar == null) {
                            viewModel.registrarMascota(nuevaMascota)
                            Toast.makeText(context, "Mascota Creada", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.editarMascota(mascotaAEditar!!, nuevaMascota)
                            Toast.makeText(context, "Mascota Actualizada", Toast.LENGTH_SHORT).show()
                            mascotaAEditar = null
                        }
                    } else { Toast.makeText(context, "Faltan datos", Toast.LENGTH_SHORT).show() }
                },
                modifier = Modifier.weight(1f)
            ) { Text(if (mascotaAEditar == null) "Guardar" else "Actualizar") }

            AnimatedVisibility(visible = mascotaAEditar != null) {
                OutlinedButton(onClick = { mascotaAEditar = null }) { Text("Cancelar") }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Corrección: Accedemos al valor del State
        val textoBusqueda by viewModel.busquedaMascota
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { viewModel.busquedaMascota.value = it },
            label = { Text("Buscar mascota...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(viewModel.obtenerMascotasFiltradas()) { index, mascota ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500, delayMillis = index * 50)) +
                            slideInVertically(tween(500, delayMillis = index * 50)) { it / 2 }
                ) {
                    Card(elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mascota.nombre, fontWeight = FontWeight.Bold)
                                Text("${mascota.tipo} - ${mascota.raza}", style = MaterialTheme.typography.bodySmall)
                                Text("Dueño: ${mascota.dueno.nombre}", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { mascotaAEditar = mascota }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.tertiary)
                            }
                            IconButton(onClick = { viewModel.borrarMascota(mascota) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}