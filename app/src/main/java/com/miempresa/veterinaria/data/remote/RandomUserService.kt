package com.miempresa.veterinaria.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * RandomUserService - Interface de API para RandomUser.me (Paso 6 - Retrofit)
 *
 * Define los endpoints disponibles para obtener datos aleatorios de usuarios,
 * utilizados para generar fotos de perfil para clientes y veterinarios.
 *
 * API Documentation: https://randomuser.me/documentation
 *
 * Características técnicas:
 * - Usa suspend functions para compatibilidad con Coroutines
 * - Parámetros de query para optimizar respuesta (solo campos necesarios)
 * - Conversión automática JSON -> Kotlin con Gson
 *
 * Uso:
 * ```kotlin
 * val response = RetrofitClient.instancia.obtenerUsuarioAleatorio()
 * val fotoUrl = response.results.firstOrNull()?.picture?.large
 * ```
 *
 * @author Veterinaria App Team
 * @since 1.0
 */
interface RandomUserService {

    /**
     * Obtiene un usuario aleatorio desde la API
     *
     * Endpoint: GET /api/
     *
     * @param incluir Campos a incluir en la respuesta (por defecto solo "picture")
     *                Esto optimiza el ancho de banda al solicitar solo los datos necesarios
     *
     * @return RandomUserResponse con la lista de usuarios (generalmente 1)
     *
     * @throws retrofit2.HttpException si el servidor responde con error HTTP
     * @throws java.io.IOException si hay problemas de conectividad
     */
    @GET("api/")
    suspend fun obtenerUsuarioAleatorio(
        @Query("inc") incluir: String = "picture"
    ): RandomUserResponse

    /**
     * Obtiene múltiples usuarios aleatorios en una sola llamada
     *
     * Endpoint: GET /api/?results=N
     *
     * @param cantidad Número de usuarios a obtener (máximo recomendado: 10)
     * @param incluir Campos a incluir en la respuesta
     *
     * @return RandomUserResponse con la lista de usuarios solicitados
     */
    @GET("api/")
    suspend fun obtenerMultiplesUsuarios(
        @Query("results") cantidad: Int,
        @Query("inc") incluir: String = "picture"
    ): RandomUserResponse
}
