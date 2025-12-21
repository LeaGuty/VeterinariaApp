package com.miempresa.veterinaria.data

import com.miempresa.veterinaria.model.Cliente
import com.miempresa.veterinaria.model.Consulta
import com.miempresa.veterinaria.model.Mascota
import com.miempresa.veterinaria.model.Veterinario
import kotlinx.coroutines.delay

class VeterinariaRepository {

    // Listas mutables para simular una base de datos en memoria
    private val clientes = mutableListOf<Cliente>()
    private val mascotas = mutableListOf<Mascota>()
    private val consultas = mutableListOf<Consulta>()

    // Datos iniciales de prueba (para no empezar con la app vacía)
    init {
        val cliente1 = Cliente("Juan Pérez", "juan@mail.com", "+56912345678", "12.345.678-5")
        val cliente2 = Cliente("Maria Silva", "maria@mail.com", "+56987654321", "9.876.543-3")
        clientes.add(cliente1)
        clientes.add(cliente2)

        val mascota1 = Mascota("Firulais", "Perro", "Mestizo", 3, cliente1)
        val mascota2 = Mascota("Mishina", "Gato", "Siames", 2, cliente2)
        mascotas.add(mascota1)
        mascotas.add(mascota2)
    }

    // Funciones para obtener datos (simulamos un retraso de red con 'delay')
    suspend fun obtenerClientes(): List<Cliente> {
        delay(500) // Simula que tarda 1 segundo en cargar
        return clientes.toList()
    }

    suspend fun obtenerMascotas(): List<Mascota> {
        delay(500)
        return mascotas.toList()
    }

    suspend fun obtenerConsultas(): List<Consulta> {
        delay(500)
        return consultas.toList()
    }

    fun obtenerVeterinarios(): List<Veterinario> {
        return listOf(
            Veterinario("Dr. Simi", "General"),
            Veterinario("Dra. Polo", "Cirugía")
        )
    }

    // Funciones para agregar datos
    fun agregarCliente(cliente: Cliente) {
        clientes.add(cliente)
    }

    fun agregarMascota(mascota: Mascota) {
        mascotas.add(mascota)
    }

    fun agregarConsulta(consulta: Consulta) {
        consultas.add(consulta)
    }

    fun eliminarCliente(cliente: Cliente) {
        clientes.remove(cliente)
        // También borramos las mascotas de ese cliente para mantener consistencia
        mascotas.removeAll { it.dueno == cliente }
    }

    fun eliminarMascota(mascota: Mascota) {
        mascotas.remove(mascota)
    }

    fun eliminarConsulta(consulta: Consulta) {
        consultas.remove(consulta)
    }

    fun actualizarCliente(clienteAntiguo: Cliente, clienteNuevo: Cliente) {
        val index = clientes.indexOf(clienteAntiguo)
        if (index != -1) {
            clientes[index] = clienteNuevo
            // También actualizamos las mascotas asociadas a este cliente (si cambió nombre/foto)
            mascotas.replaceAll { if (it.dueno == clienteAntiguo) it.copy(dueno = clienteNuevo) else it }
        }
    }

    fun actualizarMascota(mascotaAntigua: Mascota, mascotaNueva: Mascota) {
        val index = mascotas.indexOf(mascotaAntigua)
        if (index != -1) {
            mascotas[index] = mascotaNueva
        }
    }
}