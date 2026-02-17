package com.miempresa.veterinaria.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Veterinario - Modelo de dominio para profesionales veterinarios
 *
 * Representa a un veterinario del sistema con su información profesional,
 * horario de atención y foto de perfil (obtenida desde API externa).
 *
 * Uso en la aplicación:
 * - Selección de veterinario al agendar consultas
 * - Visualización en listas con foto (Glide)
 * - Validación de disponibilidad horaria
 *
 * @property nombre Nombre completo del veterinario (ej: "Dr. Simi")
 * @property especialidad Área de especialización (ej: "General", "Cirugía")
 * @property horario Lista de horas de atención disponibles
 * @property fotoUrl URL de la foto de perfil (desde RandomUser API)
 *
 * @author Veterinaria App Team
 * @since 1.0
 */
data class Veterinario(
    val nombre: String,
    val especialidad: String,
    val horario: List<LocalTime> = generarHorarioPorDefecto(),
    val fotoUrl: String? = null
) {
    companion object {
        /**
         * Genera el horario de atención por defecto
         *
         * Horario estándar: 9:00 AM - 5:00 PM, intervalos de 1 hora
         *
         * @return Lista de LocalTime con las horas de atención disponibles
         */
        fun generarHorarioPorDefecto(): List<LocalTime> {
            val horario = mutableListOf<LocalTime>()
            var hora = LocalTime.of(9, 0)  // Inicio: 9:00 AM

            while (hora.isBefore(LocalTime.of(17, 0))) {  // Fin: 5:00 PM
                horario.add(hora)
                hora = hora.plusHours(1)
            }

            return horario
        }
    }

    /**
     * Obtiene las horas disponibles para una fecha específica
     *
     * Filtra el horario del veterinario excluyendo las horas que ya tienen
     * consultas agendadas para la fecha indicada.
     *
     * @param fecha Fecha para la cual consultar disponibilidad
     * @param todasLasConsultas Lista completa de consultas en el sistema
     *
     * @return Lista de horas disponibles (no ocupadas) para la fecha
     *
     * Ejemplo:
     * ```kotlin
     * val horasLibres = veterinario.obtenerHorasDisponibles(
     *     LocalDate.now(),
     *     listaConsultas
     * )
     * // Retorna: [09:00, 10:00, 13:00, 14:00, 16:00] si 11:00, 12:00 y 15:00 están ocupadas
     * ```
     */
    fun obtenerHorasDisponibles(fecha: LocalDate, todasLasConsultas: List<Consulta>): List<LocalTime> {
        // Filtrar consultas del día para este veterinario
        val citasDelDia = todasLasConsultas.filter { consulta ->
            consulta.veterinario.nombre == this.nombre && consulta.fecha == fecha
        }

        // Extraer las horas ocupadas
        val horasOcupadas = citasDelDia.map { it.hora }

        // Retornar solo las horas no ocupadas
        return horario.filter { hora -> hora !in horasOcupadas }
    }
}
