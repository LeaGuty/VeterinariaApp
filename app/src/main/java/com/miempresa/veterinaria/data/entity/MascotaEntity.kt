package com.miempresa.veterinaria.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "mascotas",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["rut"],
            childColumns = ["duenoRut"],
            onDelete = ForeignKey.CASCADE // Si borras al dueño, se borran sus mascotas automáticamente
        )
    ]
)
data class MascotaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val tipo: String,
    val raza: String,
    val edad: Int,
    val duenoRut: String, // Clave foránea que conecta con ClienteEntity
    val fotoUri: String? = null
)