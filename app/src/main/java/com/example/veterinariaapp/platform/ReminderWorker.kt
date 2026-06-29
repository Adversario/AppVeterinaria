package com.example.veterinariaapp.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.veterinariaapp.R

class ReminderWorker(
    ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Recordatorio"
        val message = inputData.getString("message") ?: "Tienes un evento pendiente."

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "vet_reminders"

        val channel = NotificationChannel(
            channelId,
            "Veterinaria - Recordatorios",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(channel)

        val notif = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        nm.notify((System.currentTimeMillis() % 100000).toInt(), notif)
        return Result.success()
    }
}