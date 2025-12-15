package com.miempresa.veterinaria.model

import android.net.Uri

data class Cliente(
    val nombre: String,
    val correo: String,
    val telefono: String,
    val rut: String,
    val fotoUri: Uri? = null // Puede ser nulo si no tiene foto
)