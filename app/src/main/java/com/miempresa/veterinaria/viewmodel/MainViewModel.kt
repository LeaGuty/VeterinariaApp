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
import android.util.Log

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

        // CAMBIO AQUÍ: Lanzamos una corrutina para obtener los veterinarios con fotos
        viewModelScope.launch {
            try {
                veterinarios.value = repository.obtenerVeterinariosConFotos()
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error cargando veterinarios", e)
            }
        }
    }

    // --- Clientes ---
    fun registrarCliente(cliente: Cliente) = viewModelScope.launch { repository.agregarCliente(cliente) }

    fun registrarClienteConFoto(nombre: String, correo: String, telefono: String, rut: String) = viewModelScope.launch {
        try {
            // 1. Obtenemos la URL de la imagen de forma asíncrona
            val response = com.miempresa.veterinaria.data.remote.RetrofitClient.instancia.obtenerUsuarioAleatorio()
            val urlImagen = response.results.firstOrNull()?.picture?.large

            // 2. Creamos el objeto Cliente con la URL obtenida
            val nuevoCliente = Cliente(
                nombre = nombre,
                correo = correo,
                telefono = telefono,
                rut = rut,
                fotoUri = urlImagen?.let { android.net.Uri.parse(it) }
            )

            // 3. Guardamos en el repositorio (hilo IO)
            repository.agregarCliente(nuevoCliente)
            Log.d("VeterinariApp", "Cliente registrado con foto: $urlImagen")
        } catch (e: Exception) {
            Log.e("VeterinariApp", "Error al registrar cliente con foto", e)
            // Fallback: registrar sin foto si falla la API
            repository.agregarCliente(Cliente(nombre, correo, telefono, rut, null))
        }
    }
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
    fun agendarConsulta(consulta: Consulta) = viewModelScope.launch {
        try {
            repository.agregarConsulta(consulta)
            Log.d("VeterinariApp", "Consulta agendada con éxito para: ${consulta.mascota.nombre}")
        } catch (e: Exception) {
            Log.e("VeterinariApp", "Error al agendar consulta", e)
            // Aquí podríamos actualizar un estado de UI para mostrar un mensaje de error al usuario
        }
    }

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