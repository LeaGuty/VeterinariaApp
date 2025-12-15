package com.miempresa.veterinaria.model

import android.net.Uri

data class Mascota(
    val nombre: String,
    val tipo: String, // Perro, Gato, etc.
    val raza: String,
    val edad: Int,
    val dueno: Cliente,
    val fotoUri: Uri? = null
) {
    // Pequeña lógica para saber si es cachorro
    fun esCachorro(): Boolean = edad < 1
}