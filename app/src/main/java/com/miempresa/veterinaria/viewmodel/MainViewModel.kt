package com.miempresa.veterinaria.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miempresa.veterinaria.VeterinariaApplication
import com.miempresa.veterinaria.data.VeterinariaRepository
import com.miempresa.veterinaria.data.remote.RetrofitClient
import com.miempresa.veterinaria.model.*
import com.miempresa.veterinaria.util.AppLogger
import kotlinx.coroutines.launch

/**
 * MainViewModel - ViewModel central de la aplicación (Paso 6 - Arquitectura MVVM)
 *
 * Actúa como intermediario entre la capa de UI (Composables) y la capa de datos (Repository),
 * siguiendo el patrón de arquitectura MVVM recomendado por Google.
 *
 * Arquitectura:
 * ```
 * Composables (UI) <-> MainViewModel <-> VeterinariaRepository <-> Room/Retrofit
 * ```
 *
 * Responsabilidades:
 * - Exponer estados observables para la UI mediante mutableStateOf
 * - Procesar acciones del usuario (CRUD, filtros, navegación)
 * - Coordinar operaciones asíncronas con viewModelScope
 * - Sobrevivir cambios de configuración (rotación de pantalla)
 *
 * Características técnicas (Paso 2):
 * - Coroutines con viewModelScope para operaciones asíncronas
 * - Estados reactivos con Compose State
 * - Manejo de errores centralizado (Paso 3)
 * - Logging detallado para debugging
 *
 * @param repository Instancia del repositorio para acceso a datos
 *
 * @author Veterinaria App Team
 * @since 1.0
 */
