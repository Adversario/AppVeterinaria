package com.example.veterinariaapp.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.veterinariaapp.R
import kotlin.concurrent.thread

class ReminderService : Service() {

    override fun onCreate() {
        super.onCreate()
        crearCanal()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notifId = 1001
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("VeterinariaApp")
            .setContentText("Simulando proceso/carga...")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, false)

        // Foreground para que no lo mate en Android moderno
        startForeground(notifId, builder.build())

        thread {
            for (p in 0..100 step 10) {
                Thread.sleep(350)
                nm.notify(
                    notifId,
                    builder.setProgress(100, p, false)
                        .setContentText("Progreso: $p%")
                        .build()
                )
            }

            nm.notify(
                notifId,
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("VeterinariaApp")
                    .setContentText("Proceso finalizado ✅")
                    .setOngoing(false)
                    .build()
            )

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Veterinaria Notificaciones",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "vet_channel"
    }
}