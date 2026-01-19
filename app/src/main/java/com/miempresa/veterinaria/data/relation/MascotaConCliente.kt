package com.miempresa.veterinaria.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.miempresa.veterinaria.data.entity.ClienteEntity
import com.miempresa.veterinaria.data.entity.MascotaEntity

data class MascotaConCliente(
    @Embedded val mascota: MascotaEntity,
    @Relation(
        parentColumn = "duenoRut", // Columna en MascotaEntity
        entityColumn = "rut"       // Columna en ClienteEntity
    )
    val cliente: ClienteEntity
)