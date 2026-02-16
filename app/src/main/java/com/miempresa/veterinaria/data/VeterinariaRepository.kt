package com.miempresa.veterinaria.data

import android.net.Uri
import com.miempresa.veterinaria.data.dao.VeterinariaDao
import com.miempresa.veterinaria.data.entity.*
import com.miempresa.veterinaria.model.Cliente
import com.miempresa.veterinaria.model.Consulta
import com.miempresa.veterinaria.model.Mascota
import com.miempresa.veterinaria.model.TipoConsulta
import com.miempresa.veterinaria.model.Veterinario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.miempresa.veterinaria.data.remote.RetrofitClient

class VeterinariaRepository(private val dao: VeterinariaDao) {

    // --- Clientes ---
    val clientes: Flow<List<Cliente>> = dao.obtenerClientes().map { entities ->
        entities.map { entity ->
            Cliente(entity.nombre, entity.correo, entity.telefono, entity.rut, entity.fotoUri?.let { Uri.parse(it) })
        }
    }

    suspend fun agregarCliente(cliente: Cliente) {
        val entity = ClienteEntity(cliente.rut, cliente.nombre, cliente.correo, cliente.telefono, cliente.fotoUri?.toString())
        dao.insertarCliente(entity)
    }

    suspend fun eliminarCliente(cliente: Cliente) {
        val entity = ClienteEntity(cliente.rut, cliente.nombre, cliente.correo, cliente.telefono, cliente.fotoUri?.toString())
        dao.eliminarCliente(entity)
    }

    suspend fun actualizarCliente(antiguo: Cliente, nuevo: Cliente) {
        if (antiguo.rut != nuevo.rut) eliminarCliente(antiguo)
        agregarCliente(nuevo)
    }

    // --- Mascotas ---
    val mascotas: Flow<List<Mascota>> = dao.obtenerMascotasConCliente().map { relations ->
        relations.map { rel ->
            val duenoModel = Cliente(
                rel.cliente.nombre, rel.cliente.correo, rel.cliente.telefono, rel.cliente.rut, rel.cliente.fotoUri?.let { Uri.parse(it) }
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

    suspend fun agregarMascota(mascota: Mascota) {
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
    }

    suspend fun eliminarMascota(mascota: Mascota) {
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
    }

    suspend fun actualizarMascota(antigua: Mascota, nueva: Mascota) {
        agregarMascota(nueva)
    }

    // --- Consultas ---
    val consultas: Flow<List<Consulta>> = dao.obtenerConsultasConDetalles().map { relations ->
        relations.map { rel ->
            val duenoModel = Cliente(
                rel.mascotaConCliente.cliente.nombre,
                rel.mascotaConCliente.cliente.correo,
                rel.mascotaConCliente.cliente.telefono,
                rel.mascotaConCliente.cliente.rut,
                rel.mascotaConCliente.cliente.fotoUri?.let { Uri.parse(it) }
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
            val vetModel = Veterinario(rel.consulta.veterinarioNombre, "General")

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

    suspend fun agregarConsulta(consulta: Consulta) = withContext(Dispatchers.IO) {
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
    }

    suspend fun eliminarConsulta(consulta: Consulta) = withContext(Dispatchers.IO) {
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
    }

    suspend fun obtenerVeterinariosConFotos(): List<Veterinario> = withContext(Dispatchers.IO) {
        val nombres = listOf("Dr. Simi", "Dra. Polo", "Dr. House", "Dra. Quinn")

        nombres.map { nombre ->
            try {
                // Llamada a la API de RandomUser
                val response = RetrofitClient.instancia.obtenerUsuarioAleatorio()
                val urlImagen = response.results.firstOrNull()?.picture?.large

                // CORRECCIÓN: Pasamos el nombre, la especialidad y la foto.
                // El horario se cargará con su valor por defecto 'generarHorarioPorDefecto()'
                // automáticamente si no lo pasamos, o podemos pasarlo explícitamente.
                Veterinario(
                    nombre = nombre,
                    especialidad = "General",
                    fotoUrl = urlImagen
                )
            } catch (e: Exception) {
                // Logueamos el error para debugging (Paso 3)
                android.util.Log.e("VeterinariaRepo", "Error al obtener foto para $nombre", e)

                // Devolvemos el veterinario con los datos básicos en caso de fallo de red
                Veterinario(
                    nombre = nombre,
                    especialidad = "General",
                    fotoUrl = null
                )
            }
        }
    }
    fun obtenerVeterinarios(): List<Veterinario> {
        return listOf(Veterinario("Dr. Simi", "General"), Veterinario("Dra. Polo", "Cirugía"))
    }
}
