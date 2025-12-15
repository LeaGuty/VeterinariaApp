package com.miempresa.veterinaria.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miempresa.veterinaria.data.VeterinariaRepository
import com.miempresa.veterinaria.model.Cliente
import com.miempresa.veterinaria.model.Consulta
import com.miempresa.veterinaria.model.Mascota
import com.miempresa.veterinaria.model.Veterinario
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = VeterinariaRepository()

    // Estados de la UI (Lo que se verá en pantalla)
    val clientes = mutableStateOf<List<Cliente>>(emptyList())
    val mascotas = mutableStateOf<List<Mascota>>(emptyList())
    val consultas = mutableStateOf<List<Consulta>>(emptyList())
    val veterinarios = mutableStateOf<List<Veterinario>>(emptyList())

    val isLoading = mutableStateOf(false) // Para mostrar cargando

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        // Usamos viewModelScope para ejecutar tareas en segundo plano (Corrutinas)
        viewModelScope.launch {
            isLoading.value = true

            // Cargamos datos del repositorio
            clientes.value = repository.obtenerClientes()
            mascotas.value = repository.obtenerMascotas()
            consultas.value = repository.obtenerConsultas()
            veterinarios.value = repository.obtenerVeterinarios()

            isLoading.value = false
        }
    }

    // Funciones públicas para agregar datos desde la UI
    fun registrarCliente(cliente: Cliente) {
        repository.agregarCliente(cliente)
        // Actualizamos la lista observable
        viewModelScope.launch { clientes.value = repository.obtenerClientes() }
    }

    fun registrarMascota(mascota: Mascota) {
        repository.agregarMascota(mascota)
        viewModelScope.launch { mascotas.value = repository.obtenerMascotas() }
    }

    fun agendarConsulta(consulta: Consulta) {
        repository.agregarConsulta(consulta)
        viewModelScope.launch { consultas.value = repository.obtenerConsultas() }
    }

    fun borrarCliente(cliente: Cliente) {
        repository.eliminarCliente(cliente)
        // Actualizamos todas las listas porque borrar un cliente puede borrar mascotas
        viewModelScope.launch {
            clientes.value = repository.obtenerClientes()
            mascotas.value = repository.obtenerMascotas()
        }
    }

    fun borrarMascota(mascota: Mascota) {
        repository.eliminarMascota(mascota)
        viewModelScope.launch { mascotas.value = repository.obtenerMascotas() }
    }

    fun borrarConsulta(consulta: Consulta) {
        repository.eliminarConsulta(consulta)
        viewModelScope.launch { consultas.value = repository.obtenerConsultas() }
    }
}