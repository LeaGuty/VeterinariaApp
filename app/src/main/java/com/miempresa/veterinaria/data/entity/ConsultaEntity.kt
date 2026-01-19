package com.miempresa.veterinaria.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "consultas",
    foreignKeys = [
        ForeignKey(
            entity = MascotaEntity::class,
            parentColumns = ["id"],
            childColumns = ["mascotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ConsultaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mascotaId: Int, // Conecta con el ID de la mascota
    val veterinarioNombre: String,
    val fecha: LocalDate,
    val hora: LocalTime,
    val motivo: String,
    val observaciones: String = ""
)
