package com.miempresa.veterinaria.data

import android.net.Uri
import com.miempresa.veterinaria.data.dao.VeterinariaDao
import com.miempresa.veterinaria.data.entity.*
import com.miempresa.veterinaria.data.remote.RetrofitClient
import com.miempresa.veterinaria.model.Cliente
import com.miempresa.veterinaria.model.Consulta
import com.miempresa.veterinaria.model.Mascota
import com.miempresa.veterinaria.model.TipoConsulta
import com.miempresa.veterinaria.model.Veterinario
import com.miempresa.veterinaria.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * VeterinariaRepository - Capa de abstracción de datos (Paso 6 - Patrón Repository)
 *
 * Implementa el patrón Repository para proporcionar una API limpia de acceso a datos,
 * abstrayendo las fuentes de datos (Room local + Retrofit remoto) del resto de la aplicación.
 *
 * Arquitectura MVVM:
 * ```
 * UI (Compose) -> ViewModel -> Repository -> DAO (Room) / API (Retrofit)
 * ```
 *
 * Responsabilidades:
 * - Exponer datos mediante Kotlin Flows para observación reactiva
 * - Mapear entidades de BD a modelos de dominio
 * - Coordinar operaciones entre fuentes locales y remotas
 * - Manejar errores de red con fallbacks apropiados
 *
 * Características técnicas (Paso 2):
 * - Operaciones suspendidas para uso con Coroutines
 * - Dispatchers.IO para operaciones de I/O
 * - Try-catch robusto con logging (Paso 3)
 * - Optimización de llamadas API paralelas
 *
 * @param dao Instancia del DAO de Room para acceso a base de datos local
 *
 * @author Veterinaria App Team
 * @since 1.0
 */
class VeterinariaRepository(private val dao: VeterinariaDao) {

    companion object {
        private const val TAG = "VeterinariaRepo"

        /** Flag para simular errores de red (Paso 3 - Testing) */
        var simularErrorRed: Boolean = false

        /** Nombres de veterinarios predefinidos */
        private val NOMBRES_VETERINARIOS = listOf(
            "Dr. Simi",
            "Dra. Polo",
            "Dr. House",
            "Dra. Quinn"
        )
    }

    // ============================================================================
    // CLIENTES - Flujos reactivos y operaciones CRUD
    // ============================================================================

    /**
     * Flow de clientes que emite automáticamente cuando hay cambios en la BD
     *
     * Características:
     * - Observación reactiva mediante Flow
     * - Mapeo automático de Entity a Model
     * - Actualización en tiempo real
     */
    val clientes: Flow<List<Cliente>> = dao.obtenerClientes().map { entities ->
        AppLogger.d(TAG, "Flow clientes emitió ${entities.size} registros")
        entities.map { entity ->
            Cliente(
                nombre = entity.nombre,
                correo = entity.correo,
                telefono = entity.telefono,
                rut = entity.rut,
                fotoUri = entity.fotoUri?.let { Uri.parse(it) }
            )
        }
    }

    /**
     * Agrega un nuevo cliente a la base de datos
     *
     * @param cliente Cliente a agregar
     * @throws Exception si falla la inserción en BD
     */
    suspend fun agregarCliente(cliente: Cliente) = withContext(Dispatchers.IO) {
        try {
            val entity = ClienteEntity(
                rut = cliente.rut,
                nombre = cliente.nombre,
                correo = cliente.correo,
                telefono = cliente.telefono,
                fotoUri = cliente.fotoUri?.toString()
            )
            dao.insertarCliente(entity)
            AppLogger.database("INSERT", "Cliente", "RUT: ${cliente.rut}, Nombre: ${cliente.nombre}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al insertar cliente: ${cliente.rut}", e)
            throw e
        }
    }

