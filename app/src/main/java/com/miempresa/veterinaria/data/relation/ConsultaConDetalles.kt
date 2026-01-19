package com.miempresa.veterinaria.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.miempresa.veterinaria.data.entity.ConsultaEntity
import com.miempresa.veterinaria.data.entity.MascotaEntity

data class ConsultaConDetalles(
    @Embedded val consulta: ConsultaEntity,
    @Relation(
        entity = MascotaEntity::class,
        parentColumn = "mascotaId",
        entityColumn = "id"
    )
    val mascotaConCliente: MascotaConCliente
)
