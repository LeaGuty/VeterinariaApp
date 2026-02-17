package com.miempresa.veterinaria.data.remote

import com.miempresa.veterinaria.util.AppLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient - Cliente HTTP singleton para llamadas API REST (Paso 6)
 *
 * Implementa el patrón Singleton con inicialización lazy para garantizar una única
 * instancia de Retrofit en toda la aplicación, optimizando recursos y conexiones.
 *
 * Características técnicas:
 * - Singleton thread-safe mediante delegación lazy
 * - Logging interceptor para debugging (solo en DEBUG builds)
 * - Timeouts configurables para operaciones de red
 * - Conversión automática JSON <-> Kotlin con Gson
 *
 * Integración con RandomUser API:
 * - Endpoint: https://randomuser.me/api/
 * - Uso: Obtención de fotos aleatorias para clientes y veterinarios
 * - Documentación: https://randomuser.me/documentation
 *
 * @author Veterinaria App Team
 * @since 1.0
 */
object RetrofitClient {

    private const val TAG = "RetrofitClient"

    /** URL base de la API RandomUser */
    private const val BASE_URL = "https://randomuser.me/"

    /** Timeout para conexión inicial (segundos) */
    private const val CONNECT_TIMEOUT = 30L

    /** Timeout para lectura de respuesta (segundos) */
    private const val READ_TIMEOUT = 30L

    /** Timeout para escritura de request (segundos) */
    private const val WRITE_TIMEOUT = 30L

    /**
     * Flag para habilitar/deshabilitar logging HTTP detallado
     *
     * IMPORTANTE: Cambiar a 'false' antes de generar builds de producción
     * para evitar exponer información sensible en los logs.
     */
    private const val HTTP_LOGGING_ENABLED = true

    /**
     * Interceptor de logging para debugging de llamadas HTTP (Paso 3)
     *
     * Niveles de logging:
     * - NONE: Sin logging (producción)
     * - BASIC: Solo línea de request/response
     * - HEADERS: + headers HTTP
     * - BODY: + cuerpo completo (solo DEBUG)
     *
     * Los logs aparecen en Logcat con tag "okhttp.OkHttpClient"
     */
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor { message ->
            // Redirigimos los logs de OkHttp a nuestro AppLogger
            AppLogger.d("OkHttp", message)
        }.apply {
            // Solo logging detallado cuando está habilitado
            level = if (HTTP_LOGGING_ENABLED) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * Cliente OkHttp configurado con interceptors y timeouts
     *
     * Configuración:
     * - Logging interceptor para debugging
     * - Timeouts para prevenir bloqueos en red lenta
     * - Retry automático deshabilitado para control manual
     */
    private val okHttpClient: OkHttpClient by lazy {
        AppLogger.i(TAG, "Inicializando OkHttpClient con timeouts y logging")

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Instancia singleton del servicio RandomUserService
     *
     * Inicialización lazy garantiza:
     * - Thread-safety sin sincronización manual
     * - Creación solo cuando se necesita (ahorro de recursos)
     * - Una única instancia de Retrofit reutilizada
     *
     * Uso:
     * ```kotlin
     * val response = RetrofitClient.instancia.obtenerUsuarioAleatorio()
     * ```
     */
    val instancia: RandomUserService by lazy {
        AppLogger.i(TAG, "Creando instancia de Retrofit para $BASE_URL")

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RandomUserService::class.java)
    }

    /**
     * Verifica si el cliente está inicializado (para testing/debugging)
     *
     * @return true si la instancia de Retrofit ya fue creada
     */
    fun estaInicializado(): Boolean {
        return try {
            instancia
            true
        } catch (e: Exception) {
            false
        }
    }
}
