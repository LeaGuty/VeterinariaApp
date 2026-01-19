package com.miempresa.veterinaria

import android.app.Application
import com.miempresa.veterinaria.data.VeterinariaDatabase
import com.miempresa.veterinaria.data.VeterinariaRepository

class VeterinariaApplication : Application() {
    // Inicializamos la base de datos de forma perezosa (solo cuando se necesite)
    val database by lazy { VeterinariaDatabase.getDatabase(this) }

    // Inicializamos el repositorio pasándole el DAO de la base de datos
    val repository by lazy { VeterinariaRepository(database.veterinariaDao()) }
}