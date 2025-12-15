package com.miempresa.veterinaria.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miempresa.veterinaria.model.*
import com.miempresa.veterinaria.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConsultas(viewModel: MainViewModel) {
    val context = LocalContext.current
    val listaMascotas by viewModel.mascotas
    val listaVeterinarios by viewModel.veterinarios
    val listaConsultas by viewModel.consultas

    // Estados del formulario
    var mascotaSel by remember { mutableStateOf<Mascota?>(null) }
    var vetSel by remember { mutableStateOf<Veterinario?>(null) }
    var fechaSel by remember { mutableStateOf(LocalDate.now()) }
    var horaSel by remember { mutableStateOf<LocalTime?>(null) }
    var motivo by remember { mutableStateOf("") }

    // Estados para Dropdowns y Dialogs
    var expandirMascota by remember { mutableStateOf(false) }
    var expandirVet by remember { mutableStateOf(false) }
    var mostrarCalendario by remember { mutableStateOf(false) }

    // Calcular horas disponibles dinámicamente
    val horasDisponibles = remember(vetSel, fechaSel, listaConsultas) {
        vetSel?.obtenerHorasDisponibles(fechaSel, listaConsultas) ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Agendar Consulta", style = MaterialTheme.typography.headlineSmall)

        // 1. Selección de Mascota
        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedButton(onClick = { expandirMascota = true }, modifier = Modifier.fillMaxWidth()) {
                Text(mascotaSel?.nombre ?: "Seleccionar Paciente (Mascota)")
            }
            DropdownMenu(expanded = expandirMascota, onDismissRequest = { expandirMascota = false }) {
                listaMascotas.forEach { mascota ->
                    DropdownMenuItem(text = { Text(mascota.nombre) }, onClick = { mascotaSel = mascota; expandirMascota = false })
                }
            }
        }

        // 2. Selección de Veterinario
        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedButton(onClick = { expandirVet = true }, modifier = Modifier.fillMaxWidth()) {
                Text(vetSel?.nombre ?: "Seleccionar Veterinario")
            }
            DropdownMenu(expanded = expandirVet, onDismissRequest = { expandirVet = false }) {
                listaVeterinarios.forEach { vet ->
                    DropdownMenuItem(text = { Text(vet.nombre) }, onClick = { vetSel = vet; horaSel = null; expandirVet = false })
                }
            }
        }

        // 3. Fecha (Calendario)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Text("Fecha: ${fechaSel.format(DateTimeFormatter.ISO_LOCAL_DATE)}", modifier = Modifier.weight(1f))
            IconButton(onClick = { mostrarCalendario = true }) {
                Icon(Icons.Default.CalendarToday, null)
            }
        }

        if (mostrarCalendario) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { mostrarCalendario = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            fechaSel = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        }
                        mostrarCalendario = false
                    }) { Text("Aceptar") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // 4. Horas Disponibles (Lista horizontal)
        Text("Horarios Disponibles:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        if (vetSel == null) {
            Text("Selecciona un veterinario primero", color = MaterialTheme.colorScheme.secondary)
        } else if (horasDisponibles.isEmpty()) {
            Text("No hay horas para esta fecha", color = MaterialTheme.colorScheme.error)
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                horasDisponibles.take(4).forEach { hora ->
                    FilterChip(
                        selected = horaSel == hora,
                        onClick = { horaSel = hora },
                        label = { Text(hora.toString()) }
                    )
                }
            }
        }

        OutlinedTextField(value = motivo, onValueChange = { motivo = it }, label = { Text("Motivo Consulta") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (mascotaSel != null && vetSel != null && horaSel != null) {
                    val nuevaConsulta = Consulta(mascotaSel!!, vetSel!!, fechaSel, horaSel!!, TipoConsulta.GENERAL, motivo)
                    viewModel.agendarConsulta(nuevaConsulta)
                    Toast.makeText(context, "Cita Agendada", Toast.LENGTH_SHORT).show()
                    motivo = ""; horaSel = null
                } else {
                    Toast.makeText(context, "Faltan datos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            enabled = horaSel != null
        ) {
            Text("Confirmar Cita")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Lista de Citas Agendadas
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listaConsultas) { consulta ->
                Card(elevation = CardDefaults.cardElevation(4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${consulta.fecha} - ${consulta.hora}", fontWeight = FontWeight.Bold)
                            Text("Paciente: ${consulta.mascota.nombre}")
                            Text("Vet: ${consulta.veterinario.nombre}", style = MaterialTheme.typography.bodySmall)
                            Text("Motivo: ${consulta.motivoConsulta}", style = MaterialTheme.typography.bodySmall)
                        }

                        // Botones de Acción
                        Row {
                            // Botón COMPARTIR (Intent Implícito)
                            IconButton(onClick = {
                                val resumen = """
                                    📅 Cita Veterinaria Agendada
                                    Fecha: ${consulta.fecha} a las ${consulta.hora}
                                    Paciente: ${consulta.mascota.nombre}
                                    Veterinario: ${consulta.veterinario.nombre}
                                    Motivo: ${consulta.motivoConsulta}
                                """.trimIndent()

                                val intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, resumen)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(intent, "Compartir Cita")
                                context.startActivity(shareIntent)
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.primary)
                            }

                            // Botón BORRAR
                            IconButton(onClick = { viewModel.borrarConsulta(consulta) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}