package com.example.veterinariaapp.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class PowerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val msg = when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> "Cargador conectado 🔌"
            Intent.ACTION_POWER_DISCONNECTED -> "Cargador desconectado 🔋"
            else -> "Evento: ${intent.action}"
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}