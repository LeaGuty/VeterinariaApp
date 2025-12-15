package com.miempresa.veterinaria.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.veterinaria.model.Cliente
import com.miempresa.veterinaria.model.Mascota
import com.miempresa.veterinaria.viewmodel.MainViewModel

@Composable
fun PantallaMascotas(viewModel: MainViewModel) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var edadStr by remember { mutableStateOf("") }

    // Para el Dropdown de Dueños
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var expandirDropdown by remember { mutableStateOf(false) }

    val listaClientes by viewModel.clientes
    val listaMascotas by viewModel.mascotas
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Registro de Mascotas", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))

        // Formulario
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Mascota") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo (Perro, Gato...)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = raza, onValueChange = { raza = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = edadStr, onValueChange = { edadStr = it }, label = { Text("Edad (años)") }, modifier = Modifier.fillMaxWidth())

        // Selector de Dueño (Dropdown)
        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedButton(onClick = { expandirDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                Text(clienteSeleccionado?.nombre ?: "Seleccionar Dueño")
            }
            DropdownMenu(expanded = expandirDropdown, onDismissRequest = { expandirDropdown = false }) {
                listaClientes.forEach { cliente ->
                    DropdownMenuItem(
                        text = { Text(cliente.nombre) },
                        onClick = {
                            clienteSeleccionado = cliente
                            expandirDropdown = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                if (nombre.isNotBlank() && clienteSeleccionado != null && edadStr.toIntOrNull() != null) {
                    val nuevaMascota = Mascota(nombre, tipo, raza, edadStr.toInt(), clienteSeleccionado!!)
                    viewModel.registrarMascota(nuevaMascota)
                    Toast.makeText(context, "Mascota Registrada", Toast.LENGTH_SHORT).show()
                    nombre = ""; tipo = ""; raza = ""; edadStr = ""; clienteSeleccionado = null
                } else {
                    Toast.makeText(context, "Faltan datos o edad inválida", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Guardar Mascota")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Lista con Opción de Eliminar
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listaMascotas) { mascota ->
                Card(elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(mascota.nombre, fontWeight = FontWeight.Bold)
                            Text("${mascota.tipo} - ${mascota.raza}", style = MaterialTheme.typography.bodySmall)
                            Text("Dueño: ${mascota.dueno.nombre}", style = MaterialTheme.typography.bodySmall)
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