// Archivo: com/miempresa/veterinaria/data/remote/RandomUserService.kt
package com.miempresa.veterinaria.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface RandomUserService {
    @GET("api/")
    suspend fun obtenerUsuarioAleatorio(
        @Query("inc") incluir: String = "picture" // Le pedimos a la API que solo envíe la foto para ahorrar datos
    ): RandomUserResponse
}