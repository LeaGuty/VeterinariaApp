package com.miempresa.veterinaria.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape // NUEVO: Para la imagen redonda
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // NUEVO: Para recortar la imagen
import androidx.compose.ui.layout.ContentScale // NUEVO: Para ajustar la imagen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
// NUEVO: Imports de Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.miempresa.veterinaria.model.Cliente
import com.miempresa.veterinaria.util.Validaciones
import com.miempresa.veterinaria.viewmodel.MainViewModel

@Composable
fun PantallaGestion(viewModel: MainViewModel) {
    var pestanaActual by remember { mutableIntStateOf(0) }
    val titulos = listOf("Clientes", "Mascotas", "Agenda")
    var clienteAEditar by remember { mutableStateOf<Cliente?>(null) }

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

        // Navegación entre pestañas con Crossfade
        Crossfade(
            targetState = pestanaActual,
            animationSpec = tween(durationMillis = 500),
            label = "PestanasTransition"
        ) { targetPestana ->
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (targetPestana) {
                    0 -> {
                        Column {
                            FormularioCliente(viewModel, clienteAEditar) { clienteAEditar = null }
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                            ListaClientes(viewModel, onEditar = { cliente -> clienteAEditar = cliente })
                        }
                    }
                    1 -> PantallaMascotas(viewModel)
                    2 -> PantallaConsultas(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FormularioCliente(
    viewModel: MainViewModel,
    clienteAEditar: Cliente?,
    onLimpiarEdicion: () -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }

    LaunchedEffect(clienteAEditar) {
        if (clienteAEditar != null) {
            nombre = clienteAEditar.nombre
            correo = clienteAEditar.correo
            telefono = clienteAEditar.telefono
            rut = clienteAEditar.rut
        } else {
            nombre = ""; correo = ""; telefono = ""; rut = ""
        }
    }

    val formularioValido = Validaciones.esTextoValido(nombre) &&
            Validaciones.esCorreoValido(correo) &&
            Validaciones.esTelefonoValido(telefono) &&
            Validaciones.esRutValido(rut)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.animateContentSize()
    ) {
        AnimatedContent(
            targetState = clienteAEditar == null,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)) with
                        fadeOut(animationSpec = tween(90))
            },
            label = "TituloFormulario"
        ) { esNuevo ->
            Text(
                if (esNuevo) "Nuevo Cliente" else "Editando Cliente",
                style = MaterialTheme.typography.titleLarge,
                color = if (esNuevo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }

        OutlinedTextField(
            value = nombre, onValueChange = { nombre = it },
            label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )
        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("Correo") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = correo.isNotEmpty() && !Validaciones.esCorreoValido(correo)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = telefono, onValueChange = { telefono = it },
                label = { Text("Teléfono") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            OutlinedTextField(
                value = rut, onValueChange = { rut = it },
                label = { Text("RUT") }, modifier = Modifier.weight(1f),
                isError = rut.isNotEmpty() && !Validaciones.esRutValido(rut),
                enabled = clienteAEditar == null
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (clienteAEditar == null) {
                        // NUEVO: Usamos la función que busca la foto en la API
                        viewModel.registrarClienteConFoto(nombre, correo, telefono, rut)
                        Toast.makeText(context, "Cliente Guardado", Toast.LENGTH_SHORT).show()
                        // Limpiamos campos solo al crear
                        nombre = ""; correo = ""; telefono = ""; rut = ""
                    } else {
                        // NUEVO: Al editar, preservamos la foto original que ya tenía el cliente
                        val clienteActualizado = Cliente(
                            nombre = nombre,
                            correo = correo,
                            telefono = telefono,
                            rut = rut,
                            fotoUri = clienteAEditar.fotoUri // Importante para no perder la foto
                        )
                        viewModel.editarCliente(clienteAEditar, clienteActualizado)
                        Toast.makeText(context, "Cliente Actualizado", Toast.LENGTH_SHORT).show()
                        onLimpiarEdicion()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = formularioValido
            ) {
                Text(if (clienteAEditar == null) "Guardar" else "Actualizar")
            }

            AnimatedVisibility(visible = clienteAEditar != null) {
                OutlinedButton(onClick = { onLimpiarEdicion() }) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class) // NUEVO: Requerido para GlideImage
@Composable
fun ListaClientes(viewModel: MainViewModel, onEditar: (Cliente) -> Unit) {
    val listaFiltrada = viewModel.obtenerClientesFiltrados()
    val textoBusqueda by viewModel.busquedaCliente
    val context = LocalContext.current
    var clienteAEliminar by remember { mutableStateOf<Cliente?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { viewModel.busquedaCliente.value = it },
            label = { Text("Buscar por Nombre o RUT") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            singleLine = true
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(listaFiltrada) { index, cliente ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500, delayMillis = index * 50)) +
                            slideInHorizontally(tween(500, delayMillis = index * 50)) { -it / 2 }
                ) {
                    Card(elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // NUEVO: Componente Glide para mostrar la foto del cliente
                            GlideImage(
                                model = cliente.fotoUri,
                                contentDescription = "Foto de ${cliente.nombre}",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(cliente.nombre, fontWeight = FontWeight.Bold)
                                Text(cliente.rut, style = MaterialTheme.typography.bodySmall)
                                Text(cliente.correo, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { onEditar(cliente) }) {
                                Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.tertiary)
                            }
                            IconButton(onClick = {
                                val intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Cliente: ${cliente.nombre} - ${cliente.telefono}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir"))
                            }) {
                                Icon(Icons.Default.Share, "Compartir", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { clienteAEliminar = cliente }) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    clienteAEliminar?.let { cliente ->
        AlertDialog(
            onDismissRequest = { clienteAEliminar = null },
            title = { Text("Eliminar Cliente") },
            text = { Text("¿Estás seguro de que deseas eliminar a ${cliente.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.borrarCliente(cliente)
                        clienteAEliminar = null
                        Toast.makeText(context, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { clienteAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}