    /**
     * Elimina un cliente de la base de datos
     *
     * @param cliente Cliente a eliminar
     */
    suspend fun eliminarCliente(cliente: Cliente) = withContext(Dispatchers.IO) {
        try {
            val entity = ClienteEntity(
                rut = cliente.rut,
                nombre = cliente.nombre,
                correo = cliente.correo,
                telefono = cliente.telefono,
                fotoUri = cliente.fotoUri?.toString()
            )
            dao.eliminarCliente(entity)
            AppLogger.database("DELETE", "Cliente", "RUT: ${cliente.rut}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al eliminar cliente: ${cliente.rut}", e)
            throw e
        }
    }

    /**
     * Actualiza los datos de un cliente existente
     *
     * Nota: Si el RUT cambia, se elimina el antiguo y se crea uno nuevo
     *
     * @param antiguo Cliente con datos anteriores
     * @param nuevo Cliente con datos actualizados
     */
    suspend fun actualizarCliente(antiguo: Cliente, nuevo: Cliente) = withContext(Dispatchers.IO) {
        try {
            if (antiguo.rut != nuevo.rut) {
                eliminarCliente(antiguo)
            }
            agregarCliente(nuevo)
            AppLogger.database("UPDATE", "Cliente", "RUT: ${nuevo.rut}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al actualizar cliente: ${antiguo.rut} -> ${nuevo.rut}", e)
            throw e
        }
    }

    // ============================================================================
    // MASCOTAS - Flujos reactivos y operaciones CRUD
    // ============================================================================

    /**
     * Flow de mascotas con relación a su dueño (Cliente)
     *
     * Utiliza @Transaction en el DAO para garantizar consistencia
     * en la lectura de datos relacionados
     */
    val mascotas: Flow<List<Mascota>> = dao.obtenerMascotasConCliente().map { relations ->
        AppLogger.d(TAG, "Flow mascotas emitió ${relations.size} registros")
        relations.map { rel ->
            val duenoModel = Cliente(
                nombre = rel.cliente.nombre,
                correo = rel.cliente.correo,
                telefono = rel.cliente.telefono,
                rut = rel.cliente.rut,
                fotoUri = rel.cliente.fotoUri?.let { Uri.parse(it) }
            )
            Mascota(
                id = rel.mascota.id,
                nombre = rel.mascota.nombre,
                tipo = rel.mascota.tipo,
                raza = rel.mascota.raza,
                edad = rel.mascota.edad,
                dueno = duenoModel,
                fotoUri = rel.mascota.fotoUri?.let { Uri.parse(it) }
            )
        }
    }

    /**
     * Agrega una nueva mascota a la base de datos
     *
     * @param mascota Mascota a agregar
     */
    suspend fun agregarMascota(mascota: Mascota) = withContext(Dispatchers.IO) {
        try {
            val entity = MascotaEntity(
                id = mascota.id,
                nombre = mascota.nombre,
                tipo = mascota.tipo,
                raza = mascota.raza,
                edad = mascota.edad,
                duenoRut = mascota.dueno.rut,
                fotoUri = mascota.fotoUri?.toString()
            )
            dao.insertarMascota(entity)
            AppLogger.database("INSERT", "Mascota", "ID: ${mascota.id}, Nombre: ${mascota.nombre}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al insertar mascota: ${mascota.nombre}", e)
            throw e
        }
    }

    /**
     * Elimina una mascota de la base de datos
     *
     * @param mascota Mascota a eliminar
     */
    suspend fun eliminarMascota(mascota: Mascota) = withContext(Dispatchers.IO) {
        try {
            val entity = MascotaEntity(
                id = mascota.id,
                nombre = mascota.nombre,
                tipo = mascota.tipo,
                raza = mascota.raza,
                edad = mascota.edad,
                duenoRut = mascota.dueno.rut,
                fotoUri = mascota.fotoUri?.toString()
            )
            dao.eliminarMascota(entity)
            AppLogger.database("DELETE", "Mascota", "ID: ${mascota.id}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al eliminar mascota: ${mascota.id}", e)
            throw e
        }
    }

