package com.miempresa.veterinaria.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference

/**
 * SimuladorFuga - Herramienta para demostrar y detectar Memory Leaks (Paso 4-5)
 *
 * Este objeto singleton demuestra diferentes tipos de fugas de memoria comunes
 * en Android y cómo LeakCanary las detecta.
 *
 * IMPORTANTE: Este código es SOLO para propósitos educativos y de testing.
 * Las fugas aquí implementadas son INTENCIONALES para que LeakCanary las detecte.
 *
 * Tipos de fugas demostradas:
 * 1. Contexto atrapado en singleton (más común)
 * 2. Listener no liberado
 * 3. Handler con referencia a Activity
 * 4. Colección que crece indefinidamente
 *
 * Uso para testing:
 * 1. Descomentar la línea en MainActivity.onCreate()
 * 2. Abrir y cerrar la app varias veces
 * 3. Esperar la notificación de LeakCanary
 * 4. Analizar el heap trace generado
 *
 * @author Veterinaria App Team
 * @since 1.0
 */
object SimuladorFuga {

    private const val TAG = "SimuladorFuga"

    // ============================================================================
    // FUGA TIPO 1: Contexto atrapado en variable estática (Paso 5)
    // ============================================================================
    // PROBLEMA: El Context (Activity) se guarda en un objeto estático que vive
    // mientras la app esté en memoria. Cuando la Activity se destruye, el GC
    // no puede liberarla porque SimuladorFuga mantiene una referencia.
    //
    // SÍNTOMAS en LeakCanary:
    // - "MainActivity has leaked"
    // - Path: SimuladorFuga.contextoAtrapado -> MainActivity instance
    //
    // SOLUCIÓN: Usar WeakReference o ApplicationContext
    // ============================================================================
    var contextoAtrapado: Context? = null

    // ============================================================================
    // FUGA TIPO 2: Lista que acumula contextos (Paso 5)
    // ============================================================================
    // PROBLEMA: Cada vez que se llama acumularContexto(), se agrega una referencia
    // fuerte a la lista. Las Activities destruidas permanecen en memoria.
    //
    // SÍNTOMAS en LeakCanary:
    // - Múltiples instancias de Activity retenidas
    // - Path: SimuladorFuga.listaDeContextos -> ArrayList -> MainActivity
    //
    // SOLUCIÓN: Limpiar la lista en onDestroy() o usar WeakReference
    // ============================================================================
    private val listaDeContextos = mutableListOf<Context>()

    /**
     * Acumula contextos en la lista (CAUSA FUGA)
     *
     * @param context Context a agregar (generalmente una Activity)
     */
    fun acumularContexto(context: Context) {
        listaDeContextos.add(context)
        AppLogger.w(TAG, "Contexto acumulado. Total en lista: ${listaDeContextos.size}")
    }

    // ============================================================================
    // FUGA TIPO 3: Callback/Listener no liberado (Paso 5)
    // ============================================================================
    // PROBLEMA: El callback mantiene una referencia implícita a la Activity
    // (a través del closure o inner class). Si no se libera, la Activity
    // permanece en memoria.
    //
    // SÍNTOMAS en LeakCanary:
    // - "Anonymous class implementing FugaCallback has leaked"
    // - Path: SimuladorFuga.callbackRetenido -> anonymous class -> MainActivity
    //
    // SOLUCIÓN: Remover el callback en onDestroy() o usar WeakReference
    // ============================================================================

    /** Interface de callback que puede causar fugas si no se libera */
    interface FugaCallback {
        fun onEvento(mensaje: String)
    }

    private var callbackRetenido: FugaCallback? = null

    /**
     * Registra un callback que será retenido (CAUSA FUGA si no se libera)
     *
     * @param callback Callback a retener
     */
    fun registrarCallback(callback: FugaCallback) {
        callbackRetenido = callback
        AppLogger.w(TAG, "Callback registrado - puede causar fuga si no se libera")
    }

    /**
     * Libera el callback registrado (SOLUCIÓN a la fuga)
     */
    fun liberarCallback() {
        callbackRetenido = null
        AppLogger.i(TAG, "Callback liberado correctamente")
    }

