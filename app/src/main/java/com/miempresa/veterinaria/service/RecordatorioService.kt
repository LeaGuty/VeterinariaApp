package com.miempresa.veterinaria.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast

class RecordatorioService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null // No necesitamos enlazarlo, es un servicio "started"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Mostramos un mensaje de que el servicio arrancó
        Toast.makeText(this, "Servicio de Recordatorios Iniciado 🕒", Toast.LENGTH_SHORT).show()

        // Simulamos una tarea que tarda 5 segundos (ej: verificar citas)
        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(this, "✅ Tarea de fondo: Datos sincronizados", Toast.LENGTH_LONG).show()
            // El servicio se detiene a sí mismo al terminar
            stopSelf()
        }, 5000)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Servicio Detenido", Toast.LENGTH_SHORT).show()
    }
}