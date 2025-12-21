package com.miempresa.veterinaria.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.miempresa.veterinaria.receiver.ModoAvionReceiver
import com.miempresa.veterinaria.ui.GestionActivity
import kotlin.random.Random

class RecordatorioService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val modoAvionReceiver = ModoAvionReceiver()
    private val CHANNEL_ID = "canal_veterinaria_recordatorios"

    private val runnableCode = object : Runnable {
        override fun run() {
            lanzarNotificacion("Recordatorio Veterinaria", "¡Recuerda revisar la agenda de hoy!")
            handler.postDelayed(this, 30000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        crearCanalNotificacion()
        
        // Registrar el Receiver aquí permite que funcione en segundo plano
        val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        registerReceiver(modoAvionReceiver, filter)
        
        handler.post(runnableCode)
    }

    private fun lanzarNotificacion(titulo: String, mensaje: String) {
        val intent = Intent(this, GestionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(), builder.build())
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios Veterinarios"
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnableCode)
        try {
            unregisterReceiver(modoAvionReceiver)
        } catch (e: Exception) { }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}