    // ============================================================================
    // FUGA TIPO 4: Handler con Runnable que referencia Activity (Paso 5)
    // ============================================================================
    // PROBLEMA: El Handler mantiene el Runnable en su cola de mensajes.
    // Si el Runnable es una inner class o lambda que captura 'this' de la Activity,
    // la Activity no puede ser garbage collected hasta que el mensaje se procese.
    //
    // SÍNTOMAS en LeakCanary:
    // - "Handler.mCallback or Message.obj has leaked"
    // - Path: Handler -> MessageQueue -> Runnable -> Activity
    //
    // SOLUCIÓN: Usar WeakReference en el Runnable, o Handler.removeCallbacks()
    // ============================================================================

    private val handler = Handler(Looper.getMainLooper())
    private var runnablePendiente: Runnable? = null

    /**
     * Programa una tarea con delay que retiene el contexto (CAUSA FUGA)
     *
     * @param context Context que será capturado en el closure
     * @param delayMs Delay en milisegundos antes de ejecutar
     */
    fun programarTareaConFuga(context: Context, delayMs: Long = 30000) {
        runnablePendiente = Runnable {
            // Este closure captura 'context' - si la Activity se destruye antes
            // de que se ejecute, habrá una fuga
            AppLogger.d(TAG, "Tarea ejecutada con contexto: ${context.javaClass.simpleName}")
        }
        handler.postDelayed(runnablePendiente!!, delayMs)
        AppLogger.w(TAG, "Tarea programada con delay de ${delayMs}ms - contexto retenido")
    }

    /**
     * Cancela la tarea pendiente (SOLUCIÓN a la fuga)
     */
    fun cancelarTareaPendiente() {
        runnablePendiente?.let { handler.removeCallbacks(it) }
        runnablePendiente = null
        AppLogger.i(TAG, "Tarea pendiente cancelada - contexto liberado")
    }

    // ============================================================================
    // SOLUCIONES RECOMENDADAS (Paso 5 - Correcciones)
    // ============================================================================

    /**
     * WeakReference - Solución para retener contexto sin causar fuga
     *
     * WeakReference permite que el GC libere el objeto referenciado cuando
     * no hay otras referencias fuertes.
     */
    private var contextoDebil: WeakReference<Context>? = null

    /**
     * Guarda el contexto de forma segura usando WeakReference
     *
     * @param context Context a guardar
     */
    fun guardarContextoSeguro(context: Context) {
        contextoDebil = WeakReference(context)
        AppLogger.i(TAG, "Contexto guardado con WeakReference - NO causará fuga")
    }

    /**
     * Obtiene el contexto guardado (puede ser null si fue garbage collected)
     *
     * @return Context si aún está disponible, null si fue liberado
     */
    fun obtenerContextoSeguro(): Context? {
        return contextoDebil?.get()
    }

    // ============================================================================
    // MÉTODOS DE LIMPIEZA
    // ============================================================================

    /**
     * Limpia todas las referencias para prevenir fugas
     *
     * IMPORTANTE: Llamar este método cuando la Activity/Fragment se destruya
     */
    fun limpiarTodo() {
        contextoAtrapado = null
        listaDeContextos.clear()
        callbackRetenido = null
        cancelarTareaPendiente()
        contextoDebil = null
        AppLogger.i(TAG, "Todas las referencias limpiadas")
    }

    /**
     * Obtiene estadísticas de retención para debugging
     *
     * @return String con información de objetos retenidos
     */
    fun obtenerEstadisticas(): String {
        return buildString {
            appendLine("=== SimuladorFuga Stats ===")
            appendLine("Contexto atrapado: ${contextoAtrapado != null}")
            appendLine("Contextos en lista: ${listaDeContextos.size}")
            appendLine("Callback retenido: ${callbackRetenido != null}")
            appendLine("Runnable pendiente: ${runnablePendiente != null}")
            appendLine("Contexto débil disponible: ${contextoDebil?.get() != null}")
        }
    }
}
