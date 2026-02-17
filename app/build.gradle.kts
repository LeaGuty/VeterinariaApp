plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // Plugin de KSP agregado correctamente
}

android {
    namespace = "com.miempresa.veterinaria"
    compileSdk = 35 // CORREGIDO: Usar asignación estándar (ajusta a 34 o 35 según tu SDK instalado)

    defaultConfig {
        applicationId = "com.miempresa.veterinaria"
        minSdk = 24
        targetSdk = 35 // CORREGIDO: Debe coincidir con compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        // Habilita generación de BuildConfig (requerido desde AGP 8.0+)
        // Necesario para acceder a BuildConfig.DEBUG en AppLogger
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // Dependencias de Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // <--- ¡AQUÍ FALTABA EL PARÉNTESIS DE CIERRE!

    // Retrofit para la API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ============================================================================
    // LIBRERÍA EXTERNA: GLIDE (Paso 6)
    // ============================================================================
    // Justificación técnica: Glide es una librería de carga de imágenes optimizada
    // que proporciona:
    // - Caché automático en memoria y disco
    // - Decodificación eficiente de imágenes (reduce OOM)
    // - Gestión automática del ciclo de vida
    // - Transformaciones (redondeo, blur, etc.)
    // - Integración nativa con Compose mediante el módulo compose
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")

    // ============================================================================
    // LIBRERÍA EXTERNA: RETROFIT + OKHTTP (Paso 6)
    // ============================================================================
    // Justificación técnica: Retrofit simplifica las llamadas HTTP REST mediante:
    // - Conversión automática JSON <-> Kotlin objects (Gson)
    // - Soporte nativo para Coroutines (suspend functions)
    // - Type-safe API definitions con interfaces
    // - Interceptors para logging y debugging (OkHttp)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp Logging Interceptor para debugging de llamadas HTTP (Paso 3)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ============================================================================
    // DIAGNÓSTICO DE MEMORIA: LEAKCANARY (Paso 4)
    // ============================================================================
    // Justificación técnica: LeakCanary detecta memory leaks automáticamente:
    // - Monitorea Activities, Fragments, Views y ViewModels
    // - Genera heap dumps cuando detecta objetos que no se liberan
    // - Proporciona trace detallado del leak path
    // - Solo activo en builds DEBUG (no afecta producción)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

}