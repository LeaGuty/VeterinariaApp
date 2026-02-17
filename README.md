# Documentación Técnica - Veterinaria App

## Resumen de Mejoras Implementadas

Este documento detalla las mejoras técnicas avanzadas implementadas en la aplicación Veterinaria App, siguiendo los 6 pasos establecidos.

---

## Paso 1: Flujo Funcional Seleccionado

### Flujo: Registro de Clientes con Foto

Se seleccionó el flujo de **registro de clientes con obtención automática de foto** por su complejidad técnica:

```
Usuario ingresa datos → API RandomUser → Obtiene foto → Guarda en Room → Actualiza UI
```

**Archivos involucrados:**
- `MainViewModel.kt` - Coordina el flujo
- `VeterinariaRepository.kt` - Acceso a datos
- `RetrofitClient.kt` - Cliente HTTP
- `PantallasGestion.kt` - UI del formulario

**Justificación:**
- Combina operaciones locales (Room) y remotas (Retrofit)
- Requiere manejo de estados de carga y errores
- Demuestra uso de Coroutines en segundo plano
- Ejemplifica el patrón Repository

---

## Paso 2: Procesos en Segundo Plano con Kotlin Coroutines

### Implementación en MainViewModel

```kotlin
fun registrarClienteConFoto(nombre: String, correo: String, telefono: String, rut: String) =
    viewModelScope.launch {
        try {
            estaCargando.value = true

            // Llamada asíncrona a API
            val response = RetrofitClient.instancia.obtenerUsuarioAleatorio()
            val urlImagen = response.results.firstOrNull()?.picture?.large

            // Guardar en BD (también asíncrono)
            repository.agregarCliente(nuevoCliente)

        } catch (e: Exception) {
            // Fallback sin foto
        } finally {
            estaCargando.value = false
        }
    }
```

### Implementación en VeterinariaRepository

```kotlin
suspend fun obtenerVeterinariosConFotos(): List<Veterinario> = withContext(Dispatchers.IO) {
    // Llamadas paralelas con coroutineScope + async
    coroutineScope {
        NOMBRES_VETERINARIOS.map { nombre ->
            async { obtenerVeterinarioConFoto(nombre) }
        }.awaitAll()
    }
}
```

**Características:**
- `viewModelScope` para operaciones ligadas al ciclo de vida
- `Dispatchers.IO` para operaciones de I/O
- `async/awaitAll` para llamadas paralelas (optimización N+1)
- Estados de carga (`estaCargando`) para feedback en UI

---

## Paso 3: Técnicas de Debugging

### Sistema de Logging Centralizado (AppLogger)

**Ubicación:** `util/AppLogger.kt`

```kotlin
object AppLogger {
    fun d(tag: String, message: String)  // Debug
    fun i(tag: String, message: String)  // Info
    fun w(tag: String, message: String)  // Warning
    fun e(tag: String, message: String, throwable: Throwable? = null)  // Error
    fun network(endpoint: String, success: Boolean, details: String)  // Red
    fun database(operation: String, entity: String, details: String)  // BD
    fun performance(tag: String, operation: String, durationMs: Long)  // Rendimiento
}
```

**Características:**
- Desactivación automática en builds de producción
- Tags consistentes con prefijo `VeterinariaApp:`
- Métodos especializados para red, BD y rendimiento

### Try-Catch en Operaciones Críticas

```kotlin
suspend fun agregarCliente(cliente: Cliente) = withContext(Dispatchers.IO) {
    try {
        dao.insertarCliente(entity)
        AppLogger.database("INSERT", "Cliente", "RUT: ${cliente.rut}")
    } catch (e: Exception) {
        AppLogger.e(TAG, "Error al insertar cliente: ${cliente.rut}", e)
        throw e
    }
}
```

### Simulación de Errores para Testing

```kotlin
companion object {
    var simularErrorRed: Boolean = false
}

suspend fun obtenerVeterinariosConFotos() = withContext(Dispatchers.IO) {
    if (simularErrorRed) {
        throw IOException("Error de red simulado para testing")
    }
    // ... código normal
}
```

**Uso desde ViewModel:**
```kotlin
viewModel.configurarSimulacionErrorRed(true)  // Activar simulación
viewModel.recargarVeterinarios()               // Probar comportamiento
```

---

## Paso 4: Integración de LeakCanary

### Configuración en build.gradle.kts

```kotlin
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
```

**Características de LeakCanary:**
- Detección automática de memory leaks
- Generación de heap dumps
- Notificaciones cuando detecta fugas
- Análisis detallado del leak path
- Solo activo en builds DEBUG

---

## Paso 5: Detección y Corrección de Memory Leaks

### Escenarios de Fuga Demostrados

**Ubicación:** `util/SimuladorFuga.kt`

#### Tipo 1: Contexto Atrapado en Singleton
```kotlin
object SimuladorFuga {
    var contextoAtrapado: Context? = null  // ❌ CAUSA FUGA
}

// En MainActivity.onCreate():
SimuladorFuga.contextoAtrapado = this  // La Activity nunca será liberada
```

**Heap Trace esperado:**
```
├─ SimuladorFuga class
│    ↓ static SimuladorFuga.contextoAtrapado
╰→ MainActivity instance
```

#### Tipo 2: Lista que Acumula Contextos
```kotlin
private val listaDeContextos = mutableListOf<Context>()

fun acumularContexto(context: Context) {
    listaDeContextos.add(context)  // ❌ CAUSA FUGA
}
```

