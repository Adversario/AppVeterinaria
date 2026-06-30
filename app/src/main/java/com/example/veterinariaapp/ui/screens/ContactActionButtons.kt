package com.example.veterinariaapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ContactActionButtons(phone: String?) {
    val context = LocalContext.current
    val cleanPhone = phone.orEmpty().trim()
    val whatsAppPhone = cleanPhone.filter { it.isDigit() }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
            enabled = cleanPhone.isNotBlank(),
            onClick = {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")))
            }
        ) {
            Icon(Icons.Filled.Phone, contentDescription = "Llamar")
        }

        IconButton(
            enabled = whatsAppPhone.isNotBlank(),
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsAppPhone"))
                )
            }
        ) {
            Icon(Icons.Filled.Chat, contentDescription = "WhatsApp")
        }
    }
}
