package com.miempresa.veterinaria.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val fotoUri: String? = null
)