    /**
     * Actualiza los datos de una mascota existente
     *
     * @param antigua Mascota con datos anteriores (no usado, mantenido por compatibilidad)
     * @param nueva Mascota con datos actualizados
     */
    suspend fun actualizarMascota(antigua: Mascota, nueva: Mascota) = withContext(Dispatchers.IO) {
        try {
            agregarMascota(nueva)
            AppLogger.database("UPDATE", "Mascota", "ID: ${nueva.id}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al actualizar mascota: ${nueva.id}", e)
            throw e
        }
    }

    // ============================================================================
    // CONSULTAS - Flujos reactivos y operaciones CRUD
    // ============================================================================

    /**
     * Flow de consultas con relaciones completas (Mascota + Cliente + Veterinario)
     *
     * Utiliza múltiples @Transaction para cargar toda la información relacionada
     */
    val consultas: Flow<List<Consulta>> = dao.obtenerConsultasConDetalles().map { relations ->
        AppLogger.d(TAG, "Flow consultas emitió ${relations.size} registros")
        relations.map { rel ->
            val duenoModel = Cliente(
                nombre = rel.mascotaConCliente.cliente.nombre,
                correo = rel.mascotaConCliente.cliente.correo,
                telefono = rel.mascotaConCliente.cliente.telefono,
                rut = rel.mascotaConCliente.cliente.rut,
                fotoUri = rel.mascotaConCliente.cliente.fotoUri?.let { Uri.parse(it) }
            )
            val mascotaModel = Mascota(
                id = rel.mascotaConCliente.mascota.id,
                nombre = rel.mascotaConCliente.mascota.nombre,
                tipo = rel.mascotaConCliente.mascota.tipo,
                raza = rel.mascotaConCliente.mascota.raza,
                edad = rel.mascotaConCliente.mascota.edad,
                dueno = duenoModel,
                fotoUri = rel.mascotaConCliente.mascota.fotoUri?.let { Uri.parse(it) }
            )
            val vetModel = Veterinario(
                nombre = rel.consulta.veterinarioNombre,
                especialidad = "General"
            )

            Consulta(
                id = rel.consulta.id,
                mascota = mascotaModel,
                veterinario = vetModel,
                fecha = rel.consulta.fecha,
                hora = rel.consulta.hora,
                tipoConsulta = TipoConsulta.GENERAL,
                motivoConsulta = rel.consulta.motivo,
                observaciones = rel.consulta.observaciones
            )
        }
    }

    /**
     * Agenda una nueva consulta en la base de datos (Paso 2 - Coroutines)
     *
     * Ejecuta la operación en Dispatchers.IO para no bloquear el hilo principal
     *
     * @param consulta Consulta a agendar
     */
    suspend fun agregarConsulta(consulta: Consulta) = withContext(Dispatchers.IO) {
        try {
            val entity = ConsultaEntity(
                id = consulta.id,
                mascotaId = consulta.mascota.id,
                veterinarioNombre = consulta.veterinario.nombre,
                fecha = consulta.fecha,
                hora = consulta.hora,
                motivo = consulta.motivoConsulta,
                observaciones = consulta.observaciones
            )
            dao.insertarConsulta(entity)
            AppLogger.database("INSERT", "Consulta",
                "ID: ${consulta.id}, Mascota: ${consulta.mascota.nombre}, Vet: ${consulta.veterinario.nombre}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al insertar consulta: ${consulta.id}", e)
            throw e
        }
    }

    /**
     * Elimina una consulta de la base de datos
     *
     * @param consulta Consulta a eliminar
     */
    suspend fun eliminarConsulta(consulta: Consulta) = withContext(Dispatchers.IO) {
        try {
            val entity = ConsultaEntity(
                id = consulta.id,
                mascotaId = consulta.mascota.id,
                veterinarioNombre = consulta.veterinario.nombre,
                fecha = consulta.fecha,
                hora = consulta.hora,
                motivo = consulta.motivoConsulta,
                observaciones = consulta.observaciones
            )
            dao.eliminarConsulta(entity)
            AppLogger.database("DELETE", "Consulta", "ID: ${consulta.id}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al eliminar consulta: ${consulta.id}", e)
            throw e
        }
    }