class MainViewModel(private val repository: VeterinariaRepository) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"

        /**
         * Factory para crear instancias del ViewModel con dependencias
         *
         * Uso:
         * ```kotlin
         * val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
         * ```
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as VeterinariaApplication)
                MainViewModel(application.repository)
            }
        }
    }

    // ============================================================================
    // ESTADOS DE UI - Observables para Composables
    // ============================================================================

    /** Lista de clientes registrados en el sistema */
    val clientes = mutableStateOf<List<Cliente>>(emptyList())

    /** Lista de mascotas registradas con sus dueños */
    val mascotas = mutableStateOf<List<Mascota>>(emptyList())

    /** Lista de consultas agendadas */
    val consultas = mutableStateOf<List<Consulta>>(emptyList())

    /** Lista de veterinarios disponibles (con fotos desde API) */
    val veterinarios = mutableStateOf<List<Veterinario>>(emptyList())

    /** Texto de búsqueda para filtrar clientes */
    var busquedaCliente = mutableStateOf("")

    /** Texto de búsqueda para filtrar mascotas */
    var busquedaMascota = mutableStateOf("")

    /** Estado de carga para mostrar indicadores en UI */
    val estaCargando = mutableStateOf(false)

    /** Mensaje de error para mostrar al usuario (null si no hay error) */
    val mensajeError = mutableStateOf<String?>(null)

    // ============================================================================
    // INICIALIZACIÓN - Carga de datos al crear el ViewModel
    // ============================================================================

    init {
        AppLogger.i(TAG, "Inicializando MainViewModel - configurando collectors de Flow")

        // Collector para Flow de clientes (reactivo a cambios en BD)
        viewModelScope.launch {
            repository.clientes.collect { listaClientes ->
                clientes.value = listaClientes
                AppLogger.d(TAG, "Clientes actualizados: ${listaClientes.size} registros")
            }
        }

        // Collector para Flow de mascotas
        viewModelScope.launch {
            repository.mascotas.collect { listaMascotas ->
                mascotas.value = listaMascotas
                AppLogger.d(TAG, "Mascotas actualizadas: ${listaMascotas.size} registros")
            }
        }

        // Collector para Flow de consultas
        viewModelScope.launch {
            repository.consultas.collect { listaConsultas ->
                consultas.value = listaConsultas
                AppLogger.d(TAG, "Consultas actualizadas: ${listaConsultas.size} registros")
            }
        }

        // Carga inicial de veterinarios desde API remota (Paso 2)
        cargarVeterinarios()
    }

    /**
     * Carga los veterinarios con fotos desde la API remota
     *
     * Características (Paso 2 y 3):
     * - Ejecuta en viewModelScope (Coroutines)
     * - Maneja estados de carga para UI
     * - Captura y registra errores
     */
    private fun cargarVeterinarios() {
        viewModelScope.launch {
            try {
                estaCargando.value = true
                AppLogger.i(TAG, "Iniciando carga de veterinarios desde API")

                veterinarios.value = repository.obtenerVeterinariosConFotos()

                AppLogger.i(TAG, "Veterinarios cargados exitosamente: ${veterinarios.value.size}")
            } catch (e: Exception) {
                // Paso 3: Manejo de errores con logging
                AppLogger.e(TAG, "Error al cargar veterinarios", e)
                mensajeError.value = "Error al cargar veterinarios: ${e.message}"

                // Fallback: Usar lista vacía o datos locales
                veterinarios.value = repository.obtenerVeterinarios()
            } finally {
                estaCargando.value = false
            }
        }
    }

    /**
     * Recarga los veterinarios (útil para retry después de error)
     */
    fun recargarVeterinarios() {
        mensajeError.value = null
        cargarVeterinarios()
    }

    /**
     * Limpia el mensaje de error actual
     */
    fun limpiarError() {
        mensajeError.value = null
    }

    // ============================================================================
    // OPERACIONES CRUD - CLIENTES (Paso 1 - Flujo funcional principal)
    // ============================================================================

    /**
     * Registra un nuevo cliente en el sistema
     *
     * @param cliente Cliente a registrar
     */
    fun registrarCliente(cliente: Cliente) = viewModelScope.launch {
        try {
            repository.agregarCliente(cliente)
            AppLogger.i(TAG, "Cliente registrado: ${cliente.nombre}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al registrar cliente", e)
            mensajeError.value = "Error al registrar cliente: ${e.message}"
        }
    }

    /**
     * Registra un cliente con foto obtenida desde API externa (Paso 2 y 6)
     *
     * Flujo:
     * 1. Llama a RandomUser API para obtener foto
     * 2. Crea objeto Cliente con la URL obtenida
     * 3. Guarda en base de datos local
     *
     * Manejo de errores (Paso 3):
     * - Si falla la API, registra el cliente sin foto
     * - Logging detallado en cada paso
     *
     * @param nombre Nombre completo del cliente
     * @param correo Email del cliente
     * @param telefono Teléfono de contacto
     * @param rut RUT chileno (identificador único)
     */
    fun registrarClienteConFoto(
        nombre: String,
        correo: String,
        telefono: String,
        rut: String
    ) = viewModelScope.launch {
        val startTime = System.currentTimeMillis()

        try {
            estaCargando.value = true
            AppLogger.i(TAG, "Registrando cliente con foto: $nombre")

            // Paso 6: Uso de Retrofit para obtener foto desde API
            val response = RetrofitClient.instancia.obtenerUsuarioAleatorio()
            val urlImagen = response.results.firstOrNull()?.picture?.large

            // Crear cliente con la URL obtenida
            val nuevoCliente = Cliente(
                nombre = nombre,
                correo = correo,
                telefono = telefono,
                rut = rut,
                fotoUri = urlImagen?.let { android.net.Uri.parse(it) }
            )

            // Guardar en base de datos
            repository.agregarCliente(nuevoCliente)

            val duration = System.currentTimeMillis() - startTime
            AppLogger.performance(TAG, "Registro cliente con foto", duration)
            AppLogger.i(TAG, "Cliente registrado exitosamente con foto: $urlImagen")

        } catch (e: Exception) {
            // Paso 3: Manejo de errores con fallback
            AppLogger.e(TAG, "Error al obtener foto para cliente, guardando sin foto", e)

            // Fallback: Registrar sin foto si falla la API
            try {
                val clienteSinFoto = Cliente(nombre, correo, telefono, rut, null)
                repository.agregarCliente(clienteSinFoto)
                AppLogger.w(TAG, "Cliente registrado sin foto (fallback): $nombre")
            } catch (dbError: Exception) {
                AppLogger.e(TAG, "Error crítico al guardar cliente", dbError)
                mensajeError.value = "Error al guardar cliente: ${dbError.message}"
            }
        } finally {
            estaCargando.value = false
        }
    }

    /**
     * Elimina un cliente del sistema
     *
     * @param cliente Cliente a eliminar
     */
    fun borrarCliente(cliente: Cliente) = viewModelScope.launch {
        try {
            repository.eliminarCliente(cliente)
            AppLogger.i(TAG, "Cliente eliminado: ${cliente.nombre}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al eliminar cliente", e)
            mensajeError.value = "Error al eliminar cliente: ${e.message}"
        }
    }

    /**
     * Actualiza los datos de un cliente existente
     *
     * @param antiguo Cliente con datos originales
     * @param nuevo Cliente con datos actualizados
     */
    fun editarCliente(antiguo: Cliente, nuevo: Cliente) = viewModelScope.launch {
        try {
            repository.actualizarCliente(antiguo, nuevo)
            AppLogger.i(TAG, "Cliente actualizado: ${antiguo.nombre} -> ${nuevo.nombre}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al actualizar cliente", e)
            mensajeError.value = "Error al actualizar cliente: ${e.message}"
        }
    }

    // ============================================================================
    // OPERACIONES CRUD - MASCOTAS
    // ============================================================================

    /**
     * Registra una nueva mascota en el sistema
     *
     * @param mascota Mascota a registrar
     */
    fun registrarMascota(mascota: Mascota) = viewModelScope.launch {
        try {
            repository.agregarMascota(mascota)
            AppLogger.i(TAG, "Mascota registrada: ${mascota.nombre}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al registrar mascota", e)
            mensajeError.value = "Error al registrar mascota: ${e.message}"
        }
    }

    /**
     * Actualiza los datos de una mascota existente
     *
     * @param antigua Mascota con datos originales (necesario para el ID)
     * @param nueva Mascota con datos actualizados
     */
    fun editarMascota(antigua: Mascota, nueva: Mascota) = viewModelScope.launch {
        try {
            // Preservar el ID original para que Room sepa cuál actualizar
            val mascotaConId = nueva.copy(id = antigua.id)
            repository.actualizarMascota(antigua, mascotaConId)
            AppLogger.i(TAG, "Mascota actualizada: ${antigua.nombre} -> ${nueva.nombre}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al actualizar mascota", e)
            mensajeError.value = "Error al actualizar mascota: ${e.message}"
        }
    }

    /**
     * Elimina una mascota del sistema
     *
     * @param mascota Mascota a eliminar
     */
    fun borrarMascota(mascota: Mascota) = viewModelScope.launch {
        try {
            repository.eliminarMascota(mascota)
            AppLogger.i(TAG, "Mascota eliminada: ${mascota.nombre}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al eliminar mascota", e)
            mensajeError.value = "Error al eliminar mascota: ${e.message}"
        }
    }

    // ============================================================================
    // OPERACIONES CRUD - CONSULTAS
    // ============================================================================

    /**
     * Agenda una nueva consulta veterinaria (Paso 2 - Coroutines)
     *
     * @param consulta Consulta a agendar
     */
    fun agendarConsulta(consulta: Consulta) = viewModelScope.launch {
        try {
            repository.agregarConsulta(consulta)
            AppLogger.i(TAG, "Consulta agendada: ${consulta.mascota.nombre} con ${consulta.veterinario.nombre}")
        } catch (e: Exception) {
            // Paso 3: Manejo de errores con logging
            AppLogger.e(TAG, "Error al agendar consulta", e)
            mensajeError.value = "Error al agendar consulta: ${e.message}"
        }
    }

    /**
     * Elimina una consulta del sistema
     *
     * @param consulta Consulta a eliminar
     */
    fun borrarConsulta(consulta: Consulta) = viewModelScope.launch {
        try {
            repository.eliminarConsulta(consulta)
            AppLogger.i(TAG, "Consulta eliminada: ID ${consulta.id}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al eliminar consulta", e)
            mensajeError.value = "Error al eliminar consulta: ${e.message}"
        }
    }

    // ============================================================================
    // FILTROS - Búsqueda en listas
    // ============================================================================

    /**
     * Obtiene la lista de clientes filtrada según el texto de búsqueda
     *
     * Criterios de búsqueda:
     * - Nombre del cliente (parcial, case-insensitive)
     * - RUT del cliente (parcial)
     *
     * @return Lista filtrada de clientes
     */
    fun obtenerClientesFiltrados(): List<Cliente> {
        val query = busquedaCliente.value.lowercase().trim()
        if (query.isEmpty()) return clientes.value

        return clientes.value.filter { cliente ->
            cliente.nombre.lowercase().contains(query) ||
                    cliente.rut.contains(query)
        }
    }

    /**
     * Obtiene la lista de mascotas filtrada según el texto de búsqueda
     *
     * Criterios de búsqueda:
     * - Nombre de la mascota (parcial, case-insensitive)
     * - Tipo de mascota (parcial, case-insensitive)
     *
     * @return Lista filtrada de mascotas
     */
    fun obtenerMascotasFiltradas(): List<Mascota> {
        val query = busquedaMascota.value.lowercase().trim()
        if (query.isEmpty()) return mascotas.value

        return mascotas.value.filter { mascota ->
            mascota.nombre.lowercase().contains(query) ||
                    mascota.tipo.lowercase().contains(query)
        }
    }

    // ============================================================================
    // DEBUGGING - Métodos para testing (Paso 3)
    // ============================================================================

    /**
     * Activa/desactiva la simulación de errores de red para testing
     *
     * Cuando está activo, las llamadas a la API fallarán intencionalmente
     * para verificar el comportamiento de la aplicación ante errores.
     *
     * @param activar true para activar simulación, false para desactivar
     */
    fun configurarSimulacionErrorRed(activar: Boolean) {
        VeterinariaRepository.simularErrorRed = activar
        AppLogger.w(TAG, "Simulación de error de red: ${if (activar) "ACTIVADA" else "DESACTIVADA"}")
    }
}
