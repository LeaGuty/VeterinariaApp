package com.miempresa.veterinaria.util

import android.util.Log

/**
 * AppLogger - Utilidad centralizada para logging y debugging (Paso 3)
 *
 * Proporciona un sistema de logging consistente y profesional para toda la aplicación.
 * Características:
 * - Logging categorizado por niveles (DEBUG, INFO, WARNING, ERROR)
 * - Control manual de habilitación para producción
 * - Formato consistente con tags identificables
 * - Soporte para excepciones con stack trace
 *
 * Uso:
 * ```kotlin
 * AppLogger.d("MiClase", "Mensaje de debug")
 * AppLogger.e("MiClase", "Error crítico", exception)
 * ```
 *
 * @author Veterinaria App Team
 * @since 1.0
 */
object AppLogger {

    /** Tag principal para identificar logs de la app en Logcat */
    private const val APP_TAG = "VeterinariaApp"

    /**
     * Indica si el logging está habilitado
     *
     * IMPORTANTE: Cambiar a 'false' antes de generar builds de producción
     * para evitar exponer información sensible en los logs.
     *
     * Alternativa: Usar BuildConfig.DEBUG después de compilar el proyecto
     */
    private const val LOGGING_ENABLED = true

    /**
     * Log de nivel DEBUG - Para información de desarrollo
     *
     * Uso: Seguimiento de flujo, valores de variables, estados intermedios
     *
     * @param tag Identificador del componente (ej: "MainViewModel", "Repository")
     * @param message Mensaje descriptivo del evento
     */
    fun d(tag: String, message: String) {
        if (LOGGING_ENABLED) {
            Log.d("$APP_TAG:$tag", message)
        }
    }

    /**
     * Log de nivel INFO - Para eventos importantes del sistema
     *
     * Uso: Inicio/fin de operaciones, cambios de estado significativos
     *
     * @param tag Identificador del componente
     * @param message Mensaje descriptivo del evento
     */
    fun i(tag: String, message: String) {
        if (LOGGING_ENABLED) {
            Log.i("$APP_TAG:$tag", message)
        }
    }

    /**
     * Log de nivel WARNING - Para situaciones anómalas no críticas
     *
     * Uso: Fallbacks, datos faltantes, comportamientos inesperados
     *
     * @param tag Identificador del componente
     * @param message Mensaje descriptivo del evento
     */
    fun w(tag: String, message: String) {
        if (LOGGING_ENABLED) {
            Log.w("$APP_TAG:$tag", message)
        }
    }

    /**
     * Log de nivel ERROR - Para errores y excepciones (Paso 3)
     *
     * Uso: Excepciones capturadas, fallos de operación, errores de red
     *
     * @param tag Identificador del componente
     * @param message Mensaje descriptivo del error
     * @param throwable Excepción opcional para incluir stack trace
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (LOGGING_ENABLED) {
            if (throwable != null) {
                Log.e("$APP_TAG:$tag", message, throwable)
            } else {
                Log.e("$APP_TAG:$tag", message)
            }
        }
    }

    /**
     * Log de operación de red - Específico para llamadas API
     *
     * Registra información detallada de llamadas HTTP para debugging
     *
     * @param endpoint Endpoint de la API llamado
     * @param success Indica si la operación fue exitosa
     * @param details Detalles adicionales (código de respuesta, tiempo, etc.)
     */
    fun network(endpoint: String, success: Boolean, details: String = "") {
        val status = if (success) "SUCCESS" else "FAILURE"
        val detailsPart = if (details.isNotEmpty()) " | $details" else ""
        i("Network", "[$status] $endpoint$detailsPart")
    }

    /**
     * Log de operación de base de datos - Específico para Room
     *
     * Registra operaciones CRUD en la base de datos local
     *
     * @param operation Tipo de operación (INSERT, UPDATE, DELETE, QUERY)
     * @param entity Nombre de la entidad afectada
     * @param details Detalles adicionales
     */
    fun database(operation: String, entity: String, details: String = "") {
        d("Database", "[$operation] $entity: $details")
    }

    /**
     * Log de rendimiento - Para medir tiempos de ejecución
     *
     * @param tag Identificador del componente
     * @param operation Nombre de la operación medida
     * @param durationMs Duración en milisegundos
     */
    fun performance(tag: String, operation: String, durationMs: Long) {
        d("Perf:$tag", "$operation completado en ${durationMs}ms")
    }
}