    // ============================================================================
    // VETERINARIOS - Carga desde API remota (Paso 2 y 6)
    // ============================================================================

    /**
     * Obtiene la lista de veterinarios con fotos desde la API remota
     *
     * OPTIMIZACIÓN (Paso 2):
     * - Usa coroutineScope + async para llamadas paralelas
     * - Reduce de N llamadas secuenciales a N llamadas paralelas
     * - Tiempo total = max(tiempo_llamada) en vez de sum(tiempos)
     *
     * MANEJO DE ERRORES (Paso 3):
     * - Try-catch individual por cada llamada
     * - Fallback a datos sin foto si falla la API
     * - Logging detallado de errores
     * - Soporte para simulación de errores en testing
     *
     * @return Lista de Veterinarios con fotos (o sin ellas si falla la API)
     */
    suspend fun obtenerVeterinariosConFotos(): List<Veterinario> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        AppLogger.i(TAG, "Iniciando carga de ${NOMBRES_VETERINARIOS.size} veterinarios con fotos")

        // Paso 3: Simular error de red para testing
        if (simularErrorRed) {
            AppLogger.w(TAG, "Simulación de error de red activada")
            throw java.io.IOException("Error de red simulado para testing")
        }

        try {
            // Paso 2: Llamadas paralelas con coroutineScope + async
            // Esto optimiza el tiempo de carga al ejecutar todas las llamadas simultáneamente
            val veterinarios = coroutineScope {
                NOMBRES_VETERINARIOS.map { nombre ->
                    async {
                        obtenerVeterinarioConFoto(nombre)
                    }
                }.awaitAll()
            }

            val duration = System.currentTimeMillis() - startTime
            AppLogger.performance(TAG, "Carga de veterinarios", duration)
            AppLogger.network("RandomUser API", true, "Cargados ${veterinarios.size} veterinarios")

            veterinarios
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error general al obtener veterinarios con fotos", e)
            // Fallback: Devolver veterinarios sin fotos
            NOMBRES_VETERINARIOS.map { nombre ->
                Veterinario(nombre = nombre, especialidad = "General", fotoUrl = null)
            }
        }
    }

    /**
     * Obtiene un veterinario individual con foto desde la API
     *
     * @param nombre Nombre del veterinario
     * @return Veterinario con foto si la API responde, sin foto si falla
     */
    private suspend fun obtenerVeterinarioConFoto(nombre: String): Veterinario {
        return try {
            val response = RetrofitClient.instancia.obtenerUsuarioAleatorio()
            val urlImagen = response.results.firstOrNull()?.picture?.large

            AppLogger.d(TAG, "Foto obtenida para $nombre: ${urlImagen != null}")

            Veterinario(
                nombre = nombre,
                especialidad = "General",
                fotoUrl = urlImagen
            )
        } catch (e: Exception) {
            // Paso 3: Logging de errores individuales
            AppLogger.e(TAG, "Error al obtener foto para veterinario: $nombre", e)
            AppLogger.network("RandomUser API", false, "Error para $nombre: ${e.message}")

            // Fallback: Devolver veterinario sin foto
            Veterinario(
                nombre = nombre,
                especialidad = "General",
                fotoUrl = null
            )
        }
    }

    /**
     * Obtiene lista simple de veterinarios sin fotos (para uso offline)
     *
     * @return Lista de veterinarios con datos básicos
     */
    fun obtenerVeterinarios(): List<Veterinario> {
        return listOf(
            Veterinario("Dr. Simi", "General"),
            Veterinario("Dra. Polo", "Cirugía")
        )
    }
}
