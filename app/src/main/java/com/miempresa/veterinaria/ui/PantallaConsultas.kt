package com.miempresa.veterinaria.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.miempresa.veterinaria.model.*
import com.miempresa.veterinaria.viewmodel.MainViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
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

    var expandirMascota by remember { mutableStateOf(false) }
    var expandirVet by remember { mutableStateOf(false) }
    var mostrarCalendario by remember { mutableStateOf(false) }

    val horasDisponibles = remember(vetSel, fechaSel, listaConsultas) {
        val disponibles = vetSel?.obtenerHorasDisponibles(fechaSel, listaConsultas) ?: emptyList()
        if (fechaSel == LocalDate.now()) {
            disponibles.filter { it.isAfter(LocalTime.now()) }
        } else {
            disponibles
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Agendar Consulta", style = MaterialTheme.typography.headlineSmall)

        // 1. Selección de Mascota (Muestra foto del dueño si existe)
        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedButton(onClick = { expandirMascota = true }, modifier = Modifier.fillMaxWidth()) {
                Text(mascotaSel?.nombre ?: "Seleccionar Paciente (Mascota)")
            }
            DropdownMenu(expanded = expandirMascota, onDismissRequest = { expandirMascota = false }) {
                listaMascotas.forEach { mascota ->
                    DropdownMenuItem(
                        leadingIcon = {
                            GlideImage(
                                model = mascota.dueno.fotoUri,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        },
                        text = { Text("${mascota.nombre} (${mascota.dueno.nombre})") },
                        onClick = { mascotaSel = mascota; expandirMascota = false }
                    )
                }
            }
        }

        // 2. Selección de Veterinario (CON FOTO DE API)
        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedButton(onClick = { expandirVet = true }, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (vetSel?.fotoUrl != null) {
                        GlideImage(
                            model = vetSel?.fotoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(vetSel?.nombre ?: "Seleccionar Veterinario")
                }
            }
            DropdownMenu(expanded = expandirVet, onDismissRequest = { expandirVet = false }) {
                listaVeterinarios.forEach { vet ->
                    DropdownMenuItem(
                        leadingIcon = {
                            GlideImage(
                                model = vet.fotoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        },
                        text = { Text(vet.nombre) },
                        onClick = { vetSel = vet; horaSel = null; expandirVet = false }
                    )
                }
            }
        }

        // 3. Fecha
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Text("Fecha: ${fechaSel.format(DateTimeFormatter.ISO_LOCAL_DATE)}", modifier = Modifier.weight(1f))
            IconButton(onClick = { mostrarCalendario = true }) {
                Icon(Icons.Default.CalendarToday, null)
            }
        }

        if (mostrarCalendario) {
            val datePickerState = rememberDatePickerState(
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val startOfToday = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                        return utcTimeMillis >= startOfToday
                    }
                }
            )
            DatePickerDialog(
                onDismissRequest = { mostrarCalendario = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            fechaSel = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            horaSel = null
                        }
                        mostrarCalendario = false
                    }) { Text("Aceptar") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // 4. Horas
        Text("Horarios Disponibles:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        if (vetSel == null) {
            Text("Selecciona un veterinario primero", color = MaterialTheme.colorScheme.secondary)
        } else if (horasDisponibles.isEmpty()) {
            Text("No hay horas disponibles", color = MaterialTheme.colorScheme.error)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(horasDisponibles) { hora ->
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
                    val nuevaConsulta = Consulta(
                        id = 0,
                        mascota = mascotaSel!!,
                        veterinario = vetSel!!,
                        fecha = fechaSel,
                        hora = horaSel!!,
                        tipoConsulta = TipoConsulta.GENERAL,
                        motivoConsulta = motivo
                    )
                    viewModel.agendarConsulta(nuevaConsulta)
                    Toast.makeText(context, "Cita Agendada", Toast.LENGTH_SHORT).show()
                    motivo = ""; horaSel = null
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            enabled = horaSel != null
        ) { Text("Confirmar Cita") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Lista de Citas con IMÁGENES
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listaConsultas) { consulta ->
                Card(elevation = CardDefaults.cardElevation(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Imagen del Veterinario asignado
                        GlideImage(
                            model = consulta.veterinario.fotoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text("${consulta.fecha} - ${consulta.hora}", fontWeight = FontWeight.Bold)
                            Text("Paciente: ${consulta.mascota.nombre}", style = MaterialTheme.typography.bodyMedium)
                            Text("Vet: ${consulta.veterinario.nombre}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = { viewModel.borrarConsulta(consulta) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}