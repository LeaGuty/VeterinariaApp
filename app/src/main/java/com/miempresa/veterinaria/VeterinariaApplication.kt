package com.miempresa.veterinaria

import android.app.Application
import com.miempresa.veterinaria.data.VeterinariaDatabase
import com.miempresa.veterinaria.data.VeterinariaRepository
import com.miempresa.veterinaria.util.AppLogger

/**
 * VeterinariaApplication - Clase Application personalizada (Paso 6 - Arquitectura)
 *
 * Punto de entrada de la aplicación que proporciona instancias singleton
 * de componentes compartidos (Database, Repository) mediante inicialización lazy.
 *
 * Patrón de diseño:
 * - Singleton para Database y Repository
 * - Lazy initialization para optimizar tiempo de arranque
 * - Service Locator simplificado (sin Hilt/Dagger)
 *
 * Configuración requerida en AndroidManifest.xml:
 * ```xml
 * <application
 *     android:name=".VeterinariaApplication"
 *     ...>
 * ```
 *
 * Uso desde Activities/ViewModels:
 * ```kotlin
 * val app = application as VeterinariaApplication
 * val repository = app.repository
 * ```
 *
 * Integración con LeakCanary (Paso 4):
 * LeakCanary se inicializa automáticamente al incluir la dependencia.
 * No requiere código adicional en esta clase.
 *
 * @author Veterinaria App Team
 * @since 1.0
 */
class VeterinariaApplication : Application() {

    companion object {
        private const val TAG = "VeterinariaApp"
    }

    /**
     * Instancia singleton de la base de datos Room
     *
     * Características:
     * - Inicialización lazy (solo cuando se necesita)
     * - Thread-safe por defecto
     * - Persiste durante todo el ciclo de vida de la app
     *
     * Ubicación del archivo: /data/data/com.miempresa.veterinaria/databases/
     */
    val database: VeterinariaDatabase by lazy {
        AppLogger.i(TAG, "Inicializando Room Database")
        VeterinariaDatabase.getDatabase(this)
    }

    /**
     * Instancia singleton del Repository
     *
     * Proporciona acceso unificado a:
     * - Base de datos local (Room)
     * - API remota (Retrofit/RandomUser)
     *
     * Características:
     * - Inicialización lazy
     * - Depende de la instancia de Database
     * - Expone Flows reactivos para observación de datos
     */
    val repository: VeterinariaRepository by lazy {
        AppLogger.i(TAG, "Inicializando Repository")
        VeterinariaRepository(database.veterinariaDao())
    }

    /**
     * Callback de inicialización de la aplicación
     *
     * Se ejecuta antes de crear cualquier Activity, Service o BroadcastReceiver.
     * Aquí se pueden realizar inicializaciones globales si es necesario.
     */
    override fun onCreate() {
        super.onCreate()
        AppLogger.i(TAG, "Application onCreate - App iniciada")

        // LeakCanary se auto-inicializa al incluir la dependencia
        // No requiere código adicional aquí
    }
}
