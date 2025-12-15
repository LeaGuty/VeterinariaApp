package com.miempresa.veterinaria.model

import java.time.LocalDate
import java.time.LocalTime

data class Consulta(
    val mascota: Mascota,
    val veterinario: Veterinario,
    val fecha: LocalDate,
    val hora: LocalTime,
    val tipoConsulta: TipoConsulta,
    val motivoConsulta: String,
    var observaciones: String = ""
)

enum class TipoConsulta(val descripcion: String) {
    GENERAL("Consulta General"),
    URGENCIA("Urgencia"),
    CONTROL("Control"),
    VACUNACION("Vacunación"),
    CIRUGIA("Cirugía")
}