#### Tipo 3: Callback No Liberado
```kotlin
private var callbackRetenido: FugaCallback? = null

fun registrarCallback(callback: FugaCallback) {
    callbackRetenido = callback  // ❌ CAUSA FUGA si no se libera
}
```

#### Tipo 4: Handler con Runnable Pendiente
```kotlin
handler.postDelayed(Runnable {
    // Captura 'context' en el closure
    Log.d(TAG, "Ejecutando en $context")
}, 60000)  // ❌ CAUSA FUGA si Activity se destruye antes
```

### Soluciones Implementadas

#### Solución 1: Limpiar en onDestroy()
```kotlin
override fun onDestroy() {
    SimuladorFuga.limpiarTodo()  // ✅ PREVIENE FUGAS
    super.onDestroy()
}
```

#### Solución 2: Usar WeakReference
```kotlin
private var contextoDebil: WeakReference<Context>? = null

fun guardarContextoSeguro(context: Context) {
    contextoDebil = WeakReference(context)  // ✅ NO CAUSA FUGA
}
```

### Cómo Reproducir la Fuga para LeakCanary

1. **Descomentar** la línea en `MainActivity.onCreate()`:
   ```kotlin
   SimuladorFuga.contextoAtrapado = this
   ```

2. **Comentar** la limpieza en `onDestroy()`:
   ```kotlin
   // SimuladorFuga.limpiarTodo()  // COMENTAR ESTO
   ```

3. **Ejecutar** la app y navegar entre pantallas

4. **Presionar Back** varias veces para destruir MainActivity

5. **Esperar** la notificación de LeakCanary

6. **Analizar** el heap trace generado

---

## Paso 6: Integración de Librerías Externas

### 1. Retrofit (API REST)

**Justificación técnica:**
- Conversión automática JSON ↔ Kotlin con Gson
- Soporte nativo para Coroutines (suspend functions)
- Interceptors para logging y debugging
- Type-safe API definitions

**Configuración:**
```kotlin
object RetrofitClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    val instancia: RandomUserService by lazy {
        Retrofit.Builder()
            .baseUrl("https://randomuser.me/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RandomUserService::class.java)
    }
}
```

### 2. Glide (Carga de Imágenes)

**Justificación técnica:**
- Caché automático en memoria y disco
- Decodificación eficiente (reduce OOM)
- Gestión automática del ciclo de vida
- Integración con Compose

**Uso:**
```kotlin
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ClienteItem(cliente: Cliente) {
    GlideImage(
        model = cliente.fotoUri,
        contentDescription = "Foto",
        modifier = Modifier.size(50.dp).clip(CircleShape)
    )
}
```

### 3. LeakCanary (Detección de Memory Leaks)

**Justificación técnica:**
- Detección automática de fugas
- Heap dumps con análisis detallado
- Solo activo en DEBUG (no afecta producción)
- Notificaciones cuando detecta problemas

### 4. OkHttp Logging Interceptor

**Justificación técnica:**
- Logging detallado de requests/responses HTTP
- Facilita debugging de problemas de red
- Configurable por nivel (NONE, BASIC, HEADERS, BODY)

---

## Arquitectura MVVM

```
┌─────────────────────────────────────────────────────────┐
│                     UI (Compose)                         │
│  MainActivity → GestionActivity → Composables            │
└────────────────────┬────────────────────────────────────┘
                     │ Estados observables (mutableStateOf)
┌────────────────────▼────────────────────────────────────┐
│                   ViewModel Layer                        │
│   MainViewModel (viewModelScope, Coroutines)             │
└────────────────────┬────────────────────────────────────┘
                     │ Operaciones suspend
┌────────────────────▼────────────────────────────────────┐
│                   Repository Layer                       │
│   VeterinariaRepository (Dispatchers.IO)                 │
└──────────┬─────────────────────┬────────────────────────┘
           │                     │
┌──────────▼──────────┐ ┌───────▼──────────┐
│   Room Database     │ │   Retrofit API   │
│   (SQLite local)    │ │   (RandomUser)   │
└─────────────────────┘ └──────────────────┘
```

---

## Resumen de Archivos Modificados

| Archivo | Cambios |
|---------|---------|
| `build.gradle.kts` | + OkHttp Logging Interceptor, documentación de dependencias |
| `AppLogger.kt` | NUEVO - Sistema de logging centralizado |
| `SimuladorFuga.kt` | REESCRITO - Demostración de memory leaks |
| `RetrofitClient.kt` | + Logging interceptor, timeouts, documentación |
| `VeterinariaRepository.kt` | + Coroutines paralelas, try-catch, logging |
| `MainViewModel.kt` | + Estados de carga/error, debugging, documentación |
| `MainActivity.kt` | + Demostración LeakCanary, ciclo de vida |

---

## Filtrar Logs en Logcat

Para ver solo los logs de la aplicación:

```
VeterinariaApp:*
```

Para ver logs específicos:
- `VeterinariaApp:MainViewModel` - ViewModel
- `VeterinariaApp:VeterinariaRepo` - Repository
- `VeterinariaApp:OkHttp` - Llamadas HTTP
- `VeterinariaApp:Database` - Operaciones BD
- `VeterinariaApp:SimuladorFuga` - Memory leaks

---

## Autor

Veterinaria App Team
Versión: 1.0
