package com.miempresa.veterinaria.model

import android.net.Uri

data class Mascota(
    val id: Int = 0, // Nuevo campo ID (0 = nuevo, >0 = existente en BD)
    val nombre: String,
    val tipo: String,
    val raza: String,
    val edad: Int,
    val dueno: Cliente,
    val fotoUri: Uri? = null
) {
    fun esCachorro(): Boolean = edad < 1
}