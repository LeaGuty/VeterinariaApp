package com.miempresa.veterinaria.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miempresa.veterinaria.VeterinariaApplication
import com.miempresa.veterinaria.data.VeterinariaRepository
import com.miempresa.veterinaria.model.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: VeterinariaRepository) : ViewModel() {

    val clientes = mutableStateOf<List<Cliente>>(emptyList())
    val mascotas = mutableStateOf<List<Mascota>>(emptyList())
    val consultas = mutableStateOf<List<Consulta>>(emptyList())
    val veterinarios = mutableStateOf<List<Veterinario>>(emptyList())

    var busquedaCliente = mutableStateOf("")
    var busquedaMascota = mutableStateOf("")

    init {
        viewModelScope.launch { repository.clientes.collect { clientes.value = it } }
        viewModelScope.launch { repository.mascotas.collect { mascotas.value = it } }
        viewModelScope.launch { repository.consultas.collect { consultas.value = it } }
        veterinarios.value = repository.obtenerVeterinarios()
    }

    // --- Clientes ---
    fun registrarCliente(cliente: Cliente) = viewModelScope.launch { repository.agregarCliente(cliente) }
    fun borrarCliente(cliente: Cliente) = viewModelScope.launch { repository.eliminarCliente(cliente) }
    fun editarCliente(antiguo: Cliente, nuevo: Cliente) = viewModelScope.launch { repository.actualizarCliente(antiguo, nuevo) }

    // --- Mascotas ---
    fun registrarMascota(mascota: Mascota) = viewModelScope.launch { repository.agregarMascota(mascota) }

    // ESTA FALTABA:
    fun editarMascota(antigua: Mascota, nueva: Mascota) = viewModelScope.launch {
        // Preservamos el ID de la antigua para que Room sepa cuál actualizar
        val mascotaConId = nueva.copy(id = antigua.id)
        repository.actualizarMascota(antigua, mascotaConId)
    }

    // ESTA FALTABA:
    fun borrarMascota(mascota: Mascota) = viewModelScope.launch { repository.eliminarMascota(mascota) }

    // --- Consultas ---
    // ESTA FALTABA:
    fun agendarConsulta(consulta: Consulta) = viewModelScope.launch { repository.agregarConsulta(consulta) }

    // ESTA FALTABA:
    fun borrarConsulta(consulta: Consulta) = viewModelScope.launch { repository.eliminarConsulta(consulta) }

    // --- Filtros ---
    fun obtenerClientesFiltrados(): List<Cliente> {
        val query = busquedaCliente.value.lowercase().trim()
        if (query.isEmpty()) return clientes.value
        return clientes.value.filter { it.nombre.lowercase().contains(query) || it.rut.contains(query) }
    }

    fun obtenerMascotasFiltradas(): List<Mascota> {
        val query = busquedaMascota.value.lowercase().trim()
        if (query.isEmpty()) return mascotas.value
        return mascotas.value.filter { it.nombre.lowercase().contains(query) || it.tipo.lowercase().contains(query) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as VeterinariaApplication)
                MainViewModel(application.repository)
            }
        }
    }
}