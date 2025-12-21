package com.miempresa.veterinaria.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.miempresa.veterinaria.R
import com.miempresa.veterinaria.ui.GestionActivity
import kotlin.random.Random

class RecordatorioService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val CHANNEL_ID = "canal_veterinaria_recordatorios"

    private val runnableCode = object : Runnable {
        override fun run() {
            // Simulamos un evento cada 30 segundos para efectos de la demo
            lanzarNotificacion("Recordatorio Veterinaria", "¡Recuerda revisar la agenda de hoy!")

            // Repetir cada 30 segundos (en una app real sería cada hora o diario)
            handler.postDelayed(this, 30000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        crearCanalNotificacion()
        // Iniciar el ciclo de recordatorios
        handler.post(runnableCode)
    }

    private fun lanzarNotificacion(titulo: String, mensaje: String) {
        // Intent para abrir la app al tocar la notificación
        val intent = Intent(this, GestionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icono del sistema por defecto
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Lo que pasa al tocar
            .setAutoCancel(true) // Se borra al tocarla

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ID único para cada notificación
        notificationManager.notify(Random.nextInt(), builder.build())
    }

    private fun crearCanalNotificacion() {
        // Crear el canal de notificaciones (Obligatorio para Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios Veterinarios"
            val descriptionText = "Notificaciones de citas y vacunas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnableCode)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}