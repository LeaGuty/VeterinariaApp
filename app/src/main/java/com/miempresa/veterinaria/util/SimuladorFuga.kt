package com.miempresa.veterinaria.util

import android.content.Context

// Este objeto (Singleton) vive mientras la app esté abierta.
object SimuladorFuga {
    // ⚠️ PELIGRO: Guardar un contexto de Activity en una variable estática
    // causará que la Activity nunca se pueda borrar de la memoria.
    var contextoAtrapado: Context? = null
}