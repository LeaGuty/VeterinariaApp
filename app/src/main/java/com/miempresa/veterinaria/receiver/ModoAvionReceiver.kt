package com.miempresa.veterinaria.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ModoAvionReceiver : BroadcastReceiver() {
    // Esta función se ejecuta AUTOMÁTICAMENTE cuando ocurre el evento
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            val esModoAvion = intent.getBooleanExtra("state", false)
            val mensaje = if (esModoAvion) "✈️ Modo Avión ACTIVADO" else "✈️ Modo Avión DESACTIVADO"

            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
        }
    }
}