package com.miempresa.veterinaria.model

import java.time.LocalDate
import java.time.LocalTime

data class Veterinario(
    val nombre: String,
    val especialidad: String,
    val horario: List<LocalTime> = generarHorarioPorDefecto()
) {
    companion object {
        fun generarHorarioPorDefecto(): List<LocalTime> {
            val horario = mutableListOf<LocalTime>()
            var hora = LocalTime.of(9, 0) // Empieza a las 9 AM
            while (hora.isBefore(LocalTime.of(17, 0))) { // Termina a las 5 PM
                horario.add(hora)
                hora = hora.plusHours(1)
            }
            return horario
        }
    }

    // Filtra las horas que ya están ocupadas
    fun obtenerHorasDisponibles(fecha: LocalDate, todasLasConsultas: List<Consulta>): List<LocalTime> {
        val citasDelDia = todasLasConsultas.filter {
            it.veterinario.nombre == this.nombre && it.fecha == fecha
        }
        val horasOcupadas = citasDelDia.map { it.hora }
        return horario.filter { it !in horasOcupadas }
